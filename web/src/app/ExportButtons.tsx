'use client';

import { useState } from 'react';
import { Download, FileText, Loader2, Upload } from 'lucide-react';
import jsPDF from 'jspdf';
import autoTable from 'jspdf-autotable';
import { generateInventoryCsv, downloadCsvFile } from '@/lib/csvUtils';

interface ExportButtonsProps {
  data: any[];
  type: string;
  columns: string[];
  onExportAll?: () => Promise<any[]>;
  onImport?: () => void;
}

export default function ExportButtons({
  data,
  type,
  columns,
  onExportAll,
  onImport
}: ExportButtonsProps) {
  const [isExporting, setIsExporting] = useState(false);

  const exportCSV = async () => {
    setIsExporting(true);
    try {
      let exportData = data;
      if (onExportAll) {
        exportData = await onExportAll();
      }

      if (exportData.length === 0) {
        alert('No data available to export.');
        return;
      }

      if (type === 'items') {
        const csvContent = generateInventoryCsv(exportData);
        downloadCsvFile(csvContent, `inventory_export_${new Date().toISOString().slice(0, 10).replace(/-/g, '')}.csv`);
      } else {
        const header = columns.join(',');
        const rows = exportData.map(row => columns.map(col => `"${row[col] || ''}"`).join(','));
        const csvContent = [header, ...rows].join('\n');
        downloadCsvFile(csvContent, `${type}_export_${Date.now()}.csv`);
      }
    } catch (e) {
      console.error('Export CSV error:', e);
      alert('Failed to export CSV. Please try again.');
    } finally {
      setIsExporting(false);
    }
  };

  const exportPDF = async () => {
    setIsExporting(true);
    try {
      let exportData = data;
      if (onExportAll) {
        exportData = await onExportAll();
      }

      if (exportData.length === 0) {
        alert('No data available to export.');
        return;
      }

      const doc = new jsPDF();
      doc.setFontSize(18);
      doc.text(`StoreBook - ${type.toUpperCase()} Report`, 14, 22);

      const tableData = exportData.map(row => columns.map(col => String(row[col] || '')));

      autoTable(doc, {
        startY: 30,
        head: [columns.map(c => c.toUpperCase())],
        body: tableData,
      });

      doc.save(`${type}_export_${Date.now()}.pdf`);
    } catch (e) {
      console.error('Export PDF error:', e);
      alert('Failed to export PDF. Please try again.');
    } finally {
      setIsExporting(false);
    }
  };

  const buttonStyle =
    'flex items-center px-3 py-1.5 text-sm font-medium text-gray-700 bg-white border border-gray-300 rounded-md hover:bg-gray-50 focus:outline-none shadow-sm transition-colors dark:bg-gray-800 dark:border-gray-700 dark:text-gray-300 dark:hover:bg-gray-700 disabled:opacity-50';

  return (
    <div className="flex items-center space-x-2">
      {/* 1. Import CSV Button */}
      {type === 'items' && onImport && (
        <button
          type="button"
          onClick={onImport}
          className={buttonStyle}
        >
          <Upload size={16} className="mr-1.5 text-teal-600 dark:text-teal-400" />
          <span>Import CSV</span>
        </button>
      )}

      {/* 2. Export CSV Button */}
      {type === 'items' && <button
        type="button"
        onClick={exportCSV}
        disabled={isExporting}
        className={buttonStyle}
      >
        {isExporting ? (
          <Loader2 size={16} className="mr-1.5 animate-spin text-teal-600" />
        ) : (
          <Download size={16} className="mr-1.5 text-teal-600 dark:text-teal-400" />
        )}
        <span>Export CSV</span>
      </button>}

      {/* 3. Export PDF Button */}
      <button
        type="button"
        onClick={exportPDF}
        disabled={isExporting}
        className={buttonStyle}
      >
        <FileText size={16} className="mr-1.5 text-red-500" />
        <span>PDF</span>
      </button>
    </div>
  );
}
