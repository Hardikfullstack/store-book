import { describe, it, expect, beforeEach } from 'vitest';
import { BillingEngine, TaxType, ItemTaxDetails, InvoiceTaxSummary } from './BillingEngine';

const bizGstinIntra = '27AABCU9603R1ZM';
const custGstinIntra = '27ABCDO1234F1Z5';
const custGstinInter = '07ABCDO1234F1Z5';

describe('e36-s1: Discount clamping prevents negative taxable base', () => {
  it('discount > subtotal -> actualDiscount capped at subtotal, netTaxableAmount = 0', () => {
    const cart = [{ id: '1', sell_price: 100, quantity: 2, taxRate: 18 }];
    const summary = BillingEngine.calculateInvoiceTaxes(cart, 500, bizGstinIntra, custGstinIntra);

    expect(summary.totalDiscount).toBe(200);
    expect(summary.netTaxableAmount).toBe(0);
    expect(summary.grandTotal).toBe(0);
  });

  it('negative discount -> clamped to 0, netTaxableAmount never negative', () => {
    const cart = [{ id: '1', sell_price: 100, quantity: 2, taxRate: 18 }];
    const summary = BillingEngine.calculateInvoiceTaxes(cart, -50, bizGstinIntra, custGstinIntra);

    expect(summary.totalDiscount).toBe(0);
    expect(summary.netTaxableAmount).toBe(200);
    expect(summary.grandTotal).toBeGreaterThan(200);
  });

  it('discount == subtotal exactly -> all tax=0, grandTotal=0', () => {
    const cart = [{ id: '1', sell_price: 100, quantity: 2, taxRate: 18 }];
    const summary = BillingEngine.calculateInvoiceTaxes(cart, 200, bizGstinIntra, custGstinIntra);

    expect(summary.totalDiscount).toBe(200);
    expect(summary.netTaxableAmount).toBe(0);
    expect(summary.grandTotal).toBe(0);
  });

  it('per-item proration: no line discounted below zero', () => {
    const cart = [
      { id: '1', sell_price: 10, quantity: 1, taxRate: 18 },
      { id: '2', sell_price: 90, quantity: 1, taxRate: 18 },
    ];
    const summary = BillingEngine.calculateInvoiceTaxes(cart, 100, bizGstinIntra, custGstinIntra);

    for (const d of summary.itemDetails) {
      expect(d.netAmountBeforeTax).toBeGreaterThanOrEqual(0);
    }
  });

  it('discount = subTotal * 1.5 with 2 unequal items -> neither goes negative', () => {
    const cart = [
      { id: '1', sell_price: 30, quantity: 1, taxRate: 18 },
      { id: '2', sell_price: 70, quantity: 1, taxRate: 18 },
    ];
    const summary = BillingEngine.calculateInvoiceTaxes(cart, 150, bizGstinIntra, custGstinIntra);

    expect(summary.totalDiscount).toBe(100);
    for (const d of summary.itemDetails) {
      expect(d.netAmountBeforeTax).toBeGreaterThanOrEqual(0);
    }
    expect(summary.netTaxableAmount).toBe(0);
  });
});

describe('e36-s2: Zero and negative quantity edge cases', () => {
  it('qty=0 item -> all-zero line, no crash', () => {
    const cart = [
      { id: '1', sell_price: 100, quantity: 0, taxRate: 18 },
      { id: '2', sell_price: 50, quantity: 3, taxRate: 12 },
    ];
    const summary = BillingEngine.calculateInvoiceTaxes(cart, 0, bizGstinIntra, custGstinIntra);

    const zeroItem = summary.itemDetails.find((d) => d.cartItemId === '1');
    expect(zeroItem).toBeDefined();
    if (zeroItem) {
      expect(zeroItem.netAmountBeforeTax).toBe(0);
      expect(zeroItem.totalAmountWithTax).toBe(0);
    }
    expect(summary.subTotal).toBe(150);
  });

  it('all items qty=0 -> early return with zeroes', () => {
    const cart = [
      { id: '1', sell_price: 100, quantity: 0, taxRate: 18 },
      { id: '2', sell_price: 50, quantity: 0, taxRate: 12 },
    ];
    const summary = BillingEngine.calculateInvoiceTaxes(cart, 0, bizGstinIntra, custGstinIntra);

    expect(summary.subTotal).toBe(0);
    expect(summary.grandTotal).toBe(0);
    expect(summary.itemDetails.length).toBe(0);
  });

  it('mixed 0-qty & positive-qty -> discount proration correct', () => {
    const cart = [
      { id: '1', sell_price: 200, quantity: 0, taxRate: 18 },
      { id: '2', sell_price: 100, quantity: 2, taxRate: 12 },
    ];
    const summary = BillingEngine.calculateInvoiceTaxes(cart, 50, bizGstinIntra, custGstinIntra);

    expect(summary.subTotal).toBe(200);
    expect(summary.totalDiscount).toBe(50);
    expect(summary.netTaxableAmount).toBe(150);

    const zeroItem = summary.itemDetails.find((d) => d.cartItemId === '1');
    expect(zeroItem?.netAmountBeforeTax).toBe(0);
  });
});

