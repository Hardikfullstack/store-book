import jsPDF from 'jspdf';
import { BillingEngine } from './BillingEngine';

export interface PdfSaleData {
  id: string;
  timestamp: number;
  totalAmount: number;
  discountAmount: number;
  customerName?: string | null;
  customerGstin?: string | null;
  businessGstin?: string | null;
  customerAddress?: string | null;
  businessAddress?: string | null;
  businessName?: string | null;
  type: string;
}

export interface PdfCartItem {
  item: {
    id: string;
    name: string;
    unit: string;
    sellPrice: number;
    buyPrice: number;
    taxRate?: number;
    hsnCode?: string;
  };
  quantity: number;
}

export function formatInvoiceDate(timestamp: number): string {
  if (!timestamp) return '-';
  const d = new Date(timestamp);
  const day = String(d.getDate()).padStart(2, '0');
  const months = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];
  const month = months[d.getMonth()];
  const year = d.getFullYear();
  let hours = d.getHours();
  const minutes = String(d.getMinutes()).padStart(2, '0');
  const ampm = hours >= 12 ? 'PM' : 'AM';
  hours = hours % 12;
  hours = hours ? hours : 12;
  const hourStr = String(hours).padStart(2, '0');
  return `${day} ${month} ${year}, ${hourStr}:${minutes} ${ampm}`;
}

