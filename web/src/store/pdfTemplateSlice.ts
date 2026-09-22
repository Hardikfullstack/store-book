import { createSlice, PayloadAction } from '@reduxjs/toolkit';

export type TemplateStyle = 'standard-gst' | 'minimalist' | 'thermal-80mm';

export interface InvoiceSettingsState {
  id: string;
  storeId: string;
  templateStyle: TemplateStyle;
  logoUrl: string;
  accentColor: string;
  headerText: string;
  footerText: string;
  signatureUrl: string;
  showGstBreakdown: boolean;
  showBankDetails: boolean;
  upiId: string;
  bankDetails: string;
  termsAndConditions: string;
  isDraft: boolean;
}

const defaultSettings: InvoiceSettingsState = {
  id: '',
  storeId: '',
  templateStyle: 'standard-gst',
  logoUrl: '',
  accentColor: '#0f766e',
  headerText: 'TAX INVOICE',
  footerText: 'Thank you for your business!',
  signatureUrl: '',
  showGstBreakdown: true,
  showBankDetails: false,
  upiId: '',
  bankDetails: '',
  termsAndConditions: '',
  isDraft: true,
};

export const pdfTemplateSlice = createSlice({
  name: 'pdfTemplate',
  initialState: defaultSettings,
  reducers: {
    setInvoiceSettings(state, action: PayloadAction<InvoiceSettingsState>) {
      return { ...action.payload };
    },
    updateTemplateStyle(state, action: PayloadAction<TemplateStyle>) {
      state.templateStyle = action.payload;
    },
    updateAccentColor(state, action: PayloadAction<string>) {
      state.accentColor = action.payload;
    },
    updateLogoUrl(state, action: PayloadAction<string>) {
      state.logoUrl = action.payload;
      if (action.payload) {
        state.isDraft = false;
      }
    },
    updateSignatureUrl(state, action: PayloadAction<string>) {
      state.signatureUrl = action.payload;
      if (action.payload) {
        state.isDraft = false;
      }
    },
    updateHeaderText(state, action: PayloadAction<string>) {
      state.headerText = action.payload;
      state.isDraft = false;
    },
    updateFooterText(state, action: PayloadAction<string>) {
      state.footerText = action.payload;
      state.isDraft = false;
    },
    updateBankDetails(state, action: PayloadAction<string>) {
      state.bankDetails = action.payload;
      state.isDraft = false;
    },
    updateTermsAndConditions(state, action: PayloadAction<string>) {
      state.termsAndConditions = action.payload;
      state.isDraft = false;
    },
    toggleGstBreakdown(state) {
      state.showGstBreakdown = !state.showGstBreakdown;
      state.isDraft = false;
    },
    toggleBankDetails(state) {
      state.showBankDetails = !state.showBankDetails;
      state.isDraft = false;
    },
    updateUpiId(state, action: PayloadAction<string>) {
      state.upiId = action.payload;
      state.isDraft = false;
    },
    mergeInvoiceSettings(
      state,
      action: PayloadAction<Partial<InvoiceSettingsState>>
    ) {
      return { ...state, ...action.payload, isDraft: true };
    },
    resetInvoiceSettings() {
      return { ...defaultSettings };
    },
  },
});

export const {
  setInvoiceSettings,
  updateTemplateStyle,
  updateAccentColor,
  updateLogoUrl,
  updateSignatureUrl,
  updateHeaderText,
  updateFooterText,
  updateBankDetails,
  updateTermsAndConditions,
  toggleGstBreakdown,
  toggleBankDetails,
  updateUpiId,
  mergeInvoiceSettings,
  resetInvoiceSettings,
} = pdfTemplateSlice.actions;

export default pdfTemplateSlice.reducer;
