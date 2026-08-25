'use client';

import React, { useState, useMemo } from 'react';
import { Layers } from 'lucide-react';
import { FormattedAmount } from '@/components/FormattedAmount';
import { BillingEngine } from '@/lib/BillingEngine';

interface DetailedGstReportViewProps {
  sales: any[];
  saleItems: any[];
  purchases: any[];
  purchaseItems: any[];
  suppliersMap: Map<string, any>;
  allItemsMap: Map<string, any>;
  businessGstin: string;
  businessName: string;
  monthName: string;
  year: number;
}

interface DetailedRow {
  id: string;
  date: number;
  txnId: string;
  type: 'Sale' | 'Purchase';
  party: string;
  item: string;
  hsn: string;
  taxRate: number;
  qty: number;
  unit: string;
  taxable: number;
  tax: number;
  total: number;
}

export default function DetailedGstReportView({
  sales,
  saleItems,
  purchases,
  purchaseItems,
  suppliersMap,
  allItemsMap,
  businessGstin,
  monthName,
  year,
}: DetailedGstReportViewProps) {
  const [filterType, setFilterType] = useState<'ALL' | 'SALE' | 'PURCHASE'>('ALL');

  // 1. Group sale item details by saleId
  const saleItemsMap = useMemo(() => {
    const map = new Map<string, any[]>();
    for (const item of saleItems) {
      const sId = String(item.saleId ?? item.sale_id ?? '').trim();
      if (sId) {
        if (!map.has(sId)) map.set(sId, []);
        map.get(sId)!.push(item);
      }
    }
    return map;
  }, [saleItems]);

  // 2. Group purchase item details by purchaseId
  const purchaseItemsMap = useMemo(() => {
    const map = new Map<string, any[]>();
    for (const item of purchaseItems) {
      const pId = String(item.purchaseId ?? item.purchase_id ?? '').trim();
      if (pId) {
        if (!map.has(pId)) map.set(pId, []);
        map.get(pId)!.push(item);
      }
    }
    return map;
  }, [purchaseItems]);

  // 3. Build DetailedRow list matching Android GSTReportScreen
  const detailedRows = useMemo(() => {
    const rows: DetailedRow[] = [];

    // Sales item details
    for (const sale of sales) {
      const sId = String(sale.id ?? sale.sale_id ?? '').trim();
      const items = saleItemsMap.get(sId) || [];
      const rawTs = Number(sale.timestamp) || 0;
      const saleTs = rawTs < 100000000000 ? rawTs * 1000 : rawTs;
      const txnId = sale.invoiceNumber || `INV${sId.slice(0, 5).toUpperCase()}`;
      const party = sale.customerName || sale.customer_name || 'Cash Customer';
      const saleDiscount = Number(sale.discountAmount ?? sale.discount_amount) || 0;
      const customerGstin = sale.customerGstin || sale.customer_gstin || '';

      const invoiceItems = items.map((i) => {
        const itemMaster = allItemsMap?.get(String(i.itemId ?? i.item_id ?? '').trim());
        const sellPrice = Number(i.sellPrice ?? i.sell_price ?? itemMaster?.sellPrice ?? 0);
        return {
          id: String(i.id ?? ''),
          sell_price: sellPrice,
          quantity: Number(i.quantity ?? 1),
          taxRate: Number(itemMaster?.taxRate ?? i.taxRate ?? i.tax_rate ?? 0),
          unit: i.unit || itemMaster?.unit || 'Units',
          hsnCode: itemMaster?.hsnCode || i.hsnCode || i.hsn_code || '-',
          name: itemMaster?.name || i.itemName || i.item_name || 'Item',
        };
      });

      const taxSummary = BillingEngine.calculateInvoiceTaxes(
        invoiceItems,
        saleDiscount,
        businessGstin,
        customerGstin
      );

      if (invoiceItems.length === 0) {
        const saleTotal = Number(sale.totalAmount ?? sale.total_amount) || 0;
        const taxable = Math.max(0, saleTotal - saleDiscount);
        rows.push({
          id: `sale-${sId}-0`,
          date: saleTs,
          txnId,
          type: 'Sale',
          party,
          item: 'General Sale',
          hsn: '-',
          taxRate: 0,
          qty: 1,
          unit: 'Unit',
          taxable,
          tax: 0,
          total: saleTotal,
        });
      } else {
        taxSummary.itemDetails.forEach((detail, idx) => {
          const rawItem = invoiceItems[idx];
          rows.push({
            id: `sale-${sId}-${idx}`,
            date: saleTs,
            txnId,
            type: 'Sale',
            party,
            item: rawItem?.name || 'Item',
            hsn: rawItem?.hsnCode || '-',
            taxRate: rawItem?.taxRate || 0,
            qty: rawItem?.quantity || 1,
            unit: rawItem?.unit || 'Units',
            taxable: detail.netAmountBeforeTax,
            tax: detail.totalTaxAmount,
            total: detail.totalAmountWithTax,
          });
        });
      }
    }

    // Purchases item details
    for (const purchase of purchases) {
      const pId = String(purchase.id ?? purchase.purchase_id ?? '').trim();
      const items = purchaseItemsMap.get(pId) || [];
      const supplier = suppliersMap?.get(String(purchase.supplierId ?? purchase.supplier_id ?? '').trim());
      const supplierGstin = supplier?.gstin || '';
      const rawTs = Number(purchase.timestamp) || 0;
      const purchaseTs = rawTs < 100000000000 ? rawTs * 1000 : rawTs;
      const txnId = purchase.billNumber || `PUR${pId.slice(0, 5).toUpperCase()}`;
      const party = purchase.supplierName ?? purchase.supplier_name ?? supplier?.name ?? 'Supplier';

      const invoiceItems = items.map((pi) => {
        const itemMaster = allItemsMap?.get(String(pi.itemId ?? pi.item_id ?? '').trim());
        const buyPrice = Number(pi.buyPrice ?? pi.buy_price ?? itemMaster?.buyPrice ?? 0);
        return {
          id: String(pi.id ?? ''),
          sell_price: buyPrice,
          quantity: Number(pi.quantity ?? 1),
          taxRate: Number(itemMaster?.taxRate ?? pi.taxRate ?? pi.tax_rate ?? 0),
          unit: pi.unit || itemMaster?.unit || 'Units',
          hsnCode: itemMaster?.hsnCode || pi.hsnCode || pi.hsn_code || '-',
          name: itemMaster?.name || pi.itemName || pi.item_name || 'Item',
        };
      });

      const taxSummary = BillingEngine.calculateInvoiceTaxes(
        invoiceItems,
        0,
        businessGstin,
        supplierGstin
      );

      if (invoiceItems.length === 0) {
        const purchaseTotal = Number(purchase.totalAmount ?? purchase.total_amount) || 0;
        rows.push({
          id: `pur-${pId}-0`,
          date: purchaseTs,
          txnId,
          type: 'Purchase',
          party,
          item: 'General Purchase',
          hsn: '-',
          taxRate: 0,
          qty: 1,
          unit: 'Unit',
          taxable: purchaseTotal,
          tax: 0,
          total: purchaseTotal,
        });
      } else {
        taxSummary.itemDetails.forEach((detail, idx) => {
          const rawItem = invoiceItems[idx];
          rows.push({
            id: `pur-${pId}-${idx}`,
            date: purchaseTs,
            txnId,
            type: 'Purchase',
            party,
            item: rawItem?.name || 'Item',
            hsn: rawItem?.hsnCode || '-',
            taxRate: rawItem?.taxRate || 0,
            qty: rawItem?.quantity || 1,
            unit: rawItem?.unit || 'Units',
            taxable: detail.netAmountBeforeTax,
            tax: detail.totalTaxAmount,
            total: detail.totalAmountWithTax,
          });
        });
      }
    }

    return rows;
  }, [sales, saleItemsMap, purchases, purchaseItemsMap, suppliersMap, allItemsMap, businessGstin]);

  // 4. Totals for Summary Cards
  const totalTaxableValue = useMemo(() => {
    return detailedRows.reduce((acc, r) => acc + r.taxable, 0);
  }, [detailedRows]);

  const consolidatedGst = useMemo(() => {
    return detailedRows.reduce((acc, r) => acc + r.tax, 0);
  }, [detailedRows]);

  // 5. Filtered rows
  const filteredRows = useMemo(() => {
    if (filterType === 'SALE') return detailedRows.filter((r) => r.type === 'Sale');
    if (filterType === 'PURCHASE') return detailedRows.filter((r) => r.type === 'Purchase');
    return detailedRows;
  }, [detailedRows, filterType]);

  return (
    <div className="flex flex-col flex-1 min-h-0 space-y-4">
      {/* 1. Metric Summary Cards (Matching Android App Detailed GST Screen) */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-3.5 shrink-0">
        {/* Total Taxable Value */}
        <div className="glass-card p-4 border border-gray-100 dark:border-gray-800 bg-white dark:bg-gray-800 rounded-2xl shadow-sm text-center">
          <div className="text-xl font-black text-blue-900 dark:text-blue-400">
            <FormattedAmount amount={totalTaxableValue} />
          </div>
          <div className="text-xs font-semibold text-gray-500 dark:text-gray-400 mt-0.5">
            Total Taxable Value
          </div>
        </div>

        {/* Consolidated GST */}
        <div className="glass-card p-4 border border-gray-100 dark:border-gray-800 bg-white dark:bg-gray-800 rounded-2xl shadow-sm text-center">
          <div className="text-xl font-black text-emerald-600 dark:text-emerald-400">
            <FormattedAmount amount={consolidatedGst} />
          </div>
          <div className="text-xs font-semibold text-gray-500 dark:text-gray-400 mt-0.5">
            Consolidated GST
          </div>
        </div>
      </div>

      {/* 2. Header & Filter Chips */}
      <div className="flex items-center justify-between shrink-0">
        <h3 className="text-sm font-bold text-gray-900 dark:text-white">
          Detailed Transaction Breakdown ({filteredRows.length})
        </h3>
        <div className="flex items-center space-x-1.5 bg-gray-100 dark:bg-gray-700/60 p-1 rounded-xl">
          <button
            type="button"
            onClick={() => setFilterType('ALL')}
            className={`px-3 py-1 rounded-lg text-xs font-bold transition-all ${
              filterType === 'ALL'
                ? 'bg-white dark:bg-gray-800 text-gray-900 dark:text-white shadow-sm'
                : 'text-gray-500 dark:text-gray-400 hover:text-gray-700 dark:hover:text-gray-200'
            }`}
          >
            All ({detailedRows.length})
          </button>
          <button
            type="button"
            onClick={() => setFilterType('SALE')}
            className={`px-3 py-1 rounded-lg text-xs font-bold transition-all ${
              filterType === 'SALE'
                ? 'bg-white dark:bg-gray-800 text-indigo-600 dark:text-indigo-400 shadow-sm'
                : 'text-gray-500 dark:text-gray-400 hover:text-gray-700 dark:hover:text-gray-200'
            }`}
          >
            Sales ({detailedRows.filter((r) => r.type === 'Sale').length})
          </button>
          <button
            type="button"
            onClick={() => setFilterType('PURCHASE')}
            className={`px-3 py-1 rounded-lg text-xs font-bold transition-all ${
              filterType === 'PURCHASE'
                ? 'bg-white dark:bg-gray-800 text-amber-600 dark:text-amber-400 shadow-sm'
                : 'text-gray-500 dark:text-gray-400 hover:text-gray-700 dark:hover:text-gray-200'
            }`}
          >
            Purchases ({detailedRows.filter((r) => r.type === 'Purchase').length})
          </button>
        </div>
      </div>

      {/* 3. Detailed Transaction Card List */}
      {filteredRows.length === 0 ? (
        <div className="glass-card p-8 text-center border border-gray-100 dark:border-gray-800 bg-white dark:bg-gray-800 rounded-2xl">
          <div className="w-10 h-10 rounded-full bg-gray-100 dark:bg-gray-700 flex items-center justify-center mx-auto text-gray-400 mb-2">
            <Layers size={20} />
          </div>
          <p className="text-sm font-semibold text-gray-700 dark:text-gray-300">
            No transactions found for detailed breakup in {monthName} {year}
          </p>
          <p className="text-xs text-gray-500 dark:text-gray-400 mt-1">
            Select a different reporting period to view transaction rows.
          </p>
        </div>
      ) : (
        <div className="flex-1 min-h-0 overflow-y-auto pr-1.5 space-y-2.5 scrollbar-thin scrollbar-thumb-gray-200 dark:scrollbar-thumb-gray-700">
          {filteredRows.map((row) => {
            const dateStr = new Date(row.date).toLocaleDateString('en-IN', {
              day: '2-digit',
              month: '2-digit',
            });

            const isSale = row.type === 'Sale';

            return (
              <div
                key={row.id}
                className="p-3.5 border border-gray-100 dark:border-gray-800 bg-white dark:bg-gray-800 hover:bg-gray-50/70 dark:hover:bg-gray-700 rounded-2xl shadow-sm transition-all"
              >
                {/* Header line: Date, Txn ID, Type Chip, Total Amount */}
                <div className="flex items-center justify-between">
                  <div className="flex items-center space-x-2">
                    <span className="text-xs text-gray-400 dark:text-gray-500 font-medium">
                      {dateStr}
                    </span>
                    <span className="text-xs font-bold text-gray-900 dark:text-white font-mono">
                      {row.txnId}
                    </span>
                    <span
                      className={`px-2 py-0.5 rounded-md text-[10px] font-bold border ${
                        isSale
                          ? 'text-indigo-700 dark:text-indigo-300 bg-indigo-50 dark:bg-indigo-950/40 border-indigo-100 dark:border-indigo-900/50'
                          : 'text-amber-700 dark:text-amber-300 bg-amber-50 dark:bg-amber-950/40 border-amber-100 dark:border-amber-900/50'
                      }`}
                    >
                      {row.type}
                    </span>
                  </div>

                  <div className="text-sm font-black text-gray-900 dark:text-white">
                    <FormattedAmount amount={row.total} />
                  </div>
                </div>

                {/* Divider */}
                <div className="my-2 border-t border-gray-50 dark:border-gray-700/60" />

                {/* Body: Item Name, Party Name, Taxable, Tax Rate */}
                <div className="flex items-center justify-between text-xs">
                  <div className="space-y-0.5">
                    <div className="font-bold text-gray-800 dark:text-gray-200">
                      {row.item}
                      {row.qty > 1 && (
                        <span className="text-gray-400 font-normal ml-1">
                          (x{row.qty} {row.unit})
                        </span>
                      )}
                    </div>
                    <div className="text-[11px] text-gray-500 dark:text-gray-400">
                      Party: <span className="font-medium">{row.party}</span>
                      {row.hsn !== '-' && <span className="ml-2 font-mono">HSN: {row.hsn}</span>}
                    </div>
                  </div>

                  <div className="text-right space-y-0.5">
                    <div className="text-gray-500 dark:text-gray-400 font-medium">
                      Taxable: <FormattedAmount amount={row.taxable} />
                    </div>
                    <div className="text-emerald-600 dark:text-emerald-400 font-bold">
                      Tax: <FormattedAmount amount={row.tax} />{' '}
                      <span className="text-[11px] font-semibold text-gray-400">
                        ({row.taxRate}%)
                      </span>
                    </div>
                  </div>
                </div>
              </div>
            );
          })}
        </div>
      )}
    </div>
  );
}