export class InvoicePdfGenerator {
  static generateInvoicePdf(
    sale: PdfSaleData,
    cartItems: PdfCartItem[],
    shopName: string,
    shopAddress: string = '',
    shopGstin: string = ''
  ) {
    const doc = new jsPDF({ unit: 'pt', format: 'a4' });
    const pageWidth = 595;
    const pageHeight = 842;
    const leftMargin = 50;
    const rightMargin = 545;

    let yPos = 50;

    const actualShopName = sale.businessName || shopName || 'StoreBook';
    const actualShopAddress = sale.businessAddress || shopAddress || '';
    const actualShopGstin = sale.businessGstin || shopGstin || '';

    // Shop Name (Bold 24pt, left-aligned)
    doc.setFont('helvetica', 'bold');
    doc.setFontSize(24);
    doc.setTextColor(0, 0, 0);
    doc.text(actualShopName, leftMargin, yPos);

    doc.setFont('helvetica', 'normal');
    doc.setFontSize(12);
    yPos += 20;

    if (actualShopAddress.trim()) {
      const lines = actualShopAddress.split('\n');
      for (const line of lines) {
        if (line.trim()) {
          doc.text(line.trim(), leftMargin, yPos);
          yPos += 15;
        }
      }
    }

    if (actualShopGstin.trim()) {
      doc.text(`GSTIN: ${actualShopGstin.trim()}`, leftMargin, yPos);
      yPos += 15;
    }

    // Title (TAX INVOICE or QUOTATION / ESTIMATE, Bold 20pt, centered)
    yPos += 20;
    doc.setFont('helvetica', 'bold');
    doc.setFontSize(20);
    const titleText = sale.type === 'ESTIMATE' ? 'QUOTATION / ESTIMATE' : 'TAX INVOICE';
    doc.text(titleText, pageWidth / 2, yPos, { align: 'center' });

    // Invoice Meta & Customer Info
    yPos += 30;
    doc.setFont('helvetica', 'normal');
    doc.setFontSize(12);

    const dateStr = formatInvoiceDate(sale.timestamp);
    const labelNo = sale.type === 'ESTIMATE' ? 'Quote No:' : 'Invoice No:';
    const cleanId = String(sale.id).startsWith('#') ? sale.id : `#${sale.id}`;

    doc.text(`${labelNo} ${cleanId}`, leftMargin, yPos);
    doc.text(`Date: ${dateStr}`, rightMargin, yPos, { align: 'right' });

    yPos += 20;
    doc.text(`Customer Name: ${sale.customerName || 'Cash Customer'}`, leftMargin, yPos);

    if (sale.customerAddress && sale.customerAddress.trim()) {
      yPos += 15;
      const addrLines = sale.customerAddress.split('\n');
      for (let index = 0; index < addrLines.length; index++) {
        const line = addrLines[index].trim();
        if (line) {
          doc.text(index === 0 ? `Address: ${line}` : line, leftMargin, yPos);
          yPos += 15;
        }
      }
    }

    if (sale.customerGstin && sale.customerGstin.trim()) {
      yPos += 15;
      doc.text(`GSTIN: ${sale.customerGstin.trim()}`, leftMargin, yPos);
    }

    // Table Header
    yPos += 30;
    doc.setFont('helvetica', 'bold');
    doc.setFontSize(12);
    doc.text('Item', leftMargin, yPos);
    doc.text('Qty', 300, yPos);
    doc.text('Rate', 400, yPos);
    doc.text('Total', rightMargin, yPos, { align: 'right' });

    yPos += 10;
    doc.setLineWidth(0.75);
    doc.line(leftMargin, yPos, rightMargin, yPos);
    yPos += 20;

    // Items
    doc.setFont('helvetica', 'normal');
    doc.setFontSize(12);

    let subtotal = 0;
    const itemsToRender = cartItems.length > 0 ? cartItems : [{
      item: {
        id: 'default',
        name: 'Purchases',
        unit: '',
        sellPrice: sale.totalAmount + (sale.discountAmount || 0),
        buyPrice: 0,
      },
      quantity: 1,
    }];

    for (const c of itemsToRender) {
      const lineTotal = c.item.sellPrice * c.quantity;
      subtotal += lineTotal;

      if (yPos > pageHeight - 120) {
        doc.addPage();
        yPos = 50;
      }

      const qtyFormatted = c.quantity % 1 === 0 ? c.quantity.toFixed(1) : String(c.quantity);
      const qtyStr = c.item.unit ? `${qtyFormatted} ${c.item.unit}` : qtyFormatted;
      const rateStr = `Rs ${Number(c.item.sellPrice).toFixed(2)}`;
      const totalStr = `Rs ${Number(lineTotal).toFixed(2)}`;

      doc.text(c.item.name || 'Item', leftMargin, yPos);
      doc.text(qtyStr, 300, yPos);
      doc.text(rateStr, 400, yPos);
      doc.text(totalStr, rightMargin, yPos, { align: 'right' });

      yPos += 20;
    }

    // Divider after items
    if (yPos > pageHeight - 120) {
      doc.addPage();
      yPos = 50;
    }

    yPos += 10;
    doc.line(leftMargin, yPos, rightMargin, yPos);
    yPos += 20;

    // Summary Section
    doc.setFont('helvetica', 'normal');
    doc.setFontSize(12);
    doc.text(`Subtotal: Rs ${subtotal.toFixed(2)}`, rightMargin, yPos, { align: 'right' });
    yPos += 20;

    if (sale.discountAmount && sale.discountAmount > 0) {
      doc.text(`Discount: -Rs ${Number(sale.discountAmount).toFixed(2)}`, rightMargin, yPos, { align: 'right' });
      yPos += 20;
    }

    // Tax breakdown if applicable
    if (actualShopGstin.trim()) {
      const taxSummary = BillingEngine.calculateInvoiceTaxes(
        cartItems.map((c) => ({
          id: c.item.id,
          sell_price: c.item.sellPrice,
          quantity: c.quantity,
          taxRate: c.item.taxRate || 0,
        })),
        sale.discountAmount || 0,
        actualShopGstin,
        sale.customerGstin
      );

      const totalTax = taxSummary.totalCgst + taxSummary.totalSgst + taxSummary.totalIgst;
      if (totalTax > 0) {
        if (taxSummary.totalCgst > 0) {
          doc.text(`CGST: Rs ${taxSummary.totalCgst.toFixed(2)}`, rightMargin, yPos, { align: 'right' });
          yPos += 20;
        }
        if (taxSummary.totalSgst > 0) {
          doc.text(`SGST: Rs ${taxSummary.totalSgst.toFixed(2)}`, rightMargin, yPos, { align: 'right' });
          yPos += 20;
        }
        if (taxSummary.totalIgst > 0) {
          doc.text(`IGST: Rs ${taxSummary.totalIgst.toFixed(2)}`, rightMargin, yPos, { align: 'right' });
          yPos += 20;
        }
      }
    }

    // Grand Total (Bold 14pt)
    doc.setFont('helvetica', 'bold');
    doc.setFontSize(14);
    doc.text(`Grand Total: Rs ${Number(sale.totalAmount).toFixed(2)}`, rightMargin, yPos, { align: 'right' });

    const prefix = sale.type === 'ESTIMATE' ? 'Estimate' : 'Invoice';
    const cleanFileNameId = String(sale.id).replace('#', '');
    doc.save(`${prefix}_${cleanFileNameId}.pdf`);
  }
}
