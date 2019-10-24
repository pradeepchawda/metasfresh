package de.metas.ui.web.pickingV2.productsToPick.rows;

import javax.annotation.Nullable;

import de.metas.handlingunits.HuId;
import de.metas.inoutcandidate.api.ShipmentScheduleId;
import de.metas.material.planning.pporder.PPOrderBOMLineId;
import de.metas.product.ProductId;
import de.metas.ui.web.window.datatypes.DocumentId;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

/*
 * #%L
 * metasfresh-webui-api
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

@EqualsAndHashCode
@ToString(of = "documentId")
public final class ProductsToPickRowId
{
	@Getter
	private final ShipmentScheduleId shipmentScheduleId;
	@Getter
	private final HuId pickFromHUId;

	private final DocumentId documentId;

	@Builder
	private ProductsToPickRowId(
			@NonNull final ProductId productId,
			@NonNull ShipmentScheduleId shipmentScheduleId,
			@Nullable final HuId pickFromHUId,
			@Nullable final PPOrderBOMLineId issueToOrderBOMLineId)
	{
		this.shipmentScheduleId = shipmentScheduleId;
		this.pickFromHUId = pickFromHUId;

		this.documentId = createDocumentId(productId, shipmentScheduleId, pickFromHUId, issueToOrderBOMLineId);
	}

	public DocumentId toDocumentId()
	{
		return documentId;
	}

	private static DocumentId createDocumentId(
			@NonNull final ProductId productId,
			@NonNull final ShipmentScheduleId shipmentScheduleId,
			@Nullable final HuId pickFromHUId,
			@Nullable final PPOrderBOMLineId issueToOrderBOMLineId)
	{
		final StringBuilder sb = new StringBuilder();
		sb.append("P").append(productId.getRepoId());
		sb.append("_").append("S").append(shipmentScheduleId.getRepoId());

		if (pickFromHUId != null)
		{
			sb.append("_").append("HU").append(pickFromHUId.getRepoId());
		}

		if (issueToOrderBOMLineId != null)
		{
			sb.append("_").append("BOM").append(issueToOrderBOMLineId.getRepoId());
		}

		return DocumentId.ofString(sb.toString());
	}

}
