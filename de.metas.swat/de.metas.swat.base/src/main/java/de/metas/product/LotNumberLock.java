package de.metas.product;

import org.adempiere.util.Check;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

/*
 * #%L
 * de.metas.swat.base
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

@Value
public class LotNumberLock
{
	int id;
	int productId;
	String lotNo;
	String description;

	@Builder
	public LotNumberLock(
			final int id,
			final int productId,
			@NonNull final String lotNo,
			final String description)
	{
		Check.assumeGreaterThanZero(id, "id");
		Check.assumeGreaterThanZero(productId, "productId");
		Check.assumeNotEmpty(lotNo, "lotNo is not empty");

		this.id = id;
		this.productId = productId;
		this.lotNo = lotNo;
		this.description = description;
	}
}
