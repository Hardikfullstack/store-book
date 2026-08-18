import {
  getPurchaseItemsForFIFO,
  getSaleItemsForFIFO,
  getUdhaarBySaleId,
  syncSale,
  syncUdhaar,
  upsertSaleItemDetail
} from '@/dataconnect';

interface PurchaseBatch {
  id: string;
  purchaseId: string;
  buyPrice: number;
  initialQty: number;
  remainingQty: number;
  timestamp: number;
}

export async function recalculateItemFIFO(
  storeId: string,
  itemId: string,
  dataConnect: any,
  editedPurchaseId?: string
) {
  console.log(`Starting FIFO recalculation for storeId: ${storeId}, itemId: ${itemId}, editedPurchaseId: ${editedPurchaseId}`);
  const now = Date.now();

  // 1. Fetch active purchases and sort them chronologically by purchase timestamp ASC (older records first)
  const purchasesResp: any = await getPurchaseItemsForFIFO(dataConnect, { storeId, itemId });
  const rawPurchases = purchasesResp.data?.purchaseItemDetails || [];
  const purchases = rawPurchases.slice().sort((a: any, b: any) => {
    const tA = a.purchase?.timestamp || a.updatedAt || 0;
    const tB = b.purchase?.timestamp || b.updatedAt || 0;
    if (tA !== tB) return tA - tB;
    return a.updatedAt - b.updatedAt;
  });

  // 2. Fetch active sale item details and sort chronologically by sale timestamp ASC (older sales first)
  const salesResp: any = await getSaleItemsForFIFO(dataConnect, { storeId, itemId });
  const rawSales = salesResp.data?.saleItemDetails || [];
  const saleItems = rawSales.slice().sort((a: any, b: any) => {
    const tA = a.sale?.timestamp || a.updatedAt || 0;
    const tB = b.sale?.timestamp || b.updatedAt || 0;
    if (tA !== tB) return tA - tB;
    return a.updatedAt - b.updatedAt;
  });

  console.log(`Loaded ${purchases.length} purchases and ${saleItems.length} sales for FIFO distribution.`, saleItems);

  // 3. If a specific purchase was edited, allocate that purchase's updated price & quantity to the oldest available sales by timestamp ASC
  if (editedPurchaseId) {
    const editedPurchaseItem = purchases.find((p: any) => p.purchaseId === editedPurchaseId || p.id === editedPurchaseId);
    if (editedPurchaseItem) {
      let remainingEditedQty = editedPurchaseItem.quantity;
      const newPrice = editedPurchaseItem.buyPrice;

      console.log(`Re-attributing edited purchase ${editedPurchaseId}: qty=${remainingEditedQty}, price=${newPrice}`);

      for (const saleItem of saleItems) {
        if (remainingEditedQty <= 0) break;

        const originalSale = saleItem.sale;
        if (!originalSale) continue;

        const existingUpdatedAt = originalSale.updatedAt || saleItem.updatedAt || originalSale.timestamp || 0;
        const existingItemUpdatedAt = saleItem.updatedAt || existingUpdatedAt;

        const matchedQty = Math.min(remainingEditedQty, saleItem.quantity);
        remainingEditedQty -= matchedQty;

        if (matchedQty === saleItem.quantity) {
          // Case A: Full match - update buyPrice and update parent sale timestamp to Date.now(), keep updatedAt as-is
          await upsertSaleItemDetail(dataConnect, {
            id: saleItem.id,
            storeId,
            saleId: saleItem.saleId,
            itemId: saleItem.itemId,
            itemName: saleItem.itemName,
            unit: saleItem.unit,
            quantity: matchedQty,
            sellPrice: saleItem.sellPrice,
            buyPrice: newPrice,
            taxRate: saleItem.taxRate,
            hsnCode: saleItem.hsnCode,
            isDeleted: false,
            updatedAt: existingItemUpdatedAt // Preserved: not updated
          });

          await syncSale(dataConnect, {
            id: saleItem.saleId,
            storeId,
            timestamp: now, // Update timestamp to new Date() as requested
            totalAmount: originalSale.totalAmount,
            discountAmount: originalSale.discountAmount,
            customerName: originalSale.customerName || '',
            customerGstin: originalSale.customerGstin || '',
            businessGstin: originalSale.businessGstin || '',
            customerAddress: originalSale.customerAddress || '',
            businessAddress: originalSale.businessAddress || '',
            type: originalSale.type,
            notes: originalSale.notes || '',
            isDeleted: false,
            updatedAt: existingUpdatedAt // Preserved: not updated
          });
        } else {
          // Case B: Split match - sale item is larger than remaining edited quantity
          const splitQty = Math.round((saleItem.quantity - matchedQty) * 10000) / 10000;
          const itemSellPrice = saleItem.sellPrice;

          // Proportional financial calculations
          const matchedItemSubtotal = matchedQty * itemSellPrice;
          const splitItemSubtotal = splitQty * itemSellPrice;
          const saleSubtotalEstimate = originalSale.totalAmount + originalSale.discountAmount;

          const matchedDiscount = saleSubtotalEstimate > 0 ? (matchedItemSubtotal / saleSubtotalEstimate) * originalSale.discountAmount : 0;
          const splitDiscount = saleSubtotalEstimate > 0 ? (splitItemSubtotal / saleSubtotalEstimate) * originalSale.discountAmount : 0;

          const matchedTotalAmount = Math.max(0, matchedItemSubtotal - matchedDiscount);
          const splitTotalAmount = Math.max(0, splitItemSubtotal - splitDiscount);

          // 1. Update matched portion: quantity = matchedQty, buyPrice = newPrice, timestamp = now, keep updatedAt
          await upsertSaleItemDetail(dataConnect, {
            id: saleItem.id,
            storeId,
            saleId: saleItem.saleId,
            itemId: saleItem.itemId,
            itemName: saleItem.itemName,
            unit: saleItem.unit,
            quantity: matchedQty,
            sellPrice: saleItem.sellPrice,
            buyPrice: newPrice,
            taxRate: saleItem.taxRate,
            hsnCode: saleItem.hsnCode,
            isDeleted: false,
            updatedAt: existingItemUpdatedAt // Preserved: not updated
          });

          await syncSale(dataConnect, {
            id: saleItem.saleId,
            storeId,
            timestamp: now, // Update timestamp to new Date() for updated portion
            totalAmount: matchedTotalAmount,
            discountAmount: matchedDiscount,
            customerName: originalSale.customerName || '',
            customerGstin: originalSale.customerGstin || '',
            businessGstin: originalSale.businessGstin || '',
            customerAddress: originalSale.customerAddress || '',
            businessAddress: originalSale.businessAddress || '',
            type: originalSale.type,
            notes: originalSale.notes || '',
            isDeleted: false,
            updatedAt: existingUpdatedAt // Preserved: not updated
          });

          // 2. Create new split record for remainder: quantity = splitQty, buyPrice = original buyPrice, timestamp = old timestamp, updatedAt = old updatedAt
          const newSaleId = crypto.randomUUID();
          const newSaleItemDetailId = crypto.randomUUID();

          await syncSale(dataConnect, {
            id: newSaleId,
            storeId,
            timestamp: originalSale.timestamp, // Keep old timestamp for split remainder!
            totalAmount: splitTotalAmount,
            discountAmount: splitDiscount,
            customerName: originalSale.customerName || '',
            customerGstin: originalSale.customerGstin || '',
            businessGstin: originalSale.businessGstin || '',
            customerAddress: originalSale.customerAddress || '',
            businessAddress: originalSale.businessAddress || '',
            type: originalSale.type,
            notes: `${originalSale.notes || ''} (Split FIFO Part)`,
            isDeleted: false,
            updatedAt: existingUpdatedAt // Preserved: old updatedAt
          });

          await upsertSaleItemDetail(dataConnect, {
            id: newSaleItemDetailId,
            storeId,
            saleId: newSaleId,
            itemId: saleItem.itemId,
            itemName: saleItem.itemName,
            unit: saleItem.unit,
            quantity: splitQty,
            sellPrice: saleItem.sellPrice,
            buyPrice: saleItem.buyPrice, // Keep original buyPrice
            taxRate: saleItem.taxRate,
            hsnCode: saleItem.hsnCode,
            isDeleted: false,
            updatedAt: existingItemUpdatedAt // Preserved: old updatedAt
          });

          // 3. Proportional Udhaar entry split
          const udhaarResp: any = await getUdhaarBySaleId(dataConnect, { saleId: saleItem.saleId });
          const udhaarEntries = udhaarResp.data?.udhaarEntries || [];
          if (udhaarEntries.length > 0) {
            const originalTotalBeforeSplit = originalSale.totalAmount;
            for (const udhaar of udhaarEntries) {
              const udhaarRatioMatched = originalTotalBeforeSplit > 0 ? matchedTotalAmount / originalTotalBeforeSplit : 0;
              const udhaarRatioSplit = originalTotalBeforeSplit > 0 ? splitTotalAmount / originalTotalBeforeSplit : 0;
              const existingUdhaarUpdatedAt = udhaar.updatedAt || existingUpdatedAt;

              // Update matched udhaar: timestamp = now, keep updatedAt
              await syncUdhaar(dataConnect, {
                id: udhaar.id,
                storeId,
                customerName: udhaar.customerName,
                amount: udhaar.amount * udhaarRatioMatched,
                type: udhaar.type,
                timestamp: now,
                notes: udhaar.notes || '',
                saleId: udhaar.saleId || '',
                isDeleted: udhaar.isDeleted,
                updatedAt: existingUdhaarUpdatedAt
              });

              // Create split udhaar: timestamp = old timestamp, updatedAt = old updatedAt
              const newUdhaarId = crypto.randomUUID();
              await syncUdhaar(dataConnect, {
                id: newUdhaarId,
                storeId,
                customerName: udhaar.customerName,
                amount: udhaar.amount * udhaarRatioSplit,
                type: udhaar.type,
                timestamp: udhaar.timestamp,
                notes: `${udhaar.notes || ''} (Split FIFO Part)`,
                saleId: newSaleId,
                isDeleted: udhaar.isDeleted,
                updatedAt: existingUdhaarUpdatedAt
              });
            }
          }
        }
      }

      console.log(`FIFO purchase edit allocation complete for purchaseId: ${editedPurchaseId}`);
      return;
    }
  }

  // 4. Default full FIFO allocation across all batches if editedPurchaseId is not provided
  const batches: PurchaseBatch[] = purchases.map((p: any) => ({
    id: p.id,
    purchaseId: p.purchaseId,
    buyPrice: p.buyPrice,
    initialQty: p.quantity,
    remainingQty: p.quantity,
    timestamp: p.purchase?.timestamp || p.updatedAt || 0
  }));

  for (const saleItem of saleItems) {
    let remainingSaleQty = saleItem.quantity;
    const originalSale = saleItem.sale;
    if (!originalSale) continue;

    const existingUpdatedAt = originalSale.updatedAt || saleItem.updatedAt || originalSale.timestamp || 0;
    const existingItemUpdatedAt = saleItem.updatedAt || existingUpdatedAt;

    let isFirstMatch = true;

    while (remainingSaleQty > 0.0001) {
      const batchIndex = batches.findIndex(b => b.remainingQty > 0.0001);
      if (batchIndex === -1) break;

      const batch = batches[batchIndex];
      const matchedQty = Math.min(remainingSaleQty, batch.remainingQty);
      batch.remainingQty = Math.max(0, batch.remainingQty - matchedQty);

      const priceChanged = Math.abs(saleItem.buyPrice - batch.buyPrice) > 0.001;

      if (isFirstMatch) {
        if (Math.abs(matchedQty - saleItem.quantity) < 0.0001) {
          await upsertSaleItemDetail(dataConnect, {
            id: saleItem.id,
            storeId,
            saleId: saleItem.saleId,
            itemId: saleItem.itemId,
            itemName: saleItem.itemName,
            unit: saleItem.unit,
            quantity: saleItem.quantity,
            sellPrice: saleItem.sellPrice,
            buyPrice: batch.buyPrice,
            taxRate: saleItem.taxRate,
            hsnCode: saleItem.hsnCode,
            isDeleted: false,
            updatedAt: existingItemUpdatedAt
          });

          if (priceChanged) {
            await syncSale(dataConnect, {
              id: saleItem.saleId,
              storeId,
              timestamp: now,
              totalAmount: originalSale.totalAmount,
              discountAmount: originalSale.discountAmount,
              customerName: originalSale.customerName || '',
              customerGstin: originalSale.customerGstin || '',
              businessGstin: originalSale.businessGstin || '',
              customerAddress: originalSale.customerAddress || '',
              businessAddress: originalSale.businessAddress || '',
              type: originalSale.type,
              notes: originalSale.notes || '',
              isDeleted: false,
              updatedAt: existingUpdatedAt
            });
          }
        } else {
          const splitQty = Math.round((saleItem.quantity - matchedQty) * 10000) / 10000;
          const itemSellPrice = saleItem.sellPrice;

          const matchedItemSubtotal = matchedQty * itemSellPrice;
          const splitItemSubtotal = splitQty * itemSellPrice;
          const saleSubtotalEstimate = originalSale.totalAmount + originalSale.discountAmount;

          const matchedDiscount = saleSubtotalEstimate > 0 ? (matchedItemSubtotal / saleSubtotalEstimate) * originalSale.discountAmount : 0;
          const splitDiscount = saleSubtotalEstimate > 0 ? (splitItemSubtotal / saleSubtotalEstimate) * originalSale.discountAmount : 0;

          const matchedTotalAmount = Math.max(0, matchedItemSubtotal - matchedDiscount);
          const splitTotalAmount = Math.max(0, splitItemSubtotal - splitDiscount);

          await upsertSaleItemDetail(dataConnect, {
            id: saleItem.id,
            storeId,
            saleId: saleItem.saleId,
            itemId: saleItem.itemId,
            itemName: saleItem.itemName,
            unit: saleItem.unit,
            quantity: matchedQty,
            sellPrice: saleItem.sellPrice,
            buyPrice: batch.buyPrice,
            taxRate: saleItem.taxRate,
            hsnCode: saleItem.hsnCode,
            isDeleted: false,
            updatedAt: existingItemUpdatedAt
          });

          const newOriginalTotal = Math.max(0, originalSale.totalAmount - splitTotalAmount);
          const newOriginalDiscount = Math.max(0, originalSale.discountAmount - splitDiscount);

          await syncSale(dataConnect, {
            id: saleItem.saleId,
            storeId,
            timestamp: now,
            totalAmount: newOriginalTotal,
            discountAmount: newOriginalDiscount,
            customerName: originalSale.customerName || '',
            customerGstin: originalSale.customerGstin || '',
            businessGstin: originalSale.businessGstin || '',
            customerAddress: originalSale.customerAddress || '',
            businessAddress: originalSale.businessAddress || '',
            type: originalSale.type,
            notes: originalSale.notes || '',
            isDeleted: false,
            updatedAt: existingUpdatedAt
          });
        }
        isFirstMatch = false;
      } else {
        const newSaleId = crypto.randomUUID();
        const newSaleItemDetailId = crypto.randomUUID();

        const itemSellPrice = saleItem.sellPrice;
        const splitItemSubtotal = matchedQty * itemSellPrice;
        const saleSubtotalEstimate = originalSale.totalAmount + originalSale.discountAmount;
        const splitDiscount = saleSubtotalEstimate > 0 ? (splitItemSubtotal / saleSubtotalEstimate) * originalSale.discountAmount : 0;
        const splitTotalAmount = Math.max(0, splitItemSubtotal - splitDiscount);

        await syncSale(dataConnect, {
          id: newSaleId,
          storeId,
          timestamp: originalSale.timestamp,
          totalAmount: splitTotalAmount,
          discountAmount: splitDiscount,
          customerName: originalSale.customerName || '',
          customerGstin: originalSale.customerGstin || '',
          businessGstin: originalSale.businessGstin || '',
          customerAddress: originalSale.customerAddress || '',
          businessAddress: originalSale.businessAddress || '',
          type: originalSale.type,
          notes: `${originalSale.notes || ''} (Split FIFO Part)`,
          isDeleted: false,
          updatedAt: existingUpdatedAt
        });

        await upsertSaleItemDetail(dataConnect, {
          id: newSaleItemDetailId,
          storeId,
          saleId: newSaleId,
          itemId: saleItem.itemId,
          itemName: saleItem.itemName,
          unit: saleItem.unit,
          quantity: matchedQty,
          sellPrice: saleItem.sellPrice,
          buyPrice: batch.buyPrice,
          taxRate: saleItem.taxRate,
          hsnCode: saleItem.hsnCode,
          isDeleted: false,
          updatedAt: existingItemUpdatedAt
        });
      }

      remainingSaleQty = Math.max(0, remainingSaleQty - matchedQty);
    }
  }

  console.log(`FIFO recalculation complete for storeId: ${storeId}, itemId: ${itemId}`);
}
