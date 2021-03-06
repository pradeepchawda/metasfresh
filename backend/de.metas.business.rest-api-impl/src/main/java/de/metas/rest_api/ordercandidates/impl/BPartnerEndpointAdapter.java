package de.metas.rest_api.ordercandidates.impl;

import java.util.Comparator;
import java.util.Optional;

import javax.annotation.Nullable;

import org.adempiere.ad.trx.api.ITrxManager;
import org.adempiere.exceptions.AdempiereException;
import org.slf4j.Logger;
import org.springframework.http.ResponseEntity;

import de.metas.bpartner.BPartnerContactId;
import de.metas.bpartner.BPartnerId;
import de.metas.bpartner.BPartnerLocationId;
import de.metas.bpartner.service.BPartnerInfo;
import de.metas.bpartner.service.BPartnerInfo.BPartnerInfoBuilder;
import de.metas.i18n.TranslatableStrings;
import de.metas.logging.LogManager;
import de.metas.rest_api.bpartner.impl.BpartnerRestController;
import de.metas.rest_api.bpartner.request.JsonRequestBPartner;
import de.metas.rest_api.bpartner.request.JsonRequestBPartnerUpsert;
import de.metas.rest_api.bpartner.request.JsonRequestBPartnerUpsertItem;
import de.metas.rest_api.bpartner.request.JsonRequestComposite;
import de.metas.rest_api.bpartner.request.JsonRequestContactUpsert;
import de.metas.rest_api.bpartner.request.JsonRequestContactUpsert.JsonRequestContactUpsertBuilder;
import de.metas.rest_api.bpartner.request.JsonRequestContactUpsertItem;
import de.metas.rest_api.bpartner.request.JsonRequestLocation;
import de.metas.rest_api.bpartner.request.JsonRequestLocationUpsert;
import de.metas.rest_api.bpartner.request.JsonRequestLocationUpsert.JsonRequestLocationUpsertBuilder;
import de.metas.rest_api.bpartner.request.JsonRequestLocationUpsertItem;
import de.metas.rest_api.bpartner.response.JsonResponseBPartner;
import de.metas.rest_api.bpartner.response.JsonResponseBPartnerCompositeUpsert;
import de.metas.rest_api.bpartner.response.JsonResponseBPartnerCompositeUpsertItem;
import de.metas.rest_api.bpartner.response.JsonResponseComposite;
import de.metas.rest_api.bpartner.response.JsonResponseContact;
import de.metas.rest_api.bpartner.response.JsonResponseLocation;
import de.metas.rest_api.bpartner.response.JsonResponseUpsertItem;
import de.metas.rest_api.common.MetasfreshId;
import de.metas.rest_api.exception.InvalidEntityException;
import de.metas.rest_api.ordercandidates.request.BPartnerLookupAdvise;
import de.metas.rest_api.ordercandidates.request.JsonRequestBPartnerLocationAndContact;
import de.metas.rest_api.utils.IdentifierString;
import de.metas.util.Check;
import de.metas.util.Services;
import de.metas.util.collections.CollectionUtils;
import lombok.NonNull;

/*
 * #%L
 * de.metas.ordercandidate.rest-api-impl
 * %%
 * Copyright (C) 2018 metas GmbH
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 2 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program. If not, see
 * <http://www.gnu.org/licenses/gpl-2.0.html>.
 * #L%
 */

final class BPartnerEndpointAdapter
{
	private static final Logger logger = LogManager.getLogger(BPartnerEndpointAdapter.class);

	private final BpartnerRestController bpartnerRestController;
	private final ITrxManager trxManager = Services.get(ITrxManager.class);

	public BPartnerEndpointAdapter(@NonNull final BpartnerRestController bpartnerRestController)
	{
		this.bpartnerRestController = bpartnerRestController;
	}

	public BPartnerInfo getCreateBPartnerInfoInTrx(
			@Nullable final JsonRequestBPartnerLocationAndContact jsonBPartnerInfo,
			final boolean billTo,
			@Nullable final String orgCode)
	{
		return getCreateBPartnerInfo0(jsonBPartnerInfo, billTo, orgCode);
	}

