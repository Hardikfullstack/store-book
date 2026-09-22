'use client';

import { useCallback, useEffect, useRef, useState } from 'react';
import { useSelector, useDispatch } from 'react-redux';
import {
  setInvoiceSettings,
  updateTemplateStyle,
  updateAccentColor,
  updateLogoUrl,
  updateHeaderText,
  updateFooterText,
  toggleGstBreakdown,
  toggleBankDetails,
  updateUpiId,
  updateBankDetails,
  updateTermsAndConditions,
  InvoiceSettingsState,
  TemplateStyle,
} from '@/store/pdfTemplateSlice';
import { RootState } from '@/store';
import { storage } from '@/lib/firebase';
import { ref as storageRef, uploadBytes, getDownloadURL, deleteObject } from 'firebase/storage';
import { FileText, Palette, ToggleLeft, ToggleRight, Loader2, RotateCcw } from 'lucide-react';
import type { PdfSaleData, PdfCartItem } from '@/lib/InvoicePdfGenerator';
import InvoicePreview from '@/components/InvoicePreview';

const mockSale: PdfSaleData = {
  id: 'preview-inv-001',
  timestamp: Date.now(),
  totalAmount: 1368,
  discountAmount: 0,
  customerName: 'Rajesh Traders',
  customerGstin: '27AAPFU0939F1ZV',
  businessGstin: '27AABCU9603R1ZM',
  customerAddress: '42 MG Road, Pune MH 411001',
  businessAddress: '',
  type: 'INVOICE',
};

const mockCartItems: PdfCartItem[] = [
  {
    item: { id: 'item-1', name: 'Wireless Mouse', unit: 'pcs', sellPrice: 899, buyPrice: 500, taxRate: 18, hsnCode: '8471' },
    quantity: 1,
  },
  {
    item: { id: 'item-2', name: 'USB-C Fast Cable (1m)', unit: 'pcs', sellPrice: 349, buyPrice: 180, taxRate: 12, hsnCode: '8544' },
    quantity: 2,
  },
  {
    item: { id: 'item-3', name: 'A5 Notebook Pack (x5)', unit: 'pack', sellPrice: 420, buyPrice: 250, taxRate: 18, hsnCode: '4820' },
    quantity: 1,
  },
];

const templates: { id: TemplateStyle; name: string; description: string }[] = [
  {
    id: 'standard-gst',
    name: 'Standard GST',
    description: 'Full invoice with GST breakdown and tax compliance fields.',
  },
  {
    id: 'minimalist',
    name: 'Minimalist',
    description: 'Clean, compact layout for quick transactions.',
  },
  {
    id: 'thermal-80mm',
    name: 'Thermal 80mm',
    description: 'Narrow receipt format optimized for thermal printers.',
  },
];

