'use client';

import { useState, useEffect, useRef } from 'react';
import { Plus, Search, Calendar, Trash2, Edit2, Loader2, ArrowDownCircle, Download } from 'lucide-react';
import { fetchMoreData } from '@/app/actions';
import Pagination from '@/app/components/Pagination';
import jsPDF from 'jspdf';
import autoTable from 'jspdf-autotable';
import ExportButtons from '@/app/ExportButtons';
import { dataConnect } from '@/lib/firebase';
import { executeQuery } from 'firebase/data-connect';
import { sanitizeInput } from '@/lib/sanitize';
import { getActiveSalesRef, syncSale, softDeleteSale, getSalesCountRef, OrderDirection } from '@/dataconnect';
import { FormattedAmount } from '@/components/FormattedAmount';
import SalesPOS from './SalesPOS';
import DynamicTable, { TableColumn, TableRowAction } from '@/components/DynamicTable';

export default function SalesClient({
  initialSales,
  userRole,
  storeId,
  isPremium
}: {
  initialSales: any[],
  userRole?: string,
  storeId?: string,
  isPremium?: boolean
}) {
  const [sales, setSales] = useState(initialSales);
  const [refreshTrigger, setRefreshTrigger] = useState(0);
  const [sortField, setSortField] = useState<string | null>(null);
  const [sortDirection, setSortDirection] = useState<'asc' | 'desc'>('asc');
  const [dataVersion, setDataVersion] = useState(0);
  const fetchedPagesAtVersionRef = useRef<Map<string, number>>(new Map());

  const buildSortVars = (sortField: string | null, direction: 'asc' | 'desc') => {
    if (!sortField) return {};
    const dir = direction === 'asc' ? OrderDirection.ASC : OrderDirection.DESC;
    return {
      orderByTimestamp: sortField === 'timestamp' ? dir : undefined,
      orderByCustomerName: sortField === 'customer_name' ? dir : undefined,
      orderByTotalAmount: sortField === 'total_amount' ? dir : undefined,
    };
  };

  const invalidateAllPages = () => {
    setDataVersion(v => v + 1);
    fetchedPagesAtVersionRef.current = new Map();
  };

  const handleSort = (field: string) => {
    invalidateAllPages();
    setCurrentPage(1);
    if (field === sortField) {
      setSortDirection((prev) => (prev === 'asc' ? 'desc' : 'asc'));
    } else {
      setSortField(field);
      setSortDirection('asc');
    }
  };

  useEffect(() => {
    if (!isPremium || !storeId) return;
    const countKey = `count`;
    const needsServerFetch = (fetchedPagesAtVersionRef.current.get(countKey) ?? -1) < dataVersion;
    const options = needsServerFetch ? { fetchPolicy: 'SERVER_ONLY' as const } : undefined;
    // Fetch total count once
    const fetchTotal = async () => {
      try {
        const resp = await executeQuery(getSalesCountRef(dataConnect, { storeId, type: 'SALE' }), options);
        setTotalItems(resp.data.sales.length);
        fetchedPagesAtVersionRef.current.set(countKey, dataVersion);
      } catch (e) {
        console.error('Count fetch error:', e);
      }
    };
    fetchTotal();
  }, [isPremium, storeId, refreshTrigger]);

  // Pagination state
  const [currentPage, setCurrentPage] = useState(1);
  const pageSize = 10;
  const [totalItems, setTotalItems] = useState(0);
  const [isLoading, setIsLoading] = useState(false);
  // Fetch paginated sales
  useEffect(() => {
    if (!isPremium || !storeId) return;
    let isMounted = true;
    const fetchPaginated = async () => {
      setIsLoading(true);
      try {
        const offset = (currentPage - 1) * pageSize;
        const pageKey = `page-${currentPage}-${sortField}-${sortDirection}`;
        const needsServerFetch = (fetchedPagesAtVersionRef.current.get(pageKey) ?? -1) < dataVersion;
        const options = needsServerFetch ? { fetchPolicy: 'SERVER_ONLY' as const } : undefined;

        const response = await executeQuery(getActiveSalesRef(dataConnect, { storeId, limit: pageSize, offset, type: 'SALE', ...buildSortVars(sortField, sortDirection) }), options);

        if (!isMounted) return;

        // Mark this page as fetched at current version
        fetchedPagesAtVersionRef.current.set(pageKey, dataVersion);

        const updated = response.data.sales
          .map((sale: any) => ({
            ...sale,
            is_deleted: 0,
            updated_at: sale.updatedAt || Date.now(),
            customer_name: sale.customerName,
            total_amount: sale.totalAmount
          }));
        setSales(updated);
      } catch (error) {
        console.error('Data Connect sales sync error:', error);
      } finally {
        if (isMounted) setIsLoading(false);
      }
    };
    fetchPaginated();
    const intervalId = setInterval(fetchPaginated, 30000);
    return () => {
      isMounted = false;
      clearInterval(intervalId);
    };
  }, [isPremium, storeId, currentPage, refreshTrigger, dataVersion]);
  const [showModal, setShowModal] = useState(false);
  const [searchQuery, setSearchQuery] = useState('');
  // Loading state handled by isLoading from pagination

  // Pagination UI replaces Load More functionality; removed loadMore handler.

  const handleDelete = async (id: string) => {
    if (confirm('Are you sure you want to delete this sale?')) {
      try {
        await softDeleteSale(dataConnect, { id, updatedAt: Math.floor(Date.now() / 1000) });
        invalidateAllPages();
        setCurrentPage(1);
        setRefreshTrigger(prev => prev + 1);
      } catch (err) {
        console.error("Failed to delete sale:", err);
      }
    }
  };

  const formatDate = (timestamp: number) => {
    if (!timestamp) return '-';
    return new Date(timestamp).toLocaleDateString('en-IN', {
      day: 'numeric', month: 'short', year: 'numeric',
      hour: '2-digit', minute: '2-digit'
    });
  };

  const generateInvoice = (sale: any) => {
    const doc = new jsPDF();

    // Header
    doc.setFontSize(20);
    doc.text('StoreBook Invoice', 14, 22);

    doc.setFontSize(10);
    doc.setTextColor(100);
    doc.text(`Invoice ID: #INV-${(sale.cloud_id || sale.id).substring(0, 8)}`, 14, 30);
    doc.text(`Date: ${formatDate(sale.timestamp || sale.updated_at)}`, 14, 35);
    doc.text(`Customer: ${sale.customer_name || 'Walk-in Customer'}`, 14, 40);

    // Format amount for invoice with 2 decimals
    const displayAmount = Number(sale.total_amount || 0).toFixed(2);

    // Table
    autoTable(doc, {
      startY: 50,
      head: [['Description', 'Amount']],
      body: [
        [sale.notes || 'Purchases', `Rs. ${displayAmount}`],
      ],
      theme: 'striped',
      headStyles: { fillColor: [13, 148, 136] } // Teal-600
    });

    // Total
    const finalY = (doc as any).lastAutoTable.finalY || 50;
    doc.setFontSize(12);
    doc.setTextColor(0);
    doc.text(`Total Amount: Rs. ${displayAmount}`, 14, finalY + 10);

    // Footer
    doc.setFontSize(10);
    doc.setTextColor(150);
    doc.text('Thank you for your business!', 14, finalY + 30);

    doc.save(`Invoice_${(sale.cloud_id || sale.id).substring(0, 8)}.pdf`);
  };

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center mb-6">
        <div>
          <h1 className="text-2xl font-bold text-gray-900 dark:text-white">Sales History</h1>
          <p className="text-gray-500 dark:text-gray-400 text-sm mt-1">View and manage all your past transactions.</p>
        </div>
        <div className="flex items-center space-x-3">
          <ExportButtons data={sales} type="sales" columns={['timestamp', 'customer_name', 'total_amount', 'notes']} />
          <button
            onClick={() => setShowModal(true)}
            className="btn-primary flex items-center space-x-2"
          >
            <Plus size={18} />
            <span>New Sale</span>
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
              className="block w-full pl-10 pr-3 py-2 border border-gray-200 dark:border-gray-700 bg-white dark:bg-gray-800 rounded-lg text-sm placeholder-gray-400 dark:placeholder-gray-500 focus:outline-none focus:ring-2 focus:ring-teal-500 focus:border-transparent transition-all dark:text-gray-100"
              placeholder="Search invoice or customer..."
            />
          </div>
        </div>

        <div className="overflow-x-auto">
          {(() => {
            const filteredSales = sales.filter((sale: any) => {
              const searchId = sale.cloud_id || sale.id;
              return (
                (sale.customer_name || '').toLowerCase().includes(searchQuery.toLowerCase()) ||
                (searchId || '').toLowerCase().includes(searchQuery.toLowerCase())
              );
            });

            const columns: TableColumn[] = [
              {
                key: 'id',
                label: 'Invoice ID',
                render: (value, row) => <span className="font-medium text-teal-600 dark:text-teal-400">#{`INV-${(row.cloud_id || value).substring(0, 8)}`}</span>
              },
              {
                key: 'timestamp',
                label: 'Date & Time',
                sortable: true,
                render: (value, row) => formatDate(value || row.updated_at)
              },
              {
                key: 'customer_name',
                label: 'Customer',
                sortable: true,
                render: (value) => <span className="font-medium text-gray-900 dark:text-gray-100">{value || 'Walk-in Customer'}</span>
              },
              {
                key: 'notes',
                label: 'Notes',
                textAlign: 'right',
                className: 'text-gray-500 dark:text-gray-400 truncate max-w-[150px]',
                render: (value) => value || '-'
              },
              {
                key: 'total_amount',
                label: 'Total Amount',
                sortable: true,
                textAlign: 'right',
                className: 'font-bold text-gray-900 dark:text-gray-100',
                render: (value) => <FormattedAmount amount={value || 0} />
              }
            ];

            const rowActions: TableRowAction[] = [
              {
                icon: <Download size={16} />,
                onClick: (sale) => generateInvoice(sale),
                className: 'text-teal-600 hover:text-teal-800 transition-colors',
                title: 'Download Invoice'
              },
              {
                icon: <Trash2 size={16} />,
                onClick: (sale) => handleDelete(sale.id),
                className: 'text-red-500 hover:text-red-700 transition-colors',
                title: 'Delete Sale'
              }
            ];

            return (
              <DynamicTable
                columns={columns}
                rows={filteredSales}
                isLoading={isLoading}
                emptyMessage="No sales found matching your search."
                rowKey="id"
                rowActions={rowActions}
                sortField={sortField || ""}
                sortDirection={sortDirection}
                onSort={handleSort}
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
          onClose={() => setShowModal(false)}
          onSuccess={() => {
            setShowModal(false);
            invalidateAllPages();
            setCurrentPage(1);
            setRefreshTrigger(prev => prev + 1);
          }}
        />
      )}
    </div>
  );
}
