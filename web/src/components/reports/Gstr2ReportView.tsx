'use client';

import React from 'react';
import { Truck } from 'lucide-react';
import { FormattedAmount } from '@/components/FormattedAmount';
import { BillingEngine } from '@/lib/BillingEngine';

interface Gstr2ReportViewProps {
  purchases: any[];
  purchaseItems: any[];
  suppliersMap: Map<string, any>;
  allItemsMap: Map<string, any>;
  businessGstin: string;
  businessName: string;
  monthName: string;
  year: number;
}

export default function Gstr2ReportView({
  purchases,
  purchaseItems,
  suppliersMap,
  allItemsMap,
  businessGstin,
  monthName,
  year,
}: Gstr2ReportViewProps) {
  // 1. Group purchase item details by purchaseId (using String key for compatibility)
  const purchaseItemsMap = React.useMemo(() => {
    const map = new Map<string, any[]>();
    for (const item of purchaseItems) {
      const pId = String(item.purchaseId ?? item.purchase_id ?? '').trim();
      if (pId) {
        if (!map.has(pId)) {
          map.set(pId, []);
        }
        map.get(pId)!.push(item);
      }
    }
    return map;
  }, [purchaseItems]);

  // 2. Compute individual purchase bill tax summaries via BillingEngine (matching Android GSTReportScreen)
  const purchasesWithTaxes = React.useMemo(() => {
    return purchases.map((purchase) => {
      const pId = String(purchase.id ?? purchase.purchase_id ?? '').trim();
      const items = purchaseItemsMap.get(pId) || [];
      const supplier = suppliersMap?.get(String(purchase.supplierId ?? purchase.supplier_id ?? '').trim());
      const supplierGstin = supplier?.gstin || '';

      const invoiceItems = items.map((pi) => {
        const itemMaster = allItemsMap?.get(String(pi.itemId ?? pi.item_id ?? '').trim());
        const buyPrice = Number(pi.buyPrice ?? pi.buy_price ?? itemMaster?.buyPrice ?? 0);
        return {
          id: String(pi.id ?? ''),
          sell_price: buyPrice, // Inward supplies: buy price acts as base calculation price
          quantity: Number(pi.quantity ?? 1),
          taxRate: Number(itemMaster?.taxRate ?? pi.taxRate ?? pi.tax_rate ?? 0),
        };
      });

      const purchaseTotal = Number(purchase.totalAmount ?? purchase.total_amount) || 0;

      let taxSummary = BillingEngine.calculateInvoiceTaxes(
        invoiceItems,
        0, // No discount on purchases
        businessGstin,
        supplierGstin
      );

      // Fallback matching Android: if line items aren't present or yielded zero taxable for a positive purchase
      if (invoiceItems.length === 0 || (taxSummary.netTaxableAmount <= 0 && purchaseTotal > 0)) {
        taxSummary = {
          ...taxSummary,
          subTotal: purchaseTotal,
          totalDiscount: 0,
          netTaxableAmount: purchaseTotal,
        };
      }

      return {
        purchase,
        supplier,
        supplierGstin,
        taxSummary,
      };
    });
  }, [purchases, purchaseItemsMap, suppliersMap, allItemsMap, businessGstin]);

  // 3. Aggregate totals matching Android sumOfBigDecimal
  const totalPurchases = React.useMemo(() => {
    return purchases.reduce((acc, p) => acc + (Number(p.totalAmount ?? p.total_amount) || 0), 0);
  }, [purchases]);

  const totalTaxable = React.useMemo(() => {
    return purchasesWithTaxes.reduce((acc, p) => acc + (p.taxSummary.netTaxableAmount || 0), 0);
  }, [purchasesWithTaxes]);

  const totalCgst = React.useMemo(() => {
    return purchasesWithTaxes.reduce((acc, p) => acc + (p.taxSummary.totalCgst || 0), 0);
  }, [purchasesWithTaxes]);

  const totalSgst = React.useMemo(() => {
    return purchasesWithTaxes.reduce((acc, p) => acc + (p.taxSummary.totalSgst || 0), 0);
  }, [purchasesWithTaxes]);

  const totalIgst = React.useMemo(() => {
    return purchasesWithTaxes.reduce((acc, p) => acc + (p.taxSummary.totalIgst || 0), 0);
  }, [purchasesWithTaxes]);

  const totalTaxLiability = totalCgst + totalSgst + totalIgst;

  return (
    <div className="flex flex-col flex-1 min-h-0 space-y-4">
      {/* 1. Metric Summary Cards (Matching Android App GSTR-2 Screen) */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-3.5 shrink-0">
        {/* Total Purchases */}
        <div className="glass-card p-4 border border-gray-100 dark:border-gray-800 bg-white dark:bg-gray-800 rounded-2xl shadow-sm text-center">
          <div className="text-xl font-black text-blue-900 dark:text-blue-400">
            <FormattedAmount amount={totalPurchases} />
          </div>
          <div className="text-xs font-semibold text-gray-500 dark:text-gray-400 mt-0.5">
            Total Purchases
          </div>
        </div>

        {/* Taxable Value */}
        <div className="glass-card p-4 border border-gray-100 dark:border-gray-800 bg-white dark:bg-gray-800 rounded-2xl shadow-sm text-center">
          <div className="text-xl font-black text-amber-600 dark:text-amber-400">
            <FormattedAmount amount={totalTaxable} />
          </div>
          <div className="text-xs font-semibold text-gray-500 dark:text-gray-400 mt-0.5">
            Taxable Value
          </div>
        </div>

        {/* Total Eligible ITC (Tax Amount) */}
        <div className="glass-card p-4 border border-gray-100 dark:border-gray-800 bg-white dark:bg-gray-800 rounded-2xl shadow-sm text-center">
          <div className="text-xl font-black text-emerald-600 dark:text-emerald-400">
            <FormattedAmount amount={totalTaxLiability} />
          </div>
          <div className="text-xs font-semibold text-gray-500 dark:text-gray-400 mt-0.5">
            Total Eligible ITC (Tax)
          </div>
        </div>
      </div>

      {/* 2. Sub-Tax Breakup Pill Card (CGST | SGST | IGST) */}
      <div className="glass-card px-5 py-3 border border-gray-100 dark:border-gray-800 bg-white dark:bg-gray-800 rounded-2xl shadow-sm shrink-0">
        <div className="grid grid-cols-3 divide-x divide-gray-100 dark:divide-gray-700 text-center">
          <div className="px-2">
            <div className="text-[10px] font-semibold text-gray-400 uppercase tracking-wider">
              CGST
            </div>
            <div className="text-sm font-bold text-gray-800 dark:text-gray-200 mt-0.5">
              <FormattedAmount amount={totalCgst} />
            </div>
          </div>
          <div className="px-2">
            <div className="text-[10px] font-semibold text-gray-400 uppercase tracking-wider">
              SGST
            </div>
            <div className="text-sm font-bold text-gray-800 dark:text-gray-200 mt-0.5">
              <FormattedAmount amount={totalSgst} />
            </div>
          </div>
          <div className="px-2">
            <div className="text-[10px] font-semibold text-gray-400 uppercase tracking-wider">
              IGST
            </div>
            <div className="text-sm font-bold text-gray-800 dark:text-gray-200 mt-0.5">
              <FormattedAmount amount={totalIgst} />
            </div>
          </div>
        </div>
      </div>

      {/* 3. Purchase Bills Section */}
      <div className="flex flex-col flex-1 min-h-0 space-y-2">
        <div className="flex items-center justify-between shrink-0">
          <h3 className="text-base font-bold text-gray-900 dark:text-white">
            Purchase Bills ({purchases.length})
          </h3>
        </div>

        {purchases.length === 0 ? (
          <div className="glass-card p-8 text-center border border-gray-100 dark:border-gray-800 bg-white dark:bg-gray-800 rounded-2xl">
            <div className="w-10 h-10 rounded-full bg-gray-100 dark:bg-gray-700 flex items-center justify-center mx-auto text-gray-400 mb-2">
              <Truck size={20} />
            </div>
            <p className="text-sm font-semibold text-gray-700 dark:text-gray-300">
              No purchases recorded in {monthName} {year}
            </p>
            <p className="text-xs text-gray-500 dark:text-gray-400 mt-1">
              Select a different reporting period to view purchase records.
            </p>
          </div>
        ) : (
          <div className="flex-1 min-h-0 overflow-y-auto pr-1.5 space-y-2.5 scrollbar-thin scrollbar-thumb-gray-200 dark:scrollbar-thumb-gray-700">
            {purchasesWithTaxes.map(({ purchase, supplier, supplierGstin, taxSummary }) => {
              const billNo =
                purchase.billNumber || `PUR${String(purchase.id ?? '').slice(0, 5).toUpperCase()}`;
              const rawTs = Number(purchase.timestamp) || 0;
              const purchaseTs = rawTs < 100000000000 ? rawTs * 1000 : rawTs;
              const dateStr = new Date(purchaseTs).toLocaleDateString(
                'en-IN',
                {
                  day: '2-digit',
                  month: 'short',
                  year: 'numeric',
                }
              );
              const supplierName =
                purchase.supplierName ?? purchase.supplier_name ?? supplier?.name ?? 'Supplier';
              const totalTax =
                taxSummary.totalCgst +
                taxSummary.totalSgst +
                taxSummary.totalIgst;

              return (
                <div
                  key={purchase.id}
                  className="p-4 border border-gray-100 dark:border-gray-800 bg-white dark:bg-gray-800 hover:bg-gray-50/70 dark:hover:bg-gray-700 rounded-2xl shadow-sm transition-all"
                >
                  <div className="flex items-center justify-between">
                    {/* Left details */}
                    <div className="flex items-center space-x-3">
                      <span className="px-2.5 py-1 text-xs font-bold text-amber-700 dark:text-amber-300 bg-amber-50 dark:bg-amber-950/40 rounded-lg border border-amber-100 dark:border-amber-900/50">
                        {billNo}
                      </span>
                      <span className="text-xs text-gray-500 dark:text-gray-400 font-medium">
                        {dateStr}
                      </span>
                    </div>

                    {/* Right Total Amount */}
                    <div className="text-base font-black text-gray-900 dark:text-white">
                      <FormattedAmount amount={purchase.totalAmount} />
                    </div>
                  </div>

                  <div className="flex items-center justify-between mt-2 pt-2 border-t border-gray-50 dark:border-gray-700/60">
                    <div className="flex items-center space-x-2">
                      <span className="text-xs font-medium text-gray-600 dark:text-gray-300">
                        {supplierName}
                      </span>
                      {supplierGstin && (
                        <span className="text-[11px] text-gray-400 dark:text-gray-500 font-mono">
                          ({supplierGstin})
                        </span>
                      )}
                    </div>
                    <div className="text-xs font-semibold text-emerald-600 dark:text-emerald-400">
                      Tax: <FormattedAmount amount={totalTax} />
                    </div>
                  </div>
                </div>
              );
            })}
          </div>
        )}
      </div>
    </div>
  );
}