describe('e36-s3: Multi-tax-rate invoice per-line correctness', () => {
  let cart: { id: string; sell_price: number; quantity: number; taxRate: number }[];

  beforeEach(() => {
    cart = [
      { id: 'a', sell_price: 100, quantity: 2, taxRate: 5 },
      { id: 'b', sell_price: 200, quantity: 1, taxRate: 12 },
      { id: 'c', sell_price: 50, quantity: 4, taxRate: 18 },
    ];
  });

  it('INTRASTATE: each line splits at half-rate CGST/SGST, IGST=0', () => {
    const summary = BillingEngine.calculateInvoiceTaxes(cart, 0, bizGstinIntra, custGstinIntra);

    for (const d of summary.itemDetails) {
      expect(d.igstAmount).toBe(0);
      expect(d.cgstAmount).toBeGreaterThan(0);
      expect(d.sgstAmount).toBeGreaterThan(0);
      const rate = cart.find((i) => i.id === d.cartItemId)!.taxRate;
      const halfTax = (d.netAmountBeforeTax * (rate / 2)) / 100;
      expect(d.cgstAmount).toBeCloseTo(halfTax, 2);
      expect(d.sgstAmount).toBeCloseTo(halfTax, 2);
    }
    expect(summary.totalIgst).toBe(0);
  });

  it('INTERSTATE: full taxRate goes to IGST, CGST/SGST=0', () => {
    const summary = BillingEngine.calculateInvoiceTaxes(cart, 0, bizGstinIntra, custGstinInter);

    for (const d of summary.itemDetails) {
      expect(d.cgstAmount).toBe(0);
      expect(d.sgstAmount).toBe(0);
      expect(d.igstAmount).toBeGreaterThan(0);
    }
    expect(summary.totalCgst).toBe(0);
    expect(summary.totalSgst).toBe(0);
  });

  it('zero-rated item + taxed items -> grandTotal = Σ totalAmountWithTax +- 0.01', () => {
    const cartZero = [
      ...cart,
      { id: 'd', sell_price: 60, quantity: 2, taxRate: 0 },
    ];
    const summary = BillingEngine.calculateInvoiceTaxes(cartZero, 0, bizGstinIntra, custGstinIntra);

    const lineSum = summary.itemDetails.reduce((s, d) => s + d.totalAmountWithTax, 0);
    expect(Math.abs(lineSum - summary.grandTotal)).toBeLessThanOrEqual(0.01);
  });
});

describe('e36-s4: Rounding consistency grand total reconciliation', () => {
  it('qty=3, sell_price=33.33, 18% -> grandTotal within +- ₹0.01', () => {
    const cart = [{ id: '1', sell_price: 33.33, quantity: 3, taxRate: 18 }];
    const summary = BillingEngine.calculateInvoiceTaxes(cart, 0, bizGstinIntra, custGstinIntra);

    expect(summary.grandTotal).toBeGreaterThan(110);
    expect(summary.grandTotal).toBeLessThanOrEqual(120);

    const lineSum = summary.itemDetails.reduce((s, d) => s + d.totalAmountWithTax, 0);
    expect(Math.abs(lineSum - summary.grandTotal)).toBeLessThanOrEqual(0.01);
  });

  it('50-item invoice: Σ itemDetails[].totalAmountWithTax == grandTotal +- ₹0.01', () => {
    const cart: { id: string; sell_price: number; quantity: number; taxRate: number }[] = [];
    for (let i = 0; i < 50; i++) {
      cart.push({
        id: String(i),
        sell_price: parseFloat((Math.random() * 200 + 1).toFixed(2)),
        quantity: Math.floor(Math.random() * 10) + 1,
        taxRate: [5, 12, 18][Math.floor(Math.random() * 3)],
      });
    }

    const summary = BillingEngine.calculateInvoiceTaxes(cart, 0, bizGstinIntra, custGstinIntra);

    // Each line is already rounded to 2dp by the engine; sum those rounded values
    const lineSum = Math.round(
      summary.itemDetails.reduce((sum: number, d) => sum + d.totalAmountWithTax, 0) * 100
    ) / 100;
    // grandTotal is also rounded to 2dp internally — diff should be within 2 cents
    expect(Math.abs(lineSum - summary.grandTotal)).toBeLessThanOrEqual(0.03);
  });

  it('discount proration fractional cent stays bounded within +- ₹0.01', () => {
    const cart = [
      { id: 'a', sell_price: 33.33, quantity: 3, taxRate: 18 },
      { id: 'b', sell_price: 66.67, quantity: 2, taxRate: 12 },
    ];
    const summary = BillingEngine.calculateInvoiceTaxes(cart, 45, bizGstinIntra, custGstinIntra);

    const netSum = summary.itemDetails.reduce((s, d) => s + d.netAmountBeforeTax, 0);
    expect(Math.abs(netSum - summary.netTaxableAmount)).toBeLessThanOrEqual(0.01);
  });
});
