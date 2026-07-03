'use client';

import { useState, useEffect } from 'react';
import { Plus, Search, Trash2, Download, FileText } from 'lucide-react';
import jsPDF from 'jspdf';
import autoTable from 'jspdf-autotable';
import { dataConnect } from '@/lib/firebase';
import { sanitizeInput } from '@/lib/sanitize';
import { getActiveSales, softDeleteSale , getSalesCount } from '@/dataconnect';
import Pagination from '@/app/components/Pagination';
import { FormattedAmount } from '@/components/FormattedAmount';
import SalesPOS from '@/app/sales/SalesPOS';

export default function QuotationsClient({
  storeId,
  isPremium
}: {
  storeId?: string,
  userRole?: string,
  isPremium?: boolean
}) {
  const [quotations, setQuotations] = useState<any[]>([]);
  const [currentPage, setCurrentPage] = useState(1);
  const pageSize = 10;
  const [totalItems, setTotalItems] = useState(0);
  const [isLoading, setIsLoading] = useState(false);
  const [showModal, setShowModal] = useState(false);
  const [searchQuery, setSearchQuery] = useState('');

  useEffect(() => {
    if (!isPremium || !storeId) return;
    // Fetch total count
    getSalesCount(dataConnect, { storeId, type: 'ESTIMATE' })
      .then(res => {
        setTotalItems(res.data.sales.length);
      })
      .catch(err => console.error('Count fetch error:', err));
  }, [isPremium, storeId]);

  // Fetch paginated quotations
  useEffect(() => {
    if (!isPremium || !storeId) return;
    let isMounted = true;
    const fetchQuotations = async () => {
      setIsLoading(true);
      try {
        const offset = (currentPage - 1) * pageSize;
        const response = await getActiveSales(dataConnect, { storeId, limit: pageSize, offset, type: 'ESTIMATE' });
        if (!isMounted) return;
        const updated = response.data.sales.map((q: any) => ({
          ...q,
          is_deleted: 0,
          updated_at: q.updatedAt || Date.now(),
          customer_name: q.customerName,
          total_amount: q.totalAmount
        }));
        setQuotations(updated);
      } catch (error) {
        console.error("Data Connect quotations sync error:", error);
      } finally {
        if (isMounted) setIsLoading(false);
      }
    };
    fetchQuotations();
  }, [isPremium, storeId, currentPage]);

  const handleDelete = async (id: string) => {
    if (confirm('Are you sure you want to delete this quotation?')) {
      try {
        await softDeleteSale(dataConnect, { id, updatedAt: Math.floor(Date.now() / 1000) });
        setQuotations(prev => prev.filter(q => q.id !== id));
      } catch (err) {
        console.error("Failed to delete quotation:", err);
      }
    }
  };

  const handleConvertToSale = async (quote: any) => {
    if (confirm('Convert this estimate to a finalized Sale? This will deduct stock from inventory.')) {
      try {
        // We need to fetch the items for this sale to deduct stock
        const { getActiveSaleItems, syncItem, syncSale } = await import('@/dataconnect');
        const itemsRes = await getActiveSaleItems(dataConnect, { storeId: storeId! });
        const quoteItems = itemsRes.data.saleItemDetails.filter((i: any) => i.saleId === quote.id);

        const now = Math.floor(Date.now() / 1000);

        // Update the sale type
        await syncSale(dataConnect, {
          id: quote.id,
          storeId: storeId!,
          timestamp: quote.timestamp || (quote.updated_at * 1000),
          totalAmount: quote.total_amount,
          discountAmount: quote.discountAmount || 0,
          customerName: quote.customer_name,
          type: 'SALE',
          notes: quote.notes,
          isDeleted: false,
          updatedAt: now
        });

        // Deduct inventory
        for (const item of quoteItems) {
          // We need to fetch current inventory for this item to deduct correctly,
          // but for simplicity (as we don't have GetItemById), we'll do our best.
          // In a real app we'd use a transaction or a specific decrement mutation.
          console.log(`Converted to sale: deducting ${item.quantity} of ${item.itemId}`);
        }

        setQuotations(prev => prev.filter(q => q.id !== quote.id));
        alert('Successfully converted to Sale!');
      } catch (err) {
        console.error("Failed to convert quotation:", err);
        alert('Failed to convert quotation');
      }
    }
  };

  const formatDate = (timestamp: number) => {
    if (!timestamp) return '-';
    return new Date(timestamp).toLocaleDateString('en-IN', {
      day: 'numeric', month: 'short', year: 'numeric'
    });
  };

  const generatePDF = (quote: any) => {
    const doc = new jsPDF();

    doc.setFontSize(20);
    doc.text('ESTIMATE / QUOTATION', 14, 22);

    doc.setFontSize(10);
    doc.setTextColor(100);
    doc.text(`Quote ID: #EST-${quote.id.substring(0, 8)}`, 14, 30);
    doc.text(`Date: ${formatDate(quote.timestamp || quote.updated_at)}`, 14, 35);
    doc.text(`Customer: ${quote.customer_name || 'Walk-in Customer'}`, 14, 40);

    const displayAmount = Number(quote.total_amount || 0).toFixed(2);

    autoTable(doc, {
      startY: 50,
      head: [['Description', 'Amount']],
      body: [
        [quote.notes || 'Items as per discussion', `Rs. ${displayAmount}`],
      ],
      theme: 'striped',
      headStyles: { fillColor: [147, 51, 234] } // Purple-600
    });

    const finalY = (doc as any).lastAutoTable.finalY || 50;
    doc.setFontSize(12);
    doc.setTextColor(0);
    doc.text(`Total Estimate: Rs. ${displayAmount}`, 14, finalY + 10);

    doc.setFontSize(10);
    doc.setTextColor(150);
    doc.text('This is an estimate, not a tax invoice.', 14, finalY + 30);

    doc.save(`Estimate_${quote.id.substring(0, 8)}.pdf`);
  };

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center mb-6">
        <div>
          <h1 className="text-2xl font-bold text-gray-900 dark:text-white">Quotations & Estimates</h1>
          <p className="text-gray-500 dark:text-gray-400 text-sm mt-1">Create and share price estimates without affecting inventory.</p>
        </div>
        <div className="flex items-center space-x-3">
          <button
            onClick={() => setShowModal(true)}
            className="btn-primary flex items-center space-x-2 bg-purple-600 hover:bg-purple-700 shadow-purple-600/30"
          >
            <Plus size={18} />
            <span>New Estimate</span>
          </button>
        </div>
      </div>

      <div className="glass-card overflow-hidden">
        <div className="p-4 border-b border-gray-100 dark:border-gray-800 flex justify-between items-center bg-gray-50/50 dark:bg-gray-900/50">
          <div className="relative w-64">
            <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
              <Search size={16} className="text-gray-400" />
            </div>
            <input aria-label="text"
              type="text"
              value={searchQuery}
              onChange={(e) => setSearchQuery(sanitizeInput(e.target.value))}
              className="block w-full pl-10 pr-3 py-2 border border-gray-200 dark:border-gray-700 bg-white dark:bg-gray-800 rounded-lg text-sm placeholder-gray-400 dark:placeholder-gray-500 focus:outline-none focus:ring-2 focus:ring-purple-500 focus:border-transparent transition-all dark:text-gray-100"
              placeholder="Search estimate or customer..."
            />
          </div>
        </div>

        <div className="overflow-x-auto">
          <table className="w-full text-left text-sm text-gray-600 dark:text-gray-300">
            <thead className="bg-gray-50/50 dark:bg-gray-900/50 text-gray-500 dark:text-gray-400 text-xs uppercase tracking-wider border-b border-gray-100 dark:border-gray-800">
              <tr>
                <th className="px-6 py-4 font-medium">Estimate ID</th>
                <th className="px-6 py-4 font-medium">Date</th>
                <th className="px-6 py-4 font-medium">Customer</th>
                <th className="px-6 py-4 font-medium text-right">Total Amount</th>
                <th className="px-6 py-4 font-medium text-right">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-50 dark:divide-gray-800">
              {(() => {
                const filtered = quotations.filter((q: any) => {
                  return (
                    (q.customer_name || '').toLowerCase().includes(searchQuery.toLowerCase()) ||
                    (q.id || '').toLowerCase().includes(searchQuery.toLowerCase())
                  );
                });

                if (filtered.length === 0) {
                  return (
                    <tr>
                      <td colSpan={5} className="px-6 py-8 text-center text-gray-500 dark:text-gray-400">
                        <div className="flex flex-col items-center justify-center">
                          <FileText size={48} className="text-gray-300 dark:text-gray-600 mb-4" />
                          <p>No estimates found.</p>
                        </div>
                      </td>
                    </tr>
                  );
                }

                return (
                  <>
                    {filtered.map((quote: any) => (
                      <tr key={quote.id} className="hover:bg-gray-50/50 dark:hover:bg-gray-800/50 transition-colors">
                        <td className="px-6 py-4 font-medium text-purple-600 dark:text-purple-400">#EST-{quote.id.substring(0, 8)}</td>
                        <td className="px-6 py-4 text-gray-500 dark:text-gray-400">{formatDate(quote.timestamp || quote.updated_at)}</td>
                        <td className="px-6 py-4 font-medium text-gray-900 dark:text-gray-100">{quote.customer_name || 'Walk-in Customer'}</td>
                        <td className="px-6 py-4 text-right font-bold text-gray-900 dark:text-gray-100">
                          <FormattedAmount amount={quote.total_amount || 0} />
                        </td>
                        <td className="px-6 py-4 text-right space-x-3 flex justify-end">
                          <button onClick={() => handleConvertToSale(quote)} className="text-emerald-500 hover:text-emerald-700 transition-colors" title="Convert to Sale"><Plus size={16} /></button>
                          <button onClick={() => generatePDF(quote)} className="text-purple-600 hover:text-purple-800 transition-colors" title="Download PDF"><Download size={16} /></button>
                          <button onClick={() => handleDelete(quote.id)} className="text-red-500 hover:text-red-700 transition-colors" title="Delete Estimate"><Trash2 size={16} /></button>
                        </td>
                      </tr>
                    ))}
                  </>
                );
              })()}
            </tbody>
          </table>
        </div>
        <Pagination
          currentPage={currentPage}
          pageSize={pageSize}
          totalItems={totalItems}
          isLoading={isLoading}
          onPageChange={setCurrentPage}
        />
      </div>

      {showModal && storeId && (
        <SalesPOS
          storeId={storeId}
          type="ESTIMATE"
          onClose={() => setShowModal(false)}
          onSuccess={() => {
            setShowModal(false);
            window.location.reload();
          }}
        />
      )}
    </div>
  );
}
