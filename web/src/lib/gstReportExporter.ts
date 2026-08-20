import { BillingEngine } from '@/lib/BillingEngine';
import { downloadCsvFile } from '@/lib/csvUtils';

/**
 * Exports GSTR-1 Outward Supplies report matching government/CA Excel standard.
 */
export function exportGstr1Report(
  sales: any[],
  saleItems: any[],
  allItemsMap: Map<string, any>,
  businessGstin: string,
  businessName: string,
  monthName: string,
  year: number
) {
  const saleItemsMap = new Map<string, any[]>();
  for (const item of saleItems) {
    if (!saleItemsMap.has(item.saleId)) {
      saleItemsMap.set(item.saleId, []);
    }
    saleItemsMap.get(item.saleId)!.push(item);
  }

  // Metadata block
  const lines: string[] = [
    `"GSTR-1 Outward Supplies (Sales) Report"`,
    `"Business Name:",${escapeCsv(businessName)},"GSTIN:",${escapeCsv(businessGstin || 'Not Provided')}`,
    `"Reporting Period:",${escapeCsv(`${monthName} ${year}`)},"Generated On:",${escapeCsv(new Date().toLocaleDateString('en-IN'))}`,
    `"Total Invoices:",${sales.length}`,
    ``,
    // Table Headers
    [
      `"Date"`,
      `"Invoice No"`,
      `"Customer Name"`,
      `"Customer GSTIN"`,
      `"Taxable Value (₹)"`,
      `"CGST (₹)"`,
      `"SGST (₹)"`,
      `"IGST (₹)"`,
      `"Total Amount (₹)"`
    ].join(',')
  ];

  let sumTaxable = 0;
  let sumCgst = 0;
  let sumSgst = 0;
  let sumIgst = 0;
  let sumTotal = 0;

  for (const sale of sales) {
    const items = saleItemsMap.get(sale.id) || [];
    const invoiceItems = items.map((i) => {
      const itemMaster = allItemsMap?.get(i.itemId);
      return {
        id: i.id,
        sell_price: i.sellPrice,
        quantity: i.quantity,
        taxRate: itemMaster?.taxRate ?? i.taxRate ?? 0,
      };
    });

    const taxSummary = BillingEngine.calculateInvoiceTaxes(
      invoiceItems,
      sale.discountAmount || 0,
      businessGstin,
      sale.customerGstin
    );

    const invoiceNo = sale.invoiceNumber || `INV${sale.id.slice(0, 5).toUpperCase()}`;
    const rawTs = Number(sale.timestamp) || 0;
    const saleTs = rawTs > 10000000000 ? rawTs : rawTs * 1000;
    const dateStr = new Date(saleTs).toLocaleDateString('en-IN');
    const customerName = sale.customerName || 'Cash / Anonymous';
    const customerGstin = sale.customerGstin || '-';

    sumTaxable += taxSummary.netTaxableAmount;
    sumCgst += taxSummary.totalCgst;
    sumSgst += taxSummary.totalSgst;
    sumIgst += taxSummary.totalIgst;
    sumTotal += sale.totalAmount;

    lines.push(
      [
        escapeCsv(dateStr),
        escapeCsv(invoiceNo),
        escapeCsv(customerName),
        escapeCsv(customerGstin),
        taxSummary.netTaxableAmount.toFixed(2),
        taxSummary.totalCgst.toFixed(2),
        taxSummary.totalSgst.toFixed(2),
        taxSummary.totalIgst.toFixed(2),
        sale.totalAmount.toFixed(2)
      ].join(',')
    );
  }

  // Total Summary Row
  lines.push(``);
  lines.push(
    [
      `"TOTAL"`,
      `""`,
      `""`,
      `""`,
      sumTaxable.toFixed(2),
      sumCgst.toFixed(2),
      sumSgst.toFixed(2),
      sumIgst.toFixed(2),
      sumTotal.toFixed(2)
    ].join(',')
  );

  const csvContent = lines.join('\r\n');
  downloadCsvFile(csvContent, `GSTR1_${monthName}_${year}.csv`);
}

function escapeCsv(val: any): string {
  if (val === null || val === undefined) return '""';
  const str = String(val);
  return `"${str.replace(/"/g, '""')}"`;
}
