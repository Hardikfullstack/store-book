import { describe, it, expect, beforeEach } from 'vitest';
import { pdfTemplateSlice } from '../pdfTemplateSlice';

describe('pdfTemplateSlice', () => {
  let state: ReturnType<typeof pdfTemplateSlice.reducer>;

  beforeEach(() => {
    state = pdfTemplateSlice.reducer(undefined, { type: 'TEST_INIT' });
  });

  it('should return default state on unknown action', () => {
    expect(state.templateStyle).toBe('standard-gst');
    expect(state.accentColor).toBe('#0f766e');
    expect(state.isDraft).toBe(true);
    expect(state.showGstBreakdown).toBe(true);
  });

  it('should set full invoice settings via setInvoiceSettings', () => {
    const newSettings = {
      id: 'test-id',
      storeId: 'store-1',
      templateStyle: 'minimalist' as const,
      accentColor: '#ff0000',
      logoUrl: 'https://example.com/logo.png',
      headerText: 'INVOICE',
      footerText: 'Thanks!',
      signatureUrl: '',
      showGstBreakdown: false,
      showBankDetails: true,
      upiId: 'test@upi',
      bankDetails: 'HDFC 1234',
      termsAndConditions: '',
      isDraft: false,
    };
    state = pdfTemplateSlice.reducer(state, { type: 'pdfTemplate/setInvoiceSettings', payload: newSettings });
    expect(state.id).toBe('test-id');
    expect(state.templateStyle).toBe('minimalist');
    expect(state.accentColor).toBe('#ff0000');
  });

  it('should update template style', () => {
    state = pdfTemplateSlice.reducer(state, { type: 'pdfTemplate/updateTemplateStyle', payload: 'thermal-80mm' });
    expect(state.templateStyle).toBe('thermal-80mm');
  });

  it('should update accent color', () => {
    state = pdfTemplateSlice.reducer(state, { type: 'pdfTemplate/updateAccentColor', payload: '#00ff00' });
    expect(state.accentColor).toBe('#00ff00');
  });

  it('should set isDraft false when logo URL is provided', () => {
    state = pdfTemplateSlice.reducer(state, { type: 'pdfTemplate/updateLogoUrl', payload: 'https://example.com/logo.png' });
    expect(state.logoUrl).toBe('https://example.com/logo.png');
    expect(state.isDraft).toBe(false);
  });

  it('should toggle GST breakdown', () => {
    state = pdfTemplateSlice.reducer(state, { type: 'pdfTemplate/toggleGstBreakdown' });
    expect(state.showGstBreakdown).toBe(false);
    expect(state.isDraft).toBe(false);
  });

  it('should toggle bank details', () => {
    state = pdfTemplateSlice.reducer(state, { type: 'pdfTemplate/toggleBankDetails' });
    expect(state.showBankDetails).toBe(true);
    expect(state.isDraft).toBe(false);
  });

  it('should merge partial settings and set isDraft true', () => {
    state = pdfTemplateSlice.reducer(state, {
      type: 'pdfTemplate/mergeInvoiceSettings',
      payload: { accentColor: '#0000ff', templateStyle: 'minimalist' as const }
    });
    expect(state.accentColor).toBe('#0000ff');
    expect(state.templateStyle).toBe('minimalist');
    expect(state.isDraft).toBe(true);
  });

  it('should reset to defaults', () => {
    state = pdfTemplateSlice.reducer(state, { type: 'pdfTemplate/updateAccentColor', payload: '#000000' });
    state = pdfTemplateSlice.reducer(state, { type: 'pdfTemplate/resetInvoiceSettings' });
    expect(state.accentColor).toBe('#0f766e');
    expect(state.templateStyle).toBe('standard-gst');
  });
});