	private BPartnerInfo getCreateBPartnerInfo0(
			@Nullable final JsonRequestBPartnerLocationAndContact jsonBPartnerInfo,
			final boolean billTo,
			@Nullable final String orgCode)
	{
		if (jsonBPartnerInfo == null)
		{
			return null;
		}
		// repackage request
		final JsonRequestBPartnerUpsert jsonRequestBPartnerUpsert = asJsonRequestBPartnerUpsert(orgCode, jsonBPartnerInfo);

		// Invoke bpartner-endpoint in trx to make sure it is committed and can be found on the next invokation.
		// Otherwise we would need to give them all at once to the endpoint but then we'd need to sort out which is which
		final ResponseEntity<JsonResponseBPartnerCompositeUpsert> response = trxManager.callInNewTrx(() -> bpartnerRestController.createOrUpdateBPartner(jsonRequestBPartnerUpsert));

		// repackage result
		return asBPartnerInfo(response, billTo);
	}

	/**
	 * Note that the resulting jsonRequestBPartnerUpsert has 3 levels of syncAdvises,
	 * but the given jsonBPartnerInfo lacks the "middle" one of those 3.
	 * We supplement the jsonBPartnerInfo's parent (outer) one for both the 1st and 2nd level of the resulting jsonRequestBPartnerUpsert.
	 */
	private JsonRequestBPartnerUpsert asJsonRequestBPartnerUpsert(
			@Nullable final String orgCode,
			@NonNull final JsonRequestBPartnerLocationAndContact jsonBPartnerInfo)
	{
		// location
		final JsonRequestLocationUpsertBuilder jsonRequestLocationUpsert = JsonRequestLocationUpsert.builder();
		if (jsonBPartnerInfo.getLocation() != null)
		{
			final JsonRequestLocation location = jsonBPartnerInfo.getLocation();
			jsonRequestLocationUpsert.requestItem(JsonRequestLocationUpsertItem
					.builder()
					.locationIdentifier(constructLocationIdentifier(location, jsonBPartnerInfo.getBpartnerLookupAdvise()))
					.location(location)
					.build())
					.syncAdvise(jsonBPartnerInfo.getSyncAdvise())
					.build();
		}

		// contact
		final JsonRequestContactUpsertBuilder jsonRequestContactUpsert = JsonRequestContactUpsert.builder();
		if (jsonBPartnerInfo.getContact() != null)
		{
			jsonRequestContactUpsert
					.requestItem(JsonRequestContactUpsertItem
							.builder()
							.contactIdentifier(IdentifierString.PREFIX_EXTERNAL_ID + jsonBPartnerInfo.getContact().getExternalId().getValue())
							.contact(jsonBPartnerInfo.getContact())
							.build())
					.syncAdvise(jsonBPartnerInfo.getSyncAdvise())
					.build();
		}

		// bpartner - make sure that we have isCustomer=true unless explicitly specified otherwise
		final JsonRequestBPartner bPartner = jsonBPartnerInfo.getBpartner();
		if (!bPartner.isCustomerSet())
		{
			bPartner.setCustomer(true);
		}

		final JsonRequestComposite bpartnerComposite = JsonRequestComposite.builder()
				.orgCode(orgCode)
				.bpartner(bPartner)
				.locations(jsonRequestLocationUpsert.build())
				.contacts(jsonRequestContactUpsert.build())
				.syncAdvise(jsonBPartnerInfo.getSyncAdvise())
				.build();
		final JsonRequestBPartnerUpsertItem requestItem = JsonRequestBPartnerUpsertItem.builder()
				.bpartnerIdentifier(constructBPartnerIdentifier(jsonBPartnerInfo))
				.bpartnerComposite(bpartnerComposite)
				.build();
		final JsonRequestBPartnerUpsert jsonRequestBPartnerUpsert = JsonRequestBPartnerUpsert.builder()
				.requestItem(requestItem)
				.build();
		return jsonRequestBPartnerUpsert;
	}

