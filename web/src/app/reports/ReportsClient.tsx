import { sanitizeInput } from '@/lib/sanitize';
'use client';

import { useState, useEffect } from 'react';
import { Download, FileText, Calendar, Filter, Loader2 } from 'lucide-react';
import { dataConnect } from '@/lib/firebase';
import { getActiveSales } from '@/dataconnect';

export default function ReportsClient({ storeId }: { storeId: string }) {
  const [sales, setSales] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    let isMounted = true;
    const fetchSales = async () => {
      try {
        const res = await getActiveSales(dataConnect, { storeId });
        if (isMounted) setSales(res.data.sales);
      } catch (err) {
        console.error(err);
      } finally {
        if (isMounted) setLoading(false);
      }
    };
    fetchSales();
    return () => { isMounted = false; };
  }, [storeId]);

  const [dateRange, setDateRange] = useState<'all' | 'month' | 'quarter' | 'year'>('month');

  const generateGSTR1 = () => {
    // Generate CSV
    const headers = ['Invoice ID', 'Date', 'Customer Name', 'Total Amount', 'Taxable Amount', 'Tax Rate', 'Tax Amount'];
    
    const now = Date.now();
    let cutoff = 0;
    if (dateRange === 'month') cutoff = now - 30 * 24 * 60 * 60 * 1000;
    if (dateRange === 'quarter') cutoff = now - 90 * 24 * 60 * 60 * 1000;
    if (dateRange === 'year') cutoff = now - 365 * 24 * 60 * 60 * 1000;

    const filteredSales = sales.filter(sale => ((sale.timestamp || sale.updatedAt) * 1000) >= cutoff);

    const rows = filteredSales.map(sale => {
      // In a real app, we'd use actual tax rates. We'll simulate 18% GST for the demo.
      const total = sale.totalAmount || 0;
      const taxable = total / 1.18;
      const tax = total - taxable;
      const date = new Date(sale.timestamp || (sale.updatedAt * 1000)).toLocaleDateString('en-IN');
      
      return [
        sale.id.substring(0, 8),
        date,
        sale.customerName || 'Walk-in',
        total.toFixed(2),
        taxable.toFixed(2),
        '18%',
        tax.toFixed(2)
      ].join(',');
    });

    const csvContent = "data:text/csv;charset=utf-8," + headers.join(',') + "\n" + rows.join('\n');
    const encodedUri = encodeURI(csvContent);
    const link = document.createElement("a");
    link.setAttribute("href", encodedUri);
    link.setAttribute("download", `GSTR1_Report_${dateRange}.csv`);
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  };

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center mb-6">
        <div>
          <h1 className="text-2xl font-bold text-gray-900 dark:text-white">Financial Reports</h1>
          <p className="text-gray-500 dark:text-gray-400 text-sm mt-1">Generate and export compliance reports.</p>
        </div>
        <select 
          value={dateRange}
          onChange={(e) => setDateRange(sanitizeInput(e.target.value) as any)}
          className="px-4 py-2 border border-gray-200 dark:border-gray-700 bg-white dark:bg-gray-800 rounded-lg text-sm"
        >
          <option value="month">Current Month</option>
          <option value="quarter">Last Quarter</option>
          <option value="year">Financial Year</option>
          <option value="all">All Time</option>
        </select>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        {/* GSTR-1 Card */}
        <div className="glass-card p-6">
          <div className="flex items-center space-x-3 mb-4">
            <div className="p-3 bg-teal-100 dark:bg-teal-900/30 text-teal-600 dark:text-teal-400 rounded-xl">
              <FileText size={24} />
            </div>
            <div>
              <h2 className="text-lg font-bold text-gray-900 dark:text-white">GSTR-1 Export</h2>
              <p className="text-sm text-gray-500 dark:text-gray-400">Outward supplies report</p>
            </div>
          </div>
          <p className="text-sm text-gray-600 dark:text-gray-300 mb-6">
            Generate a CSV file formatted for direct import into Tally or other CA software. Contains all B2B and B2C sales.
          </p>
          <button 
            onClick={generateGSTR1}
            disabled={loading}
            className="w-full btn-primary flex justify-center items-center space-x-2 py-3"
          >
            {loading ? <Loader2 size={18} className="animate-spin" /> : <Download size={18} />}
            <span>Download GSTR-1 (CSV)</span>
          </button>
        </div>

        {/* Other Reports Card */}
        <div className="glass-card p-6 opacity-60">
          <div className="flex items-center space-x-3 mb-4">
            <div className="p-3 bg-purple-100 dark:bg-purple-900/30 text-purple-600 dark:text-purple-400 rounded-xl">
              <FileText size={24} />
            </div>
            <div>
              <h2 className="text-lg font-bold text-gray-900 dark:text-white">GSTR-2 Export</h2>
              <p className="text-sm text-gray-500 dark:text-gray-400">Inward supplies (Coming Soon)</p>
            </div>
          </div>
          <p className="text-sm text-gray-600 dark:text-gray-300 mb-6">
            Track all purchases and expenses. This feature is currently in development and will be available in a future update.
          </p>
          <button disabled className="w-full px-4 py-3 bg-gray-200 dark:bg-gray-800 text-gray-400 rounded-xl font-medium cursor-not-allowed">
            Coming Soon
          </button>
        </div>
      </div>
    </div>
  );
}
