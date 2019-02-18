package de.metas.phonecall;

import java.time.LocalDate;
import java.time.LocalTime;

import javax.annotation.Nullable;

import org.adempiere.user.UserId;

import de.metas.bpartner.BPartnerLocationId;
import lombok.NonNull;
import lombok.Value;

/*
 * #%L
 * de.metas.adempiere.adempiere.base
 * %%
 * Copyright (C) 2019 metas GmbH
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

@Value
public class PhonecallSchedule
{
	@Nullable
	PhonecallScheduleId id;

	@NonNull
	BPartnerLocationId bpartnerAndLocationId;

	@NonNull
	UserId contactId;

	@NonNull
	LocalDate date;
	@NonNull
	LocalTime startTime;
	@NonNull
	LocalTime endTime;

	@NonNull
	PhonecallSchemaVersionLineId schemaVersionLineId;

}