	private String constructLocationIdentifier(
			@NonNull final JsonRequestLocation location,
			@Nullable final BPartnerLookupAdvise bpartnerLookupAdvise)
	{

		if (bpartnerLookupAdvise != null)
		{
			// JsonRequestBPartnerLocationAndContact validated in its constructor that this actually works!
			final String result;
			switch (bpartnerLookupAdvise)
			{
				case Code:
					result = null;// nothing we can do
					break;
				case ExternalId:
					result = IdentifierString.PREFIX_EXTERNAL_ID + location.getExternalId().getValue();
					break;
				case GLN:
					result = IdentifierString.PREFIX_GLN + location.getGln();
					break;
				default:
					throw new AdempiereException("Unsupported bpartnerLookupAdvise=" + bpartnerLookupAdvise).setParameter("jsonRequestLocation", location);
			}
			if (result != null)
			{
				logger.debug("jsonRequestLocation has partnerLookupAdvise={}; -> return identifierString={}", bpartnerLookupAdvise, result);
				return result;
			}
		}

		if (location.getExternalId() != null)
		{
			final String result = IdentifierString.PREFIX_EXTERNAL_ID + location.getExternalId().getValue();
			logger.debug("jsonRequestLocation has partnerLookupAdvise={}, but an externalId; -> return identifierString={}", bpartnerLookupAdvise, result);
			return result;
		}
		else if (!Check.isEmpty(location.getGln(), true))
		{
			final String result = IdentifierString.PREFIX_GLN + location.getGln();
			logger.debug("jsonRequestLocation has partnerLookupAdvise={}, but a GLN; -> return identifierString={}", bpartnerLookupAdvise, result);
			return result;
		}

		throw new InvalidEntityException(TranslatableStrings.constant("JsonRequestLocation needs either an externalId or GLN"))
				.appendParametersToMessage()
				.setParameter("jsonRequestLocation", location);
	}

	private String constructBPartnerIdentifier(@NonNull final JsonRequestBPartnerLocationAndContact jsonBPartnerInfo)
	{
		final JsonRequestBPartner bpartner = jsonBPartnerInfo.getBpartner();

		BPartnerLookupAdvise bpartnerLookupAdvise = jsonBPartnerInfo.getBpartnerLookupAdvise();
		if (bpartnerLookupAdvise != null)
		{
			// JsonRequestBPartnerLocationAndContact validated in its constructor that this actually works!
			final String result;
			switch (bpartnerLookupAdvise)
			{
				case Code:
					result = IdentifierString.PREFIX_VALUE + bpartner.getCode();
					break;
				case ExternalId:
					result = IdentifierString.PREFIX_EXTERNAL_ID + bpartner.getExternalId().getValue();
					break;
				case GLN:
					result = IdentifierString.PREFIX_GLN + jsonBPartnerInfo.getLocation().getGln();
					break;
				default:
					throw new AdempiereException("Unsupported bpartnerLookupAdvise=" + bpartnerLookupAdvise).setParameter("jsonRequestBPartnerLocationAndContact", jsonBPartnerInfo);
			}
			logger.debug("jsonBPartnerInfo has partnerLookupAdvise={}; -> return identifierString={}", bpartnerLookupAdvise, result);
			return result;
		}

		if (bpartner.getExternalId() != null)
		{
			final String result = IdentifierString.PREFIX_EXTERNAL_ID + bpartner.getExternalId().getValue();
			logger.debug("jsonBPartnerInfo has partnerLookupAdvise={}, but an externalId; -> return identifierString={}", bpartnerLookupAdvise, result);
			return result;
		}
		else if (jsonBPartnerInfo.getLocation() != null && !Check.isEmpty(jsonBPartnerInfo.getLocation().getGln(), true))
		{
			final String result = IdentifierString.PREFIX_GLN + jsonBPartnerInfo.getLocation().getGln();
			logger.debug("jsonBPartnerInfo has partnerLookupAdvise={}, but a GLN; -> return identifierString={}", bpartnerLookupAdvise, result);
			return result;
		}
		else if (!Check.isEmpty(bpartner.getCode(), true))
		{
			final String result = IdentifierString.PREFIX_VALUE + bpartner.getCode();
			logger.debug("jsonBPartnerInfo has partnerLookupAdvise={}, but a code; -> return identifierString={}", bpartnerLookupAdvise, result);
			return result;
		}

		throw new InvalidEntityException(TranslatableStrings.constant("JsonRequestBPartner needs either an externalId, code or location with a GLN"))
				.appendParametersToMessage()
				.setParameter("jsonRequestBPartner", bpartner);
	}

