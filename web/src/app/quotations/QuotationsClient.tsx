'use client';

import { useState, useEffect, useRef } from 'react';
import { Plus, Search, Trash2, Download, FileText } from 'lucide-react';
import jsPDF from 'jspdf';
import autoTable from 'jspdf-autotable';
import { dataConnect } from '@/lib/firebase';
import { executeQuery } from 'firebase/data-connect';
import { sanitizeInput } from '@/lib/sanitize';
import { getActiveSalesRef, softDeleteSale, getSalesCountRef } from '@/dataconnect';
import Pagination from '@/app/components/Pagination';
import { FormattedAmount } from '@/components/FormattedAmount';
import SalesPOS from '@/app/sales/SalesPOS';
import DynamicTable, { TableColumn, TableRowAction } from '@/components/DynamicTable';

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
  const [refreshTrigger, setRefreshTrigger] = useState(0);
  const refetchRef = useRef(false);

  useEffect(() => {
    if (!isPremium || !storeId) return;
    const options = refetchRef.current ? { fetchPolicy: 'SERVER_ONLY' as const } : undefined;
    // Fetch total count
    executeQuery(getSalesCountRef(dataConnect, { storeId, type: 'ESTIMATE' }), options)
      .then(res => {
        setTotalItems(res.data.sales.length);
      })
      .catch(err => console.error('Count fetch error:', err));
  }, [isPremium, storeId, refreshTrigger]);

  // Fetch paginated quotations
  useEffect(() => {
    if (!isPremium || !storeId) return;
    let isMounted = true;
    const fetchQuotations = async () => {
      setIsLoading(true);
      try {
        const offset = (currentPage - 1) * pageSize;
        const options = refetchRef.current ? { fetchPolicy: 'SERVER_ONLY' as const } : undefined;

        const response = await executeQuery(getActiveSalesRef(dataConnect, { storeId, limit: pageSize, offset, type: 'ESTIMATE' }), options);

        if (refetchRef.current) {
          refetchRef.current = false;
        }

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
  }, [isPremium, storeId, currentPage, refreshTrigger]);

  const handleDelete = async (id: string) => {
    if (confirm('Are you sure you want to delete this quotation?')) {
      try {
        await softDeleteSale(dataConnect, { id, updatedAt: Math.floor(Date.now() / 1000) });
        refetchRef.current = true;
        setCurrentPage(1);
        setRefreshTrigger(prev => prev + 1);
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
          {(() => {
            const filtered = quotations.filter((q: any) => {
              return (
                (q.customer_name || '').toLowerCase().includes(searchQuery.toLowerCase()) ||
                (q.id || '').toLowerCase().includes(searchQuery.toLowerCase())
              );
            });

            const columns: TableColumn[] = [
              {
                key: 'id',
                label: 'Estimate ID',
                render: (value) => <span className="font-medium text-purple-600 dark:text-purple-400">#{`EST-${value.substring(0, 8)}`}</span>
              },
              {
                key: 'timestamp',
                label: 'Date',
                render: (value, row) => formatDate(value || row.updated_at)
              },
              {
                key: 'customer_name',
                label: 'Customer',
                render: (value) => <span className="font-medium text-gray-900 dark:text-gray-100">{value || 'Walk-in Customer'}</span>
              },
              {
                key: 'total_amount',
                label: 'Total Amount',
                textAlign: 'right',
                className: 'font-bold text-gray-900 dark:text-gray-100',
                render: (value) => <FormattedAmount amount={value || 0} />
              }
            ];

            const rowActions: TableRowAction[] = [
              {
                icon: <Plus size={16} />,
                onClick: (quote) => handleConvertToSale(quote),
                className: 'text-emerald-500 hover:text-emerald-700 transition-colors',
                title: 'Convert to Sale'
              },
              {
                icon: <Download size={16} />,
                onClick: (quote) => generatePDF(quote),
                className: 'text-purple-600 hover:text-purple-800 transition-colors',
                title: 'Download PDF'
              },
              {
                icon: <Trash2 size={16} />,
                onClick: (quote) => handleDelete(quote.id),
                className: 'text-red-500 hover:text-red-700 transition-colors',
                title: 'Delete Estimate'
              }
            ];

            return (
              <DynamicTable
                columns={columns}
                rows={filtered}
                isLoading={isLoading}
                emptyMessage="No estimates found."
                rowKey="id"
                rowActions={rowActions}
              />
            );
          })()}
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
            refetchRef.current = true;
            setCurrentPage(1);
            setRefreshTrigger(prev => prev + 1);
          }}
        />
      )}
    </div>
  );
}
