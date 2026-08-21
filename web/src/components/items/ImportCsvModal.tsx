'use client';

import React, { useState, useRef } from 'react';
import { Upload, X, FileText, CheckCircle2, AlertTriangle, Download, Loader2 } from 'lucide-react';
import {
  parseInventoryCsv,
  downloadCsvFile,
  generateSampleInventoryTemplateCsv,
  InventoryCsvRow
} from '@/lib/csvUtils';
import { syncItem } from '@/dataconnect';
import { dataConnect } from '@/lib/firebase';
import { FormattedAmount } from '@/components/FormattedAmount';

interface ImportCsvModalProps {
  isOpen: boolean;
  onClose: () => void;
  storeId: string;
  onSuccess: (importedCount: number) => void;
}

export default function ImportCsvModal({
  isOpen,
  onClose,
  storeId,
  onSuccess
}: ImportCsvModalProps) {
  const [file, setFile] = useState<File | null>(null);
  const [isParsing, setIsParsing] = useState(false);
  const [validItems, setValidItems] = useState<InventoryCsvRow[]>([]);
  const [invalidItems, setInvalidItems] = useState<{ row: number; data: Record<string, unknown>; errors: string[] }[]>([]);
  const [activeTab, setActiveTab] = useState<'valid' | 'invalid'>('valid');
  const [isImporting, setIsImporting] = useState(false);
  const [importProgress, setImportProgress] = useState(0);
  const [isDragging, setIsDragging] = useState(false);
  const fileInputRef = useRef<HTMLInputElement>(null);

  if (!isOpen) return null;

  const handleFileChange = (selectedFile: File) => {
    if (!selectedFile.name.endsWith('.csv')) {
      alert('Please upload a valid .csv file');
      return;
    }

    setFile(selectedFile);
    setIsParsing(true);

    const reader = new FileReader();
    reader.onload = (e) => {
      try {
        const text = e.target?.result as string;
        const result = parseInventoryCsv(text);
        setValidItems(result.validItems);
        setInvalidItems(result.invalidItems);
        setActiveTab(result.validItems.length > 0 ? 'valid' : 'invalid');
      } catch (err) {
        console.error('Failed to parse CSV:', err);
        alert('Failed to parse CSV file. Please verify file format.');
      } finally {
        setIsParsing(false);
      }
    };
    reader.onerror = () => {
      alert('Error reading file.');
      setIsParsing(false);
    };
    reader.readAsText(selectedFile);
  };

  const handleDrop = (e: React.DragEvent) => {
    e.preventDefault();
    setIsDragging(false);
    if (e.dataTransfer.files && e.dataTransfer.files.length > 0) {
      handleFileChange(e.dataTransfer.files[0]);
    }
  };

  const handleDownloadSample = () => {
    const sampleCsv = generateSampleInventoryTemplateCsv();
    downloadCsvFile(sampleCsv, 'sample_inventory_template.csv');
  };

  const handleReset = () => {
    setFile(null);
    setValidItems([]);
    setInvalidItems([]);
    setImportProgress(0);
    if (fileInputRef.current) {
      fileInputRef.current.value = '';
    }
  };

  const handleExecuteImport = async () => {
    if (!storeId || validItems.length === 0) return;

    setIsImporting(true);
    setImportProgress(0);

    let successCount = 0;
    const now = Math.floor(Date.now() / 1000);

    for (let i = 0; i < validItems.length; i++) {
      const item = validItems[i];
      try {
        const itemId = crypto.randomUUID();
        await syncItem(dataConnect, {
          id: itemId,
          storeId,
          name: item.name,
          quantity: item.quantity,
          unit: item.unit,
          buyPrice: item.buyPrice,
          sellPrice: item.sellPrice,
          lowStockThreshold: item.lowStockThreshold,
          category: item.category || 'General',
          photoPath: '',
          barcode: '',
          hsnCode: item.hsnCode || '',
          taxRate: item.taxRate || 0,
          batchLotNumber: '',
          expiryDate: '',
          isDeleted: false,
          updatedAt: now
        });
        successCount++;
      } catch (err) {
        console.error(`Failed to import item "${item.name}":`, err);
      }
      setImportProgress(Math.round(((i + 1) / validItems.length) * 100));
    }

    setIsImporting(false);
    onSuccess(successCount);
    handleReset();
    onClose();
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/60 backdrop-blur-sm">
      <div className="bg-white dark:bg-gray-800 rounded-2xl max-w-3xl w-full shadow-2xl border border-gray-200 dark:border-gray-700 overflow-hidden flex flex-col max-h-[90vh]">
        {/* Header */}
        <div className="px-6 py-4 border-b border-gray-100 dark:border-gray-700 bg-gray-50/50 dark:bg-gray-900/50 flex items-center justify-between">
          <div className="flex items-center space-x-3">
            <div className="p-2 bg-teal-50 dark:bg-teal-950/30 text-teal-600 dark:text-teal-400 rounded-lg">
              <Upload size={20} />
            </div>
            <div>
              <h3 className="text-lg font-bold text-gray-900 dark:text-white">
                Import Inventory CSV
              </h3>
              <p className="text-xs text-gray-500 dark:text-gray-400">
                Bulk upload products and stock levels from a spreadsheet.
              </p>
            </div>
          </div>
          <button
            type="button"
            onClick={onClose}
            disabled={isImporting}
            className="text-gray-400 hover:text-gray-600 dark:hover:text-gray-200 transition-colors p-1"
          >
            <X size={20} />
          </button>
        </div>

        {/* Modal Body */}
        <div className="flex-1 overflow-y-auto p-6 space-y-6">
          {/* Upload Zone & Sample Template */}
          {!file && (
            <div className="space-y-4">
              <div
                onDragOver={(e) => { e.preventDefault(); setIsDragging(true); }}
                onDragLeave={() => setIsDragging(false)}
                onDrop={handleDrop}
                onClick={() => fileInputRef.current?.click()}
                className={`border-2 border-dashed rounded-xl p-8 text-center cursor-pointer transition-all ${
                  isDragging
                    ? 'border-teal-500 bg-teal-50/50 dark:bg-teal-950/20'
                    : 'border-gray-300 dark:border-gray-600 hover:border-teal-400 dark:hover:border-teal-500 bg-gray-50/30 dark:bg-gray-800/50'
                }`}
              >
                <input
                  ref={fileInputRef}
                  type="file"
                  accept=".csv"
                  className="hidden"
                  onChange={(e) => {
                    if (e.target.files && e.target.files.length > 0) {
                      handleFileChange(e.target.files[0]);
                    }
                  }}
                />
                <div className="mx-auto w-12 h-12 mb-3 rounded-full bg-teal-100 dark:bg-teal-900/40 text-teal-600 dark:text-teal-400 flex items-center justify-center">
                  <FileText size={24} />
                </div>
                <p className="text-sm font-semibold text-gray-800 dark:text-gray-200">
                  Click to upload or drag & drop CSV file
                </p>
                <p className="text-xs text-gray-500 dark:text-gray-400 mt-1">
                  Supports standard inventory CSV format (.csv)
                </p>
              </div>

              {/* Sample Template Helper */}
              <div className="flex items-center justify-between p-4 bg-teal-50/60 dark:bg-teal-950/20 border border-teal-100 dark:border-teal-900/40 rounded-xl text-xs">
                <div className="text-teal-900 dark:text-teal-200 font-medium">
                  Need the exact column format? Download our pre-built template.
                </div>
                <button
                  type="button"
                  onClick={handleDownloadSample}
                  className="flex items-center space-x-1.5 px-3 py-1.5 bg-teal-600 hover:bg-teal-700 text-white rounded-lg font-medium transition-colors shadow-sm"
                >
                  <Download size={14} />
                  <span>Download Sample CSV</span>
                </button>
              </div>
            </div>
          )}

          {/* Loading / Parsing State */}
          {isParsing && (
            <div className="py-12 flex flex-col items-center justify-center space-y-3">
              <Loader2 className="w-8 h-8 text-teal-600 animate-spin" />
              <p className="text-sm text-gray-600 dark:text-gray-300">Parsing and validating CSV data...</p>
            </div>
          )}

          {/* Parsed Results & Preview Table */}
          {file && !isParsing && (
            <div className="space-y-4">
              {/* File Info Bar */}
              <div className="flex items-center justify-between p-3 bg-gray-50 dark:bg-gray-900/60 border border-gray-200 dark:border-gray-700 rounded-xl">
                <div className="flex items-center space-x-2.5 text-sm font-medium text-gray-800 dark:text-gray-200">
                  <FileText size={18} className="text-teal-600 dark:text-teal-400" />
                  <span>{file.name}</span>
                  <span className="text-xs text-gray-400">({(file.size / 1024).toFixed(1)} KB)</span>
                </div>
                <button
                  type="button"
                  onClick={handleReset}
                  disabled={isImporting}
                  className="text-xs text-red-500 hover:text-red-700 font-medium px-2 py-1"
                >
                  Change File
                </button>
              </div>

              {/* Validation Summary Badges */}
              <div className="flex space-x-3">
                <button
                  type="button"
                  onClick={() => setActiveTab('valid')}
                  className={`flex-1 py-2 px-3 rounded-lg border text-sm font-semibold flex items-center justify-center space-x-2 transition-all ${
                    activeTab === 'valid'
                      ? 'border-teal-500 bg-teal-50 dark:bg-teal-950/30 text-teal-700 dark:text-teal-300 ring-2 ring-teal-500/20'
                      : 'border-gray-200 dark:border-gray-700 text-gray-600 dark:text-gray-400 hover:bg-gray-50 dark:hover:bg-gray-800'
                  }`}
                >
                  <CheckCircle2 size={16} className="text-teal-600 dark:text-teal-400" />
                  <span>Valid Items ({validItems.length})</span>
                </button>

                <button
                  type="button"
                  onClick={() => setActiveTab('invalid')}
                  className={`flex-1 py-2 px-3 rounded-lg border text-sm font-semibold flex items-center justify-center space-x-2 transition-all ${
                    activeTab === 'invalid'
                      ? 'border-red-500 bg-red-50 dark:bg-red-950/30 text-red-700 dark:text-red-300 ring-2 ring-red-500/20'
                      : 'border-gray-200 dark:border-gray-700 text-gray-600 dark:text-gray-400 hover:bg-gray-50 dark:hover:bg-gray-800'
                  }`}
                >
                  <AlertTriangle size={16} className="text-red-500" />
                  <span>Invalid Rows ({invalidItems.length})</span>
                </button>
              </div>

              {/* Active Tab Preview */}
              {activeTab === 'valid' && (
                <div className="border border-gray-200 dark:border-gray-700 rounded-xl overflow-hidden max-h-60 overflow-y-auto">
                  {validItems.length === 0 ? (
                    <div className="p-8 text-center text-sm text-gray-500 dark:text-gray-400">
                      No valid items found in this file. Please check the format.
                    </div>
                  ) : (
                    <table className="w-full text-left text-xs">
                      <thead className="bg-gray-50 dark:bg-gray-900 border-b border-gray-200 dark:border-gray-700 text-gray-500 dark:text-gray-400 font-semibold uppercase sticky top-0">
                        <tr>
                          <th className="py-2.5 px-3">Item Name</th>
                          <th className="py-2.5 px-3 text-center">Stock</th>
                          <th className="py-2.5 px-3 text-center">Unit</th>
                          <th className="py-2.5 px-3 text-right">Buy Price</th>
                          <th className="py-2.5 px-3 text-right">Sell Price</th>
                          <th className="py-2.5 px-3 text-left">Category</th>
                        </tr>
                      </thead>
                      <tbody className="divide-y divide-gray-100 dark:divide-gray-700">
                        {validItems.map((item, idx) => (
                          <tr key={idx} className="hover:bg-gray-50/50 dark:hover:bg-gray-800/50">
                            <td className="py-2 px-3 font-medium text-gray-900 dark:text-white">
                              {item.name}
                            </td>
                            <td className="py-2 px-3 text-center text-gray-700 dark:text-gray-300">
                              {item.quantity}
                            </td>
                            <td className="py-2 px-3 text-center text-gray-500 dark:text-gray-400">
                              {item.unit}
                            </td>
                            <td className="py-2 px-3 text-right text-gray-700 dark:text-gray-300">
                              <FormattedAmount amount={item.buyPrice} />
                            </td>
                            <td className="py-2 px-3 text-right text-gray-900 dark:text-white font-medium">
                              <FormattedAmount amount={item.sellPrice} />
                            </td>
                            <td className="py-2 px-3 text-gray-500 dark:text-gray-400">
                              {item.category || '-'}
                            </td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  )}
                </div>
              )}

              {activeTab === 'invalid' && (
                <div className="border border-red-200 dark:border-red-900/40 rounded-xl overflow-hidden max-h-60 overflow-y-auto bg-red-50/10">
                  {invalidItems.length === 0 ? (
                    <div className="p-8 text-center text-sm text-green-600 dark:text-green-400 font-medium">
                      ✓ Great! All rows in this CSV file are valid and ready to import.
                    </div>
                  ) : (
                    <div className="divide-y divide-red-100 dark:divide-red-900/30 text-xs">
                      {invalidItems.map((inv, idx) => (
                        <div key={idx} className="p-3 flex items-start justify-between">
                          <div>
                            <span className="font-bold text-red-600 dark:text-red-400 mr-2">
                              Row {inv.row}:
                            </span>
                            <span className="font-medium text-gray-800 dark:text-gray-200">
                              {inv.data.name as string}
                            </span>
                            <ul className="mt-1 list-disc list-inside text-red-500 dark:text-red-400 space-y-0.5">
                              {inv.errors.map((err, errIdx) => (
                                <li key={errIdx}>{err}</li>
                              ))}
                            </ul>
                          </div>
                        </div>
                      ))}
                    </div>
                  )}
                </div>
              )}

              {/* Progress Bar when importing */}
              {isImporting && (
                <div className="space-y-1.5 pt-2">
                  <div className="flex justify-between text-xs font-semibold text-gray-700 dark:text-gray-300">
                    <span>Importing items to database...</span>
                    <span>{importProgress}%</span>
                  </div>
                  <div className="w-full bg-gray-200 dark:bg-gray-700 rounded-full h-2 overflow-hidden">
                    <div
                      className="bg-teal-500 h-2 rounded-full transition-all duration-200"
                      style={{ width: `${importProgress}%` }}
                    />
                  </div>
                </div>
              )}
            </div>
          )}
        </div>

        {/* Footer */}
        <div className="px-6 py-4 border-t border-gray-100 dark:border-gray-700 bg-gray-50/50 dark:bg-gray-900/50 flex items-center justify-between">
          <button
            type="button"
            onClick={onClose}
            disabled={isImporting}
            className="px-4 py-2 text-sm font-medium text-gray-600 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-800 rounded-lg transition-colors"
          >
            Cancel
          </button>

          {file && (
            <button
              type="button"
              onClick={handleExecuteImport}
              disabled={isImporting || validItems.length === 0}
              className="px-5 py-2 text-sm font-semibold text-white bg-teal-600 hover:bg-teal-700 disabled:opacity-50 disabled:cursor-not-allowed rounded-lg transition-colors flex items-center space-x-2 shadow-sm"
            >
              {isImporting ? (
                <>
                  <Loader2 className="w-4 h-4 animate-spin" />
                  <span>Importing ({validItems.length})...</span>
                </>
              ) : (
                <>
                  <Upload size={16} />
                  <span>Import {validItems.length} Valid Items</span>
                </>
              )}
            </button>
          )}
        </div>
      </div>
    </div>
  );
}
