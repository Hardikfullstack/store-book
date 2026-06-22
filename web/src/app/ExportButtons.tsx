'use client';

import { Download, FileText } from 'lucide-react';
import jsPDF from 'jspdf';
import autoTable from 'jspdf-autotable';

export default function ExportButtons({ data, type, columns }: { data: any[], type: string, columns: string[] }) {
  
  const exportCSV = () => {
    if (data.length === 0) return;
    const header = columns.join(',');
    const rows = data.map(row => columns.map(col => `"${row[col] || ''}"`).join(','));
    const csvContent = [header, ...rows].join('\n');
    
    const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.setAttribute('download', `${type}_export_${Date.now()}.csv`);
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  };

  const exportPDF = () => {
    if (data.length === 0) return;
    const doc = new jsPDF();
    
    doc.setFontSize(18);
    doc.text(`StoreBook - ${type.toUpperCase()} Report`, 14, 22);
    
    const tableData = data.map(row => columns.map(col => String(row[col] || '')));
    
    autoTable(doc, {
      startY: 30,
      head: [columns.map(c => c.toUpperCase())],
      body: tableData,
    });
    
    doc.save(`${type}_export_${Date.now()}.pdf`);
  };

  return (
    <div className="flex space-x-2">
      <button
        onClick={exportCSV}
        className="flex items-center px-3 py-1.5 text-sm font-medium text-gray-700 bg-white border border-gray-300 rounded-md hover:bg-gray-50 focus:outline-none shadow-sm transition-colors dark:bg-gray-800 dark:border-gray-700 dark:text-gray-300 dark:hover:bg-gray-700"
      >
        <Download size={16} className="mr-1.5" />
        CSV
      </button>
      <button
        onClick={exportPDF}
        className="flex items-center px-3 py-1.5 text-sm font-medium text-gray-700 bg-white border border-gray-300 rounded-md hover:bg-gray-50 focus:outline-none shadow-sm transition-colors dark:bg-gray-800 dark:border-gray-700 dark:text-gray-300 dark:hover:bg-gray-700"
      >
        <FileText size={16} className="mr-1.5 text-red-500" />
        PDF
      </button>
    </div>
  );
}