export default function TemplateEditorClient({
  initialSettings,
}: {
  initialSettings: InvoiceSettingsState | null;
}) {
  const dispatch = useDispatch();
  const settings = useSelector((state: RootState) => state.pdfTemplate);
  const initialized = useRef(false);

  useEffect(() => {
    if (initialSettings && !initialized.current) {
      dispatch(setInvoiceSettings(initialSettings));
      initialized.current = true;
    }
  }, [dispatch, initialSettings]);

  const [isUploading, setIsUploading] = useState(false);
  const [uploadSessionId] = useState(() => `ses_${Date.now()}_${Math.random().toString(36).substring(2, 8)}`);

  const handleLogoUpload = useCallback(async () => {
    if (isUploading) return;
    const input = document.createElement('input');
    input.type = 'file';
    input.accept = 'image/*';
    input.onchange = async (e: Event) => {
      const target = e.target as HTMLInputElement;
      const file = target.files?.[0];
      if (!file) return;

      if (file.size > 2 * 1024 * 1024) {
        alert('Logo must be under 2 MB.');
        return;
      }

      setIsUploading(true);
      try {
        const storageFile = `logos/${uploadSessionId}/${Date.now()}-${file.name.replace(/[^a-zA-Z0-9.-]/g, '')}`;
        const fileRef = storageRef(storage, storageFile);
        await uploadBytes(fileRef, file);
        const downloadUrl = await getDownloadURL(fileRef);
        dispatch(updateLogoUrl(downloadUrl));
      } catch (err) {
        console.error('Storage upload failed:', err);
        alert('Failed to upload logo. Please try again.');
      } finally {
        setIsUploading(false);
      }
    };
    input.click();
  }, [isUploading, dispatch, uploadSessionId]);

  const handleRemoveLogo = useCallback(() => {
    if (settings.logoUrl) {
      // Best-effort delete from Storage by URL derivation
      try {
          const urlParts = settings.logoUrl.split('o/');
          if (urlParts.length > 1) {
              const encodedPath = urlParts[1].split('&')[0];
              const path = decodeURIComponent(encodedPath);
              const logoRef = storageRef(storage, path);
              deleteObject(logoRef).catch(() => {/* best-effort */});
          }
      } catch { /* ignore cleanup errors */ }
    }
    dispatch(updateLogoUrl(''));
  }, [settings.logoUrl, dispatch]);

  return (
    <div className="space-y-8 max-w-4xl">
      <div>
        <h1 className="text-2xl font-bold text-gray-900 dark:text-white flex items-center gap-2">
          <FileText className="text-teal-500" />
          Invoice Template Editor
        </h1>
        <p className="text-gray-500 dark:text-gray-400 text-sm mt-1">
          Customize your invoice appearance, branding, and content fields.
        </p>
      </div>

      {/* Template Selector */}
      <section className="glass-card p-6">
        <h2 className="text-lg font-bold text-gray-900 dark:text-white mb-4">
          Template Style
        </h2>
        <p className="text-sm text-gray-500 dark:text-gray-400 mb-6">
          Choose a layout for your invoices.
        </p>
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          {templates.map((template) => (
            <button
              key={template.id}
              onClick={() => dispatch(updateTemplateStyle(template.id))}
              className={`relative p-5 rounded-xl border-2 text-left transition-all ${
                settings.templateStyle === template.id
                  ? 'border-teal-500 bg-teal-50 dark:bg-teal-900/20 shadow-md'
                  : 'border-gray-200 dark:border-gray-700 hover:border-gray-300 dark:hover:border-gray-600 bg-white dark:bg-gray-800'
              }`}
            >
              {settings.templateStyle === template.id && (
                <span className="absolute top-3 right-3 inline-flex items-center justify-center w-5 h-5 rounded-full bg-teal-500 text-white">
                  <svg
                    xmlns="http://www.w3.org/2000/svg"
                    width="12"
                    height="12"
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="currentColor"
                    strokeWidth="3"
                    strokeLinecap="round"
                    strokeLinejoin="round"
                  >
                    <polyline points="20 6 9 17 4 12" />
                  </svg>
                </span>
              )}
              <h3 className="font-semibold text-gray-900 dark:text-white">
                {template.name}
              </h3>
              <p className="text-xs text-gray-500 dark:text-gray-400 mt-1">
                {template.description}
              </p>
            </button>
          ))}
        </div>
      </section>

      {/* Branding Controls */}
      <section className="glass-card p-6">
        <div className="flex items-center space-x-3 mb-6 border-b border-gray-100 dark:border-gray-800 pb-4">
          <div className="p-2 rounded-lg bg-purple-50 dark:bg-purple-900/30 text-purple-600 dark:text-purple-400">
            <Palette size={20} />
          </div>
          <div>
            <h2 className="text-lg font-bold text-gray-900 dark:text-white">
              Branding Controls
            </h2>
            <p className="text-sm text-gray-500 dark:text-gray-400">
              Customize colors, logo, and headers.
            </p>
          </div>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          {/* Accent Color */}
          <div>
            <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
              Accent Color
            </label>
            <div className="flex items-center space-x-3">
              <input
                type="color"
                value={settings.accentColor}
                onChange={(e) => dispatch(updateAccentColor(e.target.value))}
                className="h-10 w-14 rounded-lg border border-gray-200 dark:border-gray-700 cursor-pointer bg-white dark:bg-gray-800 p-0.5"
              />
              <input
                type="text"
                value={settings.accentColor}
                onChange={(e) => dispatch(updateAccentColor(e.target.value))}
                className="flex-1 px-3 py-2 border border-gray-200 dark:border-gray-700 bg-white dark:bg-gray-800 rounded-lg text-sm font-mono text-gray-900 dark:text-white focus:outline-none focus:ring-2 focus:ring-teal-500 focus:border-transparent"
              />
            </div>
          </div>

          {/* Logo Upload */}
          <div>
            <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
              Logo
            </label>
            <button
              onClick={handleLogoUpload}
              disabled={isUploading}
              className="inline-flex items-center px-4 py-2 border border-gray-200 dark:border-gray-700 rounded-lg bg-white dark:bg-gray-800 text-sm font-medium text-gray-700 dark:text-gray-300 hover:bg-gray-50 dark:hover:bg-gray-700/50 transition-colors disabled:opacity-50"
            >
              {isUploading ? (
                <><Loader2 size={14} className="animate-spin mr-2" />Uploading...</>
              ) : 'Upload Logo'}
            </button>
            {settings.logoUrl && (
              <div className="mt-2 flex items-center gap-2">
                <img
                  src={settings.logoUrl}
                  alt="Logo preview"
                  className="h-8 w-auto rounded border border-gray-200 dark:border-gray-700"
                />
                <button
                  onClick={handleRemoveLogo}
                  className="text-xs text-red-500 hover:text-red-700 transition-colors"
                >
                  Remove
                </button>
              </div>
            )}
          </div>

          {/* Header Text */}
          <div>
            <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
              Invoice Header Text
            </label>
            <input
              type="text"
              value={settings.headerText}
              onChange={(e) => dispatch(updateHeaderText(e.target.value))}
              className="w-full px-3 py-2 border border-gray-200 dark:border-gray-700 bg-white dark:bg-gray-800 rounded-lg text-sm text-gray-900 dark:text-white focus:outline-none focus:ring-2 focus:ring-teal-500 focus:border-transparent"
              placeholder="TAX INVOICE"
            />
          </div>

          {/* Footer Text */}
          <div>
            <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
              Invoice Footer Text
            </label>
            <input
              type="text"
              value={settings.footerText}
              onChange={(e) => dispatch(updateFooterText(e.target.value))}
              className="w-full px-3 py-2 border border-gray-200 dark:border-gray-700 bg-white dark:bg-gray-800 rounded-lg text-sm text-gray-900 dark:text-white focus:outline-none focus:ring-2 focus:ring-teal-500 focus:border-transparent"
              placeholder="Thank you for your business!"
            />
          </div>
        </div>
      </section>

      {/* Content Toggles */}
      <section className="glass-card p-6">
        <div className="flex items-center space-x-3 mb-6 border-b border-gray-100 dark:border-gray-800 pb-4">
          <div className="p-2 rounded-lg bg-blue-50 dark:bg-blue-900/30 text-blue-600 dark:text-blue-400">
            <ToggleRight size={20} />
          </div>
          <div>
            <h2 className="text-lg font-bold text-gray-900 dark:text-white">
              Content Toggles
            </h2>
            <p className="text-sm text-gray-500 dark:text-gray-400">
              Show or hide specific sections.
            </p>
          </div>
        </div>

        <div className="space-y-4">
          <button
            onClick={() => dispatch(toggleGstBreakdown())}
            className="w-full flex justify-between items-center p-4 rounded-lg border border-gray-200 dark:border-gray-700 hover:bg-gray-50 dark:hover:bg-gray-800/50 transition-colors"
          >
            <span className="text-sm font-medium text-gray-900 dark:text-white">
              Show GST Breakdown
            </span>
            {settings.showGstBreakdown ? (
              <ToggleRight size={20} className="text-teal-500" />
            ) : (
              <ToggleLeft size={20} className="text-gray-400" />
            )}
          </button>

          <button
            onClick={() => dispatch(toggleBankDetails())}
            className="w-full flex justify-between items-center p-4 rounded-lg border border-gray-200 dark:border-gray-700 hover:bg-gray-50 dark:hover:bg-gray-800/50 transition-colors"
          >
            <span className="text-sm font-medium text-gray-900 dark:text-white">
              Show Bank Details on Invoice
            </span>
            {settings.showBankDetails ? (
              <ToggleRight size={20} className="text-teal-500" />
            ) : (
              <ToggleLeft size={20} className="text-gray-400" />
            )}
          </button>
        </div>

        {/* Conditional Bank Details Section */}
        {settings.showBankDetails && (
          <div className="mt-6 space-y-4 border-t border-gray-100 dark:border-gray-800 pt-6">
            <h3 className="text-sm font-semibold text-gray-700 dark:text-gray-300">
              Bank & Payment Details
            </h3>

            <div>
              <label className="block text-xs font-medium text-gray-500 dark:text-gray-400 mb-1">
                UPI ID
              </label>
              <input
                type="text"
                value={settings.upiId}
                onChange={(e) => dispatch(updateUpiId(e.target.value))}
                className="w-full px-3 py-2 border border-gray-200 dark:border-gray-700 bg-white dark:bg-gray-800 rounded-lg text-sm text-gray-900 dark:text-white focus:outline-none focus:ring-2 focus:ring-teal-500 focus:border-transparent"
                placeholder="yourname@upi"
              />
            </div>

            <div>
              <label className="block text-xs font-medium text-gray-500 dark:text-gray-400 mb-1">
                Bank Details
              </label>
              <textarea
                value={settings.bankDetails}
                onChange={(e) => dispatch(updateBankDetails(e.target.value))}
                rows={3}
                className="w-full px-3 py-2 border border-gray-200 dark:border-gray-700 bg-white dark:bg-gray-800 rounded-lg text-sm text-gray-900 dark:text-white focus:outline-none focus:ring-2 focus:ring-teal-500 focus:border-transparent resize-none"
                placeholder="Account Name, Account Number, IFSC Code..."
              />
            </div>
          </div>
        )}
      </section>

      {/* Terms and Conditions */}
      <section className="glass-card p-6">
        <h2 className="text-lg font-bold text-gray-900 dark:text-white mb-4">
          Terms & Conditions
        </h2>
        <p className="text-sm text-gray-500 dark:text-gray-400 mb-4">
          Custom terms printed at the bottom of each invoice.
        </p>
        <textarea
          value={settings.termsAndConditions}
          onChange={(e) => dispatch(updateTermsAndConditions(e.target.value))}
          rows={4}
          className="w-full px-3 py-2 border border-gray-200 dark:border-gray-700 bg-white dark:bg-gray-800 rounded-lg text-sm text-gray-900 dark:text-white focus:outline-none focus:ring-2 focus:ring-teal-500 focus:border-transparent resize-none"
          placeholder="Goods once sold will not be taken back..."
        />
      </section>

      {/* Live Preview */}
      <InvoicePreview
        sale={mockSale}
        cartItems={mockCartItems}
        shopName="My Store Pvt. Ltd."
        shopAddress="12 Shopper Street, Mumbai MH 400001"
        shopGstin={mockSale.businessGstin || ''}
      />
    </div>
  );
}
