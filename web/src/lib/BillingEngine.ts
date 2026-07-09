export enum TaxType {
  INTRASTATE = 'INTRASTATE', // CGST + SGST
  INTERSTATE = 'INTERSTATE', // IGST
}

export interface ItemTaxDetails {
  cartItemId: string;
  netAmountBeforeTax: number;
  cgstAmount: number;
  sgstAmount: number;
  igstAmount: number;
  totalTaxAmount: number;
  totalAmountWithTax: number;
}

export interface InvoiceTaxSummary {
  subTotal: number;
  totalDiscount: number;
  netTaxableAmount: number;
  totalCgst: number;
  totalSgst: number;
  totalIgst: number;
  grandTotal: number;
  itemDetails: ItemTaxDetails[];
}

export class BillingEngine {
  static getStateCodeFromGSTIN(gstin?: string | null): string | null {
    if (!gstin || gstin.trim().length < 2) return null;
    return gstin.substring(0, 2);
  }

  static determineTaxType(
    businessGstin?: string | null,
    customerGstin?: string | null
  ): TaxType {
    const bizState = this.getStateCodeFromGSTIN(businessGstin);
    const custState = this.getStateCodeFromGSTIN(customerGstin);

    if (bizState && custState && bizState !== custState) {
      return TaxType.INTERSTATE;
    }
    return TaxType.INTRASTATE;
  }

  static calculateInvoiceTaxes(
    cartItems: { id: string; sell_price: number; quantity: number; taxRate?: number }[],
    totalDiscount: number,
    businessGstin?: string | null,
    customerGstin?: string | null
  ): InvoiceTaxSummary {
    const taxType = this.determineTaxType(businessGstin, customerGstin);

    let subTotal = 0;
    for (const item of cartItems) {
      subTotal += item.sell_price * item.quantity;
    }

    const actualDiscount = Math.min(totalDiscount, subTotal);
    const netTaxableAmount = subTotal - actualDiscount;

    if (subTotal <= 0) {
      return {
        subTotal: 0,
        totalDiscount: 0,
        netTaxableAmount: 0,
        totalCgst: 0,
        totalSgst: 0,
        totalIgst: 0,
        grandTotal: 0,
        itemDetails: [],
      };
    }

    let totalCgst = 0;
    let totalSgst = 0;
    let totalIgst = 0;
    const itemDetails: ItemTaxDetails[] = [];

    for (const item of cartItems) {
      const itemGross = item.sell_price * item.quantity;
      const itemDiscountRatio = subTotal > 0 ? itemGross / subTotal : 0;
      const itemDiscount = actualDiscount * itemDiscountRatio;
      const itemNetTaxable = Math.max(0, itemGross - itemDiscount);

      const taxRate = item.taxRate || 0;
      let cgst = 0;
      let sgst = 0;
      let igst = 0;

      if (taxType === TaxType.INTRASTATE) {
        const halfRate = taxRate / 2;
        cgst = (itemNetTaxable * halfRate) / 100;
        sgst = (itemNetTaxable * halfRate) / 100;
      } else {
        igst = (itemNetTaxable * taxRate) / 100;
      }

      const itemTotalTax = cgst + sgst + igst;

      totalCgst += cgst;
      totalSgst += sgst;
      totalIgst += igst;

      itemDetails.push({
        cartItemId: item.id,
        netAmountBeforeTax: Number(itemNetTaxable.toFixed(2)),
        cgstAmount: Number(cgst.toFixed(2)),
        sgstAmount: Number(sgst.toFixed(2)),
        igstAmount: Number(igst.toFixed(2)),
        totalTaxAmount: Number(itemTotalTax.toFixed(2)),
        totalAmountWithTax: Number((itemNetTaxable + itemTotalTax).toFixed(2)),
      });
    }

    const grandTotal = netTaxableAmount + totalCgst + totalSgst + totalIgst;

    return {
      subTotal: Number(subTotal.toFixed(2)),
      totalDiscount: Number(actualDiscount.toFixed(2)),
      netTaxableAmount: Number(netTaxableAmount.toFixed(2)),
      totalCgst: Number(totalCgst.toFixed(2)),
      totalSgst: Number(totalSgst.toFixed(2)),
      totalIgst: Number(totalIgst.toFixed(2)),
      grandTotal: Number(grandTotal.toFixed(2)),
      itemDetails,
    };
  }
}
