import jsPDF from 'jspdf';
import 'jspdf-autotable';
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

export class InvoicePdfGenerator {
  static generateInvoicePdf(
    sale: PdfSaleData,
    cartItems: PdfCartItem[],
    shopName: string,
    shopAddress: string,
    shopGstin: string
  ) {
    const doc = new jsPDF();
    const pageWidth = doc.internal.pageSize.width || 210;

    // Header
    doc.setFontSize(22);
    doc.setFont('helvetica', 'bold');
    doc.text(shopName, 14, 20);

    doc.setFontSize(10);
    doc.setFont('helvetica', 'normal');

    let yPos = 28;
    if (shopAddress) {
      const addressLines = doc.splitTextToSize(shopAddress, pageWidth / 2);
      doc.text(addressLines, 14, yPos);
      yPos += addressLines.length * 5;
    }

    const actualShopGstin = sale.businessGstin || shopGstin;
    if (actualShopGstin) {
      doc.text(`GSTIN: ${actualShopGstin}`, 14, yPos);
      yPos += 7;
    }

    // Title
    yPos += 5;
    doc.setFontSize(16);
    doc.setFont('helvetica', 'bold');
    const title = sale.type === 'ESTIMATE' ? 'QUOTATION / ESTIMATE' : 'TAX INVOICE';
    doc.text(title, pageWidth / 2, yPos, { align: 'center' });
    yPos += 15;

    // Invoice Meta & Customer Info
    doc.setFontSize(10);
    doc.setFont('helvetica', 'normal');

    const dateStr = new Date(sale.timestamp).toLocaleString();
    const labelNo = sale.type === 'ESTIMATE' ? 'Quote No:' : 'Invoice No:';

    doc.text(`${labelNo} #${sale.id.substring(0, 8)}`, 14, yPos);
    doc.text(`Date: ${dateStr}`, pageWidth - 14, yPos, { align: 'right' });

    yPos += 7;
    doc.text(`Customer Name: ${sale.customerName || 'Cash Customer'}`, 14, yPos);

    if (sale.customerAddress) {
      yPos += 7;
      const addrLines = doc.splitTextToSize(`Address: ${sale.customerAddress}`, pageWidth / 2);
      doc.text(addrLines, 14, yPos);
      yPos += addrLines.length * 5;
    }

    if (sale.customerGstin) {
      yPos += 7;
      doc.text(`GSTIN: ${sale.customerGstin}`, 14, yPos);
    }

    yPos += 10;

    // Calculate Taxes
    const taxSummary = BillingEngine.calculateInvoiceTaxes(
      cartItems.map(c => ({
        id: c.item.id,
        sell_price: c.item.sellPrice,
        quantity: c.quantity,
        taxRate: c.item.taxRate || 0
      })),
      sale.discountAmount || 0,
      actualShopGstin,
      sale.customerGstin
    );

    // Table Data — match item-level taxes (consume sequentially for duplicate ids)
    const consumedIndices = new Set<number>();
    const tableBody: string[][] = [];
    const footTotals: { taxable: number; cgst: number; sgst: number; igst: number } = {
      taxable: 0, cgst: 0, sgst: 0, igst: 0
    };

    for (let index = 0; index < cartItems.length; index++) {
      const c = cartItems[index];
      let taxDetail: typeof taxSummary.itemDetails[number] | undefined;
      for (let i = 0; i < taxSummary.itemDetails.length; i++) {
        if (!consumedIndices.has(i) && taxSummary.itemDetails[i].cartItemId === c.item.id) {
          taxDetail = taxSummary.itemDetails[i];
          consumedIndices.add(i);
          break;
        }
      }

      const taxableValue = taxDetail?.netAmountBeforeTax ?? (c.item.sellPrice * c.quantity);
      const cgstAmt = taxDetail?.cgstAmount ?? 0;
      const sgstAmt = taxDetail?.sgstAmount ?? 0;
      const igstAmt = taxDetail?.igstAmount ?? 0;
      const lineTotal = taxableValue + cgstAmt + sgstAmt + igstAmt;

      footTotals.taxable += taxableValue;
      footTotals.cgst += cgstAmt;
      footTotals.sgst += sgstAmt;
      footTotals.igst += igstAmt;

      tableBody.push([
        String(index + 1),
        c.item.hsnCode || '-',
        `${c.quantity} ${c.item.unit}`,
        taxableValue.toFixed(2),
        cgstAmt > 0 ? cgstAmt.toFixed(2) : '-',
        sgstAmt > 0 ? sgstAmt.toFixed(2) : '-',
        igstAmt > 0 ? igstAmt.toFixed(2) : '-',
        lineTotal.toFixed(2),
      ]);
    }

    (doc as any).autoTable({
      startY: yPos,
      head: [['#', 'HSN', 'Qty', 'Net Amt', 'CGST', 'SGST', 'IGST', 'Total']],
      body: tableBody,
      foot: [
        ['', '', '', footTotals.taxable.toFixed(2),
          footTotals.cgst > 0 ? footTotals.cgst.toFixed(2) : '-',
          footTotals.sgst > 0 ? footTotals.sgst.toFixed(2) : '-',
          footTotals.igst > 0 ? footTotals.igst.toFixed(2) : '-',
          (footTotals.taxable + footTotals.cgst + footTotals.sgst + footTotals.igst).toFixed(2)]
      ],
      theme: 'grid',
      headStyles: { fillColor: [13, 148, 136] },
      footStyles: { fillColor: [240, 240, 240], fontStyle: 'bold' },
      columnStyles: {
        0: { cellWidth: 10, halign: 'center' },
        1: { cellWidth: 18 },
        2: { cellWidth: 25 },
        3: { halign: 'right', cellWidth: 20 },
        4: { halign: 'right', cellWidth: 18 },
        5: { halign: 'right', cellWidth: 18 },
        6: { halign: 'right', cellWidth: 18 },
        7: { halign: 'right', cellWidth: 20 }
      },
      didDrawCell: function (data: any) {
        if (data.row.section === 'body') {
          data.cell.pos.x;
        }
      }
    });

    // Item description table (separate, no grid overlap with tax columns)
    const descYStart = yPos;
    let descYEnd = (doc as any).lastAutoTable.finalY;

    // Summary Section
    doc.setFontSize(10);
    doc.setFont('helvetica', 'normal');
    descYEnd += 8;

    if (sale.discountAmount > 0) {
      doc.setTextColor(220, 38, 38);
      doc.text(`Subtotal:`, pageWidth - 60, descYEnd);
      doc.text(taxSummary.subTotal.toFixed(2), pageWidth - 14, descYEnd, { align: 'right' });
      descYEnd += 6;

      doc.text(`Discount:`, pageWidth - 60, descYEnd);
      doc.text(`-${sale.discountAmount.toFixed(2)}`, pageWidth - 14, descYEnd, { align: 'right' });
      descYEnd += 6;
      doc.setTextColor(0, 0, 0);
    }

    const totalTax = taxSummary.totalCgst + taxSummary.totalSgst + taxSummary.totalIgst;
    if (totalTax > 0) {
      if (taxSummary.totalCgst > 0) {
        doc.text(`CGST:`, pageWidth - 60, descYEnd);
        doc.text(taxSummary.totalCgst.toFixed(2), pageWidth - 14, descYEnd, { align: 'right' });
        descYEnd += 6;
      }
      if (taxSummary.totalSgst > 0) {
        doc.text(`SGST:`, pageWidth - 60, descYEnd);
        doc.text(taxSummary.totalSgst.toFixed(2), pageWidth - 14, descYEnd, { align: 'right' });
        descYEnd += 6;
      }
      if (taxSummary.totalIgst > 0) {
        doc.text(`IGST:`, pageWidth - 60, descYEnd);
        doc.text(taxSummary.totalIgst.toFixed(2), pageWidth - 14, descYEnd, { align: 'right' });
        descYEnd += 6;
      }
    }

    descYEnd += 3;
    doc.setLineWidth(0.5);
    doc.line(pageWidth - 60, descYEnd, pageWidth - 14, descYEnd);
    descYEnd += 8;

    doc.setFontSize(14);
    doc.setFont('helvetica', 'bold');
    doc.text(`Grand Total:`, pageWidth - 60, descYEnd);
    doc.text(`₹ ${taxSummary.grandTotal.toFixed(2)}`, pageWidth - 14, descYEnd, { align: 'right' });

    descYEnd += 15;
    doc.setFontSize(10);
    doc.setFont('helvetica', 'italic');
    doc.text("Thank you for your business!", pageWidth / 2, descYEnd, { align: 'center' });

    const fileName = `${sale.type === 'ESTIMATE' ? 'Estimate' : 'Invoice'}_${sale.id.substring(0, 8)}.pdf`;
    doc.save(fileName);
  }
}
