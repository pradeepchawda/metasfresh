package de.metas.inoutcandidate.api;

import java.util.Collection;
import java.util.Set;

import org.compiere.model.I_C_OrderLine;
import org.compiere.model.I_M_InOut;
import org.compiere.model.I_M_InOutLine;

import de.metas.inoutcandidate.model.I_M_ShipmentSchedule;
import de.metas.process.PInstanceId;
import de.metas.product.ProductId;
import de.metas.storage.IStorageSegment;
import de.metas.util.ISingletonService;

public interface IShipmentScheduleInvalidateBL extends ISingletonService
{
	boolean isInvalid(ShipmentScheduleId shipmentScheduleId);

	void invalidateShipmentSchedule(ShipmentScheduleId shipmentScheduleId);

	void invalidateShipmentSchedules(Set<ShipmentScheduleId> shipmentScheduleIds);

	void invalidateStorageSegment(IStorageSegment storageSegment);

	void invalidateStorageSegments(Collection<IStorageSegment> storageSegments);

	/**
	 * Invalidates shipment schedules for the given storage segments.
	 * <p>
	 * <b>IMPORTANT:</b> won't invalidate any processed schedules.
	 *
	 * @param storageSegments
	 * @param addToSelectionId if not null will add the invalidated records to given selection
	 */
	void invalidateStorageSegments(Collection<IStorageSegment> storageSegments, PInstanceId addToSelectionId);

	/**
	 * Invalidate just the shipment schedules that directly reference the given <code>shipment</code>'s lines.<br>
	 * Use this method if you know that no re-allocation of on-hand-qtys is required, but just the affected schedules
	 * need to be updated (e.g. QtyPicked => QtyDelivered, if an InOut is completed).
	 */
	void invalidateJustForLines(I_M_InOut shipment);

	/**
	 * See {@link #invalidateJustForLines(I_M_InOut)}.
	 */
	void invalidateJustForLine(I_M_InOutLine shipmentLine);

	/**
	 * See {@link #notifySegmentChangedForShipmentLine(I_M_InOutLine)}.
	 */
	void notifySegmentsChangedForShipment(I_M_InOut shipment);

	/**
	 * Invalidates all shipment schedules that have the same product, bPartner, ASI and locator as the given line<br>
	 * and <b>that that do not have "force" as delivery rule</b>.<br>
	 * Notes:
	 * <ul>
	 * <li>the set of such schedules is usually relatively small, compared with the set of all schedules that have the same product.
	 * <li>As stated, do not invalidate scheds with delivery rule force, because to get their QtyToDeliver, they don't need to care about other schedules anyways. That means that a dev might need to
	 * call {@link #invalidateJustForLines(I_M_InOut)} in addition to this method.
	 * <ul>
	 *
	 * @param shipmentLine
	 * @see IShipmentSchedulePA#invalidateStorageSegments(java.util.Collection)
	 */
	void notifySegmentChangedForShipmentLine(I_M_InOutLine shipmentLine);

	/**
	 * For the given <code>schedule</code>, invalidate all shipment schedules that have the same product and warehouse and a matching ASI.
	 */
	void notifySegmentChangedForShipmentSchedule(I_M_ShipmentSchedule schedule);

	/**
	 * For the given <code>orderLine</code>, invalidate all shipment schedules that have the same product and warehouse and a matching ASI.
	 * and <b>that that do not have "force" as delivery rule</b>.<br>
	 */
	void notifySegmentChangedForOrderLine(I_C_OrderLine orderLine);

	/**
	 * Invalidate the shipment schedule referencing the given <code>orderLine</code>.
	 *
	 * @param orderLine
	 */
	void invalidateJustForOrderLine(I_C_OrderLine orderLine);
	
	/**
	 * Sets the {@link I_M_ShipmentSchedule#COLUMNNAME_IsValid} column to <code>'N'</code> for all shipment schedule entries whose order line has the given product id.
	 *
	 * @param productId
	 * @deprecated please be more selective with the invalidation, using storage segments
	 */
	@Deprecated
	void invalidateForProduct(ProductId productId);
	
	/**
	 * Invalidates all shipment schedules which have one of the given <code>headerAggregationKeys</code>.
	 *
	 * @param headerAggregationKeys
	 */
	void invalidateForHeaderAggregationKeys(Set<String> headerAggregationKeys);
	
	void notifySegmentChanged(IStorageSegment storageSegment);

	/**
	 * Notify the registered listeners that a a bunch of segments changed. Maybe they can gain a performance benefit from processing them all at once.
	 * 
	 * @param storageSegments
	 */
	void notifySegmentsChanged(Collection<IStorageSegment> storageSegments);
}
