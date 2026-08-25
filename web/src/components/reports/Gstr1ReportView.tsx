'use client';

import React from 'react';
import { Download, ShoppingBag } from 'lucide-react';
import { FormattedAmount } from '@/components/FormattedAmount';
import { exportGstr1Report } from '@/lib/gstReportExporter';
import { BillingEngine } from '@/lib/BillingEngine';
import { DcSale, DcSaleItem, DcItem } from '@/types/dataconnect';

interface InvoiceTaxItem {
  id: string;
  sell_price: number;
  quantity: number;
  taxRate?: number;
}

interface Gstr1ReportViewProps {
  sales: DcSale[];
  saleItems: DcSaleItem[];
  allItemsMap: Map<string, DcItem>;
  businessGstin: string;
  businessName: string;
  monthName: string;
  year: number;
}

export default function Gstr1ReportView({
  sales,
  saleItems,
  allItemsMap,
  businessGstin,
  businessName,
  monthName,
  year,
}: Gstr1ReportViewProps) {
  // 1. Group sale item details by saleId
  const saleItemsMap = React.useMemo(() => {
    const map = new Map<string, DcSaleItem[]>();
    for (const item of saleItems) {
      if (!map.has(item.saleId)) {
        map.set(item.saleId, []);
      }
      map.get(item.saleId)!.push(item);
    }
    return map;
  }, [saleItems]);

  // 2. Compute individual invoice tax summaries via BillingEngine (matching Android GSTReportScreen)
  const salesWithTaxes = React.useMemo(() => {
    return sales.map((sale) => {
      const items = saleItemsMap.get(sale.id) || [];
      const invoiceItems: InvoiceTaxItem[] = items.map((i) => ({
        id: i.id,
        sell_price: i.sellPrice,
        quantity: i.quantity,
        taxRate: 0,
      }));

      const taxSummary = BillingEngine.calculateInvoiceTaxes(
        invoiceItems,
        sale.discountAmount || 0,
        businessGstin,
        sale.customerGstin
      );

      return {
        sale,
        taxSummary,
      };
    });
  }, [sales, saleItemsMap, businessGstin]);

  // 3. Aggregate totals
  const totalSales = React.useMemo(() => {
    return sales.reduce((acc, s) => acc + (s.totalAmount || 0), 0);
  }, [sales]);

  const totalTaxable = React.useMemo(() => {
    return salesWithTaxes.reduce((acc, s) => acc + s.taxSummary.netTaxableAmount, 0);
  }, [salesWithTaxes]);

  const totalCgst = React.useMemo(() => {
    return salesWithTaxes.reduce((acc, s) => acc + s.taxSummary.totalCgst, 0);
  }, [salesWithTaxes]);

  const totalSgst = React.useMemo(() => {
    return salesWithTaxes.reduce((acc, s) => acc + s.taxSummary.totalSgst, 0);
  }, [salesWithTaxes]);

  const totalIgst = React.useMemo(() => {
    return salesWithTaxes.reduce((acc, s) => acc + s.taxSummary.totalIgst, 0);
  }, [salesWithTaxes]);

  const totalTaxLiability = totalCgst + totalSgst + totalIgst;

  const handleExport = () => {
    exportGstr1Report(
      sales,
      saleItems,
      allItemsMap,
      businessGstin,
      businessName,
      monthName,
      year
    );
  };

  return (
    <div className="flex flex-col flex-1 min-h-0 space-y-4">
      {/* 1. Metric Summary Cards (Matching Android App GSTR-1 Screen) */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-3.5 shrink-0">
        {/* Total Sales */}
        <div className="glass-card p-4 border border-gray-100 dark:border-gray-800 bg-white dark:bg-gray-800 rounded-2xl shadow-sm text-center">
          <div className="text-xl font-black text-blue-900 dark:text-blue-400">
            <FormattedAmount amount={totalSales} />
          </div>
          <div className="text-xs font-semibold text-gray-500 dark:text-gray-400 mt-0.5">
            Total Sales
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

        {/* Total Tax Liability */}
        <div className="glass-card p-4 border border-gray-100 dark:border-gray-800 bg-white dark:bg-gray-800 rounded-2xl shadow-sm text-center">
          <div className="text-xl font-black text-emerald-600 dark:text-emerald-400">
            <FormattedAmount amount={totalTaxLiability} />
          </div>
          <div className="text-xs font-semibold text-gray-500 dark:text-gray-400 mt-0.5">
            Total Tax Liability
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

      {/* 3. Sales Invoices Section */}
      <div className="flex flex-col flex-1 min-h-0 space-y-2">
        <div className="flex items-center justify-between shrink-0">
          <h3 className="text-base font-bold text-gray-900 dark:text-white">
            Sales Invoices ({sales.length})
          </h3>
        </div>

        {sales.length === 0 ? (
          <div className="glass-card p-8 text-center border border-gray-100 dark:border-gray-800 bg-white dark:bg-gray-800 rounded-2xl">
            <div className="w-10 h-10 rounded-full bg-gray-100 dark:bg-gray-700 flex items-center justify-center mx-auto text-gray-400 mb-2">
              <ShoppingBag size={20} />
            </div>
            <p className="text-sm font-semibold text-gray-700 dark:text-gray-300">
              No sales recorded in {monthName} {year}
            </p>
            <p className="text-xs text-gray-500 dark:text-gray-400 mt-1">
              Select a different reporting period to view invoices.
            </p>
          </div>
        ) : (
          <div className="flex-1 min-h-0 overflow-y-auto pr-1.5 space-y-2.5 scrollbar-thin scrollbar-thumb-gray-200 dark:scrollbar-thumb-gray-700">
            {salesWithTaxes.map(({ sale, taxSummary }) => {
              const invoiceNo =
                sale.invoiceNumber || `INV${sale.id.slice(0, 5).toUpperCase()}`;
              const rawTs = Number(sale.timestamp) || 0;
              const saleTs = rawTs > 10000000000 ? rawTs : rawTs * 1000;
              const dateStr = new Date(saleTs).toLocaleDateString(
                'en-IN',
                {
                  day: '2-digit',
                  month: 'short',
                  year: 'numeric',
                }
              );
              const partyName = sale.customerName || 'Cash / Anonymous';
              const totalTax =
                taxSummary.totalCgst +
                taxSummary.totalSgst +
                taxSummary.totalIgst;

              return (
                <div
                  key={sale.id}
                  className="p-4 border border-gray-100 dark:border-gray-800 bg-white dark:bg-gray-800 hover:bg-gray-50/70 dark:hover:bg-gray-700 rounded-2xl shadow-sm transition-all"
                >
                  <div className="flex items-center justify-between">
                    {/* Left details */}
                    <div className="flex items-center space-x-3">
                      <span className="px-2.5 py-1 text-xs font-bold text-indigo-700 dark:text-indigo-300 bg-indigo-50 dark:bg-indigo-950/40 rounded-lg border border-indigo-100 dark:border-indigo-900/50">
                        {invoiceNo}
                      </span>
                      <span className="text-xs text-gray-500 dark:text-gray-400 font-medium">
                        {dateStr}
                      </span>
                    </div>

                    {/* Right Total Amount */}
                    <div className="text-base font-black text-gray-900 dark:text-white">
                      <FormattedAmount amount={sale.totalAmount} />
                    </div>
                  </div>

                  <div className="flex items-center justify-between mt-2 pt-2 border-t border-gray-50 dark:border-gray-700/60">
                    <div className="text-xs font-medium text-gray-600 dark:text-gray-300">
                      {partyName}
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
