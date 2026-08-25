'use client';

import React from 'react';
import { ArrowDownRight, ArrowUpRight, TrendingUp, TrendingDown } from 'lucide-react';
import { FormattedAmount } from '@/components/FormattedAmount';
import { BillingEngine } from '@/lib/BillingEngine';

interface Gstr3BReportViewProps {
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

export default function Gstr3BReportView({
  sales,
  saleItems,
  purchases,
  purchaseItems,
  suppliersMap,
  allItemsMap,
  businessGstin,
}: Gstr3BReportViewProps) {
  // 1. Group sale item details by saleId
  const saleItemsMap = React.useMemo(() => {
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
  const purchaseItemsMap = React.useMemo(() => {
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

  // 3. Compute Outward (Sales) Tax Totals matching Android
  const salesTotals = React.useMemo(() => {
    let taxable = 0;
    let cgst = 0;
    let sgst = 0;
    let igst = 0;

    for (const sale of sales) {
      const sId = String(sale.id ?? sale.sale_id ?? '').trim();
      const items = saleItemsMap.get(sId) || [];
      const invoiceItems = items.map((i) => {
        const itemMaster = allItemsMap?.get(String(i.itemId ?? i.item_id ?? '').trim());
        const sellPrice = Number(i.sellPrice ?? i.sell_price ?? itemMaster?.sellPrice ?? 0);
        return {
          id: String(i.id ?? ''),
          sell_price: sellPrice,
          quantity: Number(i.quantity ?? 1),
          taxRate: Number(itemMaster?.taxRate ?? i.taxRate ?? i.tax_rate ?? 0),
        };
      });

      const saleTotal = Number(sale.totalAmount ?? sale.total_amount) || 0;
      const saleDiscount = Number(sale.discountAmount ?? sale.discount_amount) || 0;

      let taxSummary = BillingEngine.calculateInvoiceTaxes(
        invoiceItems,
        saleDiscount,
        businessGstin,
        sale.customerGstin || sale.customer_gstin
      );

      if (invoiceItems.length === 0 || (taxSummary.netTaxableAmount <= 0 && saleTotal > 0)) {
        const fallbackTaxable = Math.max(0, saleTotal - saleDiscount);
        taxSummary = {
          ...taxSummary,
          subTotal: saleTotal,
          totalDiscount: saleDiscount,
          netTaxableAmount: fallbackTaxable,
        };
      }

      taxable += taxSummary.netTaxableAmount;
      cgst += taxSummary.totalCgst;
      sgst += taxSummary.totalSgst;
      igst += taxSummary.totalIgst;
    }

    const totalTax = cgst + sgst + igst;
    return { taxable, cgst, sgst, igst, totalTax };
  }, [sales, saleItemsMap, allItemsMap, businessGstin]);

  // 4. Compute Inward (Purchases) Tax Totals matching Android
  const purchasesTotals = React.useMemo(() => {
    let taxable = 0;
    let cgst = 0;
    let sgst = 0;
    let igst = 0;

    for (const purchase of purchases) {
      const pId = String(purchase.id ?? purchase.purchase_id ?? '').trim();
      const items = purchaseItemsMap.get(pId) || [];
      const supplier = suppliersMap?.get(String(purchase.supplierId ?? purchase.supplier_id ?? '').trim());
      const supplierGstin = supplier?.gstin || '';

      const invoiceItems = items.map((pi) => {
        const itemMaster = allItemsMap?.get(String(pi.itemId ?? pi.item_id ?? '').trim());
        const buyPrice = Number(pi.buyPrice ?? pi.buy_price ?? itemMaster?.buyPrice ?? 0);
        return {
          id: String(pi.id ?? ''),
          sell_price: buyPrice,
          quantity: Number(pi.quantity ?? 1),
          taxRate: Number(itemMaster?.taxRate ?? pi.taxRate ?? pi.tax_rate ?? 0),
        };
      });

      const purchaseTotal = Number(purchase.totalAmount ?? purchase.total_amount) || 0;

      let taxSummary = BillingEngine.calculateInvoiceTaxes(
        invoiceItems,
        0,
        businessGstin,
        supplierGstin
      );

      if (invoiceItems.length === 0 || (taxSummary.netTaxableAmount <= 0 && purchaseTotal > 0)) {
        taxSummary = {
          ...taxSummary,
          subTotal: purchaseTotal,
          totalDiscount: 0,
          netTaxableAmount: purchaseTotal,
        };
      }

      taxable += taxSummary.netTaxableAmount;
      cgst += taxSummary.totalCgst;
      sgst += taxSummary.totalSgst;
      igst += taxSummary.totalIgst;
    }

    const totalTax = cgst + sgst + igst;
    return { taxable, cgst, sgst, igst, totalTax };
  }, [purchases, purchaseItemsMap, suppliersMap, allItemsMap, businessGstin]);

  // 5. Consolidated Net Calculations
  const netPayable = salesTotals.totalTax - purchasesTotals.totalTax;
  const isNetPayablePositive = netPayable >= 0;

  return (
    <div className="flex flex-col flex-1 min-h-0 space-y-4 overflow-y-auto pr-1 pb-4 scrollbar-thin scrollbar-thumb-gray-200 dark:scrollbar-thumb-gray-700">
      {/* 1. Top Metric Cards: Outward Tax Liability & Eligible ITC */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-3.5 shrink-0">
        {/* Outward Tax Liability */}
        <div className="glass-card p-4 border border-red-100 dark:border-red-950/40 bg-white dark:bg-gray-800 rounded-2xl shadow-sm flex items-center justify-between">
          <div>
            <div className="text-xs font-semibold text-gray-500 dark:text-gray-400">
              Outward Tax Liability
            </div>
            <div className="text-xl font-black text-rose-600 dark:text-rose-400 mt-0.5">
              <FormattedAmount amount={salesTotals.totalTax} />
            </div>
          </div>
          <div className="w-10 h-10 rounded-xl bg-rose-50 dark:bg-rose-950/50 text-rose-600 dark:text-rose-400 flex items-center justify-center">
            <ArrowUpRight size={20} />
          </div>
        </div>

        {/* Eligible ITC */}
        <div className="glass-card p-4 border border-emerald-100 dark:border-emerald-950/40 bg-white dark:bg-gray-800 rounded-2xl shadow-sm flex items-center justify-between">
          <div>
            <div className="text-xs font-semibold text-gray-500 dark:text-gray-400">
              Eligible ITC (Purchases)
            </div>
            <div className="text-xl font-black text-emerald-600 dark:text-emerald-400 mt-0.5">
              <FormattedAmount amount={purchasesTotals.totalTax} />
            </div>
          </div>
          <div className="w-10 h-10 rounded-xl bg-emerald-50 dark:bg-emerald-950/50 text-emerald-600 dark:text-emerald-400 flex items-center justify-center">
            <ArrowDownRight size={20} />
          </div>
        </div>
      </div>

      {/* 2. Consolidated Net Banner */}
      <div
        className={`p-5 rounded-2xl border transition-all ${isNetPayablePositive
          ? 'bg-rose-50/70 dark:bg-rose-950/30 border-rose-200 dark:border-rose-900/60'
          : 'bg-emerald-50/70 dark:bg-emerald-950/30 border-emerald-200 dark:border-emerald-900/60'
          }`}
      >
        <div className="flex items-center justify-between">
          <div className="space-y-0.5">
            <div className="flex items-center space-x-2">
              {isNetPayablePositive ? (
                <TrendingUp className="text-rose-600 dark:text-rose-400" size={18} />
              ) : (
                <TrendingDown className="text-emerald-600 dark:text-emerald-400" size={18} />
              )}
              <span
                className={`text-sm font-black uppercase tracking-wider ${isNetPayablePositive
                  ? 'text-rose-700 dark:text-rose-300'
                  : 'text-emerald-700 dark:text-emerald-300'
                  }`}
              >
                {isNetPayablePositive ? 'Net Tax Payable' : 'ITC Carry Forward'}
              </span>
            </div>
            <p className="text-xs text-gray-500 dark:text-gray-400">
              {isNetPayablePositive
                ? 'Consolidated GSTR-3B balance to be paid'
                : 'Consolidated GSTR-3B surplus credit to carry forward'}
            </p>
          </div>
          <div
            className={`text-2xl font-black ${isNetPayablePositive
              ? 'text-rose-700 dark:text-rose-300'
              : 'text-emerald-700 dark:text-emerald-300'
              }`}
          >
            <FormattedAmount amount={Math.abs(netPayable)} />
          </div>
        </div>
      </div>

      {/* 3. Sectional Breakdown Cards (Matching Android App GSTR-3B) */}
      <div className="space-y-3.5">
        {/* Section 1: Outward Supplies */}
        <SectionBreakdownCard
          title="1. Outward Supplies (Sales Liability)"
          taxable={salesTotals.taxable}
          cgst={salesTotals.cgst}
          sgst={salesTotals.sgst}
          igst={salesTotals.igst}
          totalTax={salesTotals.totalTax}
        />
        <br />
        {/* Section 2: Inward Supplies */}
        <SectionBreakdownCard
          title="2. Inward Supplies (Eligible ITC)"
          taxable={purchasesTotals.taxable}
          cgst={purchasesTotals.cgst}
          sgst={purchasesTotals.sgst}
          igst={purchasesTotals.igst}
          totalTax={purchasesTotals.totalTax}
        />
        <br />
        {/* Section 3: Net Tax Summary */}
        <SectionBreakdownCard
          title="3. Net Tax Summary"
          taxable={salesTotals.taxable - purchasesTotals.taxable}
          cgst={salesTotals.cgst - purchasesTotals.cgst}
          sgst={salesTotals.sgst - purchasesTotals.sgst}
          igst={salesTotals.igst - purchasesTotals.igst}
          totalTax={salesTotals.totalTax - purchasesTotals.totalTax}
        />
      </div>
    </div>
  );
}

interface SectionBreakdownCardProps {
  title: string;
  taxable: number;
  cgst: number;
  sgst: number;
  igst: number;
  totalTax: number;
}

function SectionBreakdownCard({
  title,
  taxable,
  cgst,
  sgst,
  igst,
  totalTax,
}: SectionBreakdownCardProps) {

  return (
    <div className="glass-card p-5 border border-gray-100 dark:border-gray-800 bg-white dark:bg-gray-800 rounded-2xl shadow-sm">
      <div className="flex items-center justify-between pb-3 border-b border-gray-100 dark:border-gray-700/60">
        <h4 className="text-sm font-bold text-gray-900 dark:text-white">
          {title}
        </h4>
      </div>

      <div className="divide-y divide-gray-50 dark:divide-gray-700/60 text-xs">
        <BreakdownRow label='Taxable Value' amount={taxable} />
        <BreakdownRow label='CGST Amount' amount={cgst} />
        <BreakdownRow label='SGST Amount' amount={sgst} />
        <BreakdownRow label='IGST Amount' amount={igst} />
        <BreakdownRow
          label='Total Tax Component'
          amount={totalTax}
          isBold
          highlight
        />
      </div>
    </div>
  );
}

function BreakdownRow({
  label,
  amount,
  isBold = false,
  highlight = false,
}: {
  label: string;
  amount: number;
  isBold?: boolean;
  highlight?: boolean;
}) {
  return (
    <div className={`py-2.5 flex items-center justify-between ${highlight ? 'font-black pt-3' : ''}`}>
      <span className={isBold ? 'font-bold text-gray-800 dark:text-gray-200' : 'text-gray-500 dark:text-gray-400'}>
        {label}
      </span>
      <span className={isBold ? 'font-black text-gray-900 dark:text-white text-sm' : 'font-semibold text-gray-700 dark:text-gray-300'}>
        <FormattedAmount amount={amount} />
      </span>
    </div>
  );
}
