import jsPDF from 'jspdf';
import 'jspdf-autotable';
import { BillingEngine, TaxType } from './BillingEngine';

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

    // Table Data
    const tableBody = cartItems.map((c, index) => {
      const lineTotal = c.item.sellPrice * c.quantity;
      const taxDetail = taxSummary.itemDetails.find(td => td.cartItemId === c.item.id);
      
      const rate = c.item.taxRate || 0;
      
      let taxStr = `${rate}%`;
      if (taxDetail && rate > 0) {
        if (taxDetail.cgstAmount > 0) taxStr = `C+S ${rate}%`;
        else if (taxDetail.igstAmount > 0) taxStr = `IGST ${rate}%`;
      }

      return [
        index + 1,
        c.item.name,
        c.item.hsnCode || '-',
        `${c.quantity} ${c.item.unit}`,
        c.item.sellPrice.toFixed(2),
        taxStr,
        lineTotal.toFixed(2)
      ];
    });

    (doc as any).autoTable({
      startY: yPos,
      head: [['#', 'Item', 'HSN', 'Qty', 'Rate', 'Tax', 'Total']],
      body: tableBody,
      theme: 'grid',
      headStyles: { fillColor: [13, 148, 136] }, // teal-600
      columnStyles: {
        0: { cellWidth: 10 },
        4: { halign: 'right' },
        5: { halign: 'center' },
        6: { halign: 'right' }
      }
    });

    yPos = (doc as any).lastAutoTable.finalY + 10;

    // Summary Section
    doc.setFontSize(10);
    doc.text(`Subtotal:`, pageWidth - 60, yPos);
    doc.text(taxSummary.subTotal.toFixed(2), pageWidth - 14, yPos, { align: 'right' });
    yPos += 6;

    if (sale.discountAmount > 0) {
      doc.setTextColor(220, 38, 38); // red-600
      doc.text(`Discount:`, pageWidth - 60, yPos);
      doc.text(`-${sale.discountAmount.toFixed(2)}`, pageWidth - 14, yPos, { align: 'right' });
      doc.setTextColor(0, 0, 0);
      yPos += 6;
    }

    const totalTax = taxSummary.totalCgst + taxSummary.totalSgst + taxSummary.totalIgst;
    if (totalTax > 0) {
      if (taxSummary.totalCgst > 0) {
        doc.text(`CGST:`, pageWidth - 60, yPos);
        doc.text(taxSummary.totalCgst.toFixed(2), pageWidth - 14, yPos, { align: 'right' });
        yPos += 6;
      }
      if (taxSummary.totalSgst > 0) {
        doc.text(`SGST:`, pageWidth - 60, yPos);
        doc.text(taxSummary.totalSgst.toFixed(2), pageWidth - 14, yPos, { align: 'right' });
        yPos += 6;
      }
      if (taxSummary.totalIgst > 0) {
        doc.text(`IGST:`, pageWidth - 60, yPos);
        doc.text(taxSummary.totalIgst.toFixed(2), pageWidth - 14, yPos, { align: 'right' });
        yPos += 6;
      }
    }

    yPos += 4;
    doc.setLineWidth(0.5);
    doc.line(pageWidth - 60, yPos, pageWidth - 14, yPos);
    yPos += 8;

    doc.setFontSize(14);
    doc.setFont('helvetica', 'bold');
    doc.text(`Grand Total:`, pageWidth - 60, yPos);
    doc.text(taxSummary.grandTotal.toFixed(2), pageWidth - 14, yPos, { align: 'right' });

    yPos += 15;
    doc.setFontSize(10);
    doc.setFont('helvetica', 'italic');
    doc.text("Thank you for your business!", pageWidth / 2, yPos, { align: 'center' });

    // Save PDF
    const fileName = `${sale.type === 'ESTIMATE' ? 'Estimate' : 'Invoice'}_${sale.id.substring(0, 8)}.pdf`;
    doc.save(fileName);
  }
}