	private BPartnerInfo asBPartnerInfo(
			@NonNull final ResponseEntity<JsonResponseBPartnerCompositeUpsert> response,
			final boolean billTo)
	{
		// There can be just one response item, because we have just one request item;
		final JsonResponseBPartnerCompositeUpsertItem jsonResponseBPartnerUpsert = CollectionUtils.singleElement(response.getBody().getResponseItems());
		final MetasfreshId bpartnerMetasfreshId = jsonResponseBPartnerUpsert.getResponseBPartnerItem().getMetasfreshId();
		final BPartnerId bpartnerId = BPartnerId.ofRepoId(bpartnerMetasfreshId.getValue());
		final BPartnerInfoBuilder result = BPartnerInfo
				.builder()
				.bpartnerId(bpartnerId);
		if (!jsonResponseBPartnerUpsert.getResponseLocationItems().isEmpty())
		{
			final JsonResponseUpsertItem locationUpsert = CollectionUtils.singleElement(jsonResponseBPartnerUpsert.getResponseLocationItems());
			result.bpartnerLocationId(BPartnerLocationId.ofRepoId(bpartnerId, locationUpsert.getMetasfreshId().getValue()));
		}
		else
		{
			final Comparator<JsonResponseLocation> c = billTo ? createBillToLocationComparator() : createShipToLocationComparator();

			final Optional<JsonResponseLocation> location = bpartnerRestController.retrieveBPartner(Integer.toString(bpartnerId.getRepoId())).getBody().getLocations().stream()
					.sorted(c)
					.findFirst();
			if (location.isPresent())
			{
				result.bpartnerLocationId(BPartnerLocationId.ofRepoId(bpartnerId, location.get().getMetasfreshId().getValue()));
			}
		}
		if (!jsonResponseBPartnerUpsert.getResponseContactItems().isEmpty())
		{
			final JsonResponseUpsertItem contactUpsert = CollectionUtils.singleElement(jsonResponseBPartnerUpsert.getResponseContactItems());
			result.contactId(BPartnerContactId.ofRepoId(bpartnerId, contactUpsert.getMetasfreshId().getValue()));
		}

		return result.build();
	}

	private Comparator<JsonResponseLocation> createBillToLocationComparator()
	{
		return Comparator
				.<JsonResponseLocation, Boolean>comparing(l -> !l.isBillTo() /* billTo=true first */)
				.thenComparing(
						Comparator.comparing(l -> !l.isBillToDefault())/* billToDefault=true first */);
	}

	private Comparator<JsonResponseLocation> createShipToLocationComparator()
	{
		return Comparator
				.<JsonResponseLocation, Boolean>comparing(l -> !l.isShipTo() /* shipTo=true first */)
				.thenComparing(
						Comparator.comparing(l -> !l.isShipToDefault())/* shipToDefault=true first */);
	}

	public JsonResponseBPartner getJsonBPartnerById(@NonNull final BPartnerId bpartnerId)
	{
		final ResponseEntity<JsonResponseComposite> bpartner = bpartnerRestController.retrieveBPartner(
				Integer.toString(bpartnerId.getRepoId()));
		return bpartner.getBody().getBpartner();
	}

	public JsonResponseLocation getJsonBPartnerLocationById(@NonNull final BPartnerLocationId bpartnerLocationId)
	{
		final ResponseEntity<JsonResponseLocation> location = bpartnerRestController.retrieveBPartnerLocation(
				Integer.toString(bpartnerLocationId.getBpartnerId().getRepoId()),
				Integer.toString(bpartnerLocationId.getRepoId()));
		return location.getBody();
	}

	public JsonResponseContact getJsonBPartnerContactById(@NonNull final BPartnerContactId bpartnerContactId)
	{
		final ResponseEntity<JsonResponseContact> contact = bpartnerRestController.retrieveBPartnerContact(
				Integer.toString(bpartnerContactId.getBpartnerId().getRepoId()),
				Integer.toString(bpartnerContactId.getRepoId()));
		return contact.getBody();
	}
}
