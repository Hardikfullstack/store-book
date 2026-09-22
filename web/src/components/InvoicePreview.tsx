'use client';

import { useCallback, useEffect, useRef, useState } from 'react';
import { Eye, EyeOff, Download, RefreshCw, Loader2 } from 'lucide-react';
import { InvoicePdfGenerator, type PdfSaleData, type PdfCartItem } from '@/lib/InvoicePdfGenerator';
import { useSelector } from 'react-redux';
import { RootState } from '@/store';
import type { InvoiceSettingsState } from '@/store/pdfTemplateSlice';

interface InvoicePreviewProps {
  sale: PdfSaleData;
  cartItems: PdfCartItem[];
  shopName: string;
  shopAddress: string;
  shopGstin: string;
}

export default function InvoicePreview({
  sale,
  cartItems,
  shopName,
  shopAddress,
  shopGstin,
}: InvoicePreviewProps) {
  const settings = useSelector<RootState, InvoiceSettingsState>((state) => state.pdfTemplate);
  const [pdfUrl, setPdfUrl] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [isVisible, setIsVisible] = useState(true);
  const timerRef = useRef<ReturnType<typeof setTimeout> | null>(null);

  const generatePdf = useCallback(async () => {
    setIsLoading(true);
    setError(null);
    try {
      const url = await InvoicePdfGenerator.generateInvoicePdf(
        sale,
        cartItems,
        shopName,
        shopAddress,
        shopGstin,
        settings,
      );
      setPdfUrl(url);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to generate preview');
      setPdfUrl(null);
    } finally {
      setIsLoading(false);
    }
  }, [sale, cartItems, shopName, shopAddress, shopGstin, settings]);

  useEffect(() => {
    if (!isVisible) return;
    if (timerRef.current) clearTimeout(timerRef.current);
    timerRef.current = setTimeout(() => {
      generatePdf();
    }, 400);
    return () => {
      if (timerRef.current) clearTimeout(timerRef.current);
    };
  }, [generatePdf, settings, isVisible]);

  const handleDownload = () => {
    if (pdfUrl) {
      const link = document.createElement('a');
      link.href = pdfUrl;
      link.download = `${sale.type === 'ESTIMATE' ? 'Estimate' : 'Invoice'}_${sale.id.substring(0, 8)}.pdf`;
      link.click();
    }
  };

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <h3 className="text-lg font-semibold text-gray-900 dark:text-white flex items-center gap-2">
          <Eye className="w-5 h-5 text-teal-500" />
          Live Preview
        </h3>
        <div className="flex items-center gap-2">
          <button
            onClick={() => setIsVisible((v) => !v)}
            className="p-2 rounded-lg border border-gray-200 dark:border-gray-700 hover:bg-gray-50 dark:hover:bg-gray-800 text-gray-600 dark:text-gray-400 transition-colors"
            title={isVisible ? 'Hide Preview' : 'Show Preview'}
          >
            {isVisible ? <EyeOff size={16} /> : <Eye size={16} />}
          </button>
          <button
            onClick={generatePdf}
            disabled={isLoading}
            className="p-2 rounded-lg border border-teal-500 text-teal-600 dark:text-teal-400 hover:bg-teal-50 dark:hover:bg-teal-900/20 transition-colors disabled:opacity-50"
            title="Refresh Preview"
          >
            <RefreshCw size={16} className={isLoading ? 'animate-spin' : ''} />
          </button>
          <button
            onClick={handleDownload}
            disabled={!pdfUrl}
            className="p-2 rounded-lg border border-teal-500 text-teal-600 dark:text-teal-400 hover:bg-teal-50 dark:hover:bg-teal-900/20 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
            title="Download PDF"
          >
            <Download size={16} />
          </button>
        </div>
      </div>

      {!isVisible ? (
        <div className="flex items-center justify-center h-96 border border-dashed border-gray-300 dark:border-gray-700 rounded-xl bg-gray-50 dark:bg-gray-900/50">
          <p className="text-sm text-gray-400">Preview hidden</p>
        </div>
      ) : (
        <div className="border border-gray-200 dark:border-gray-700 rounded-xl overflow-hidden bg-white dark:bg-gray-900/50">
          {isLoading && !pdfUrl ? (
            <div className="flex items-center justify-center h-96 gap-2 text-gray-400">
              <Loader2 size={20} className="animate-spin" />
              <span className="text-sm">Generating preview...</span>
            </div>
          ) : error ? (
            <div className="flex items-center justify-center h-96 text-red-500 text-sm">
              {error}
            </div>
          ) : pdfUrl ? (
            <iframe
              src={pdfUrl}
              className="w-full h-[700px] bg-white"
              title="Invoice PDF Preview"
            />
          ) : (
            <div className="flex items-center justify-center h-96 text-gray-400 text-sm">
              Click refresh to generate preview
            </div>
          )}
        </div>
      )}
    </div>
  );
}
