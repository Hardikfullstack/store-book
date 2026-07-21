'use client';

import { useState, useEffect, useRef } from 'react';
import { Plus, Search, Trash2, Download, FileText } from 'lucide-react';
import jsPDF from 'jspdf';
import autoTable from 'jspdf-autotable';
import { dataConnect } from '@/lib/firebase';
import { executeQuery } from 'firebase/data-connect';
import { sanitizeInput } from '@/lib/sanitize';
import { getActiveSalesRef, softDeleteSale, getSalesCountRef, OrderDirection } from '@/dataconnect';
import Pagination from '@/app/components/Pagination';
import { FormattedAmount } from '@/components/FormattedAmount';
import SalesPOS from '@/app/sales/SalesPOS';
import DynamicTable, { TableColumn, TableRowAction } from '@/components/DynamicTable';
import { convertQuotationToSale } from '@/app/actions';

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
  const [minAmountFilter, setMinAmountFilter] = useState('');
  const [maxAmountFilter, setMaxAmountFilter] = useState('');
  const [startDateFilter, setStartDateFilter] = useState('');
  const [endDateFilter, setEndDateFilter] = useState('');
  // E04-S3: Status filter — 'draft' shows only ESTIMATEs, 'all' shows both
  const [statusFilter, setStatusFilter] = useState<'draft' | 'all'>('draft');

  const handleSearchChange = (value: string) => {
    setSearchQuery(value);
    if (debounceRef.current) clearTimeout(debounceRef.current);
    const trimmed = value.trim();
    if (trimmed.length === 0) { setDebouncedSearch(''); setCurrentPage(1); return; }
    if (trimmed.length < 3) return;
    debounceRef.current = setTimeout(() => { setDebouncedSearch(trimmed); setCurrentPage(1); }, 400);
  };

  const handlePageChange = (page: number) => {
    setCurrentPage(page);
    const isFiltering = !!minAmountFilter || !!maxAmountFilter || !!startDateFilter || !!endDateFilter;
    if ((debouncedSearch || isFiltering) && searchResults.length > 0) {
      const start = (page - 1) * pageSize;
      setQuotations(searchResults.slice(start, start + pageSize));
    }
  };
  const [refreshTrigger, setRefreshTrigger] = useState(0);
  const [sortField, setSortField] = useState<string | null>(null);
  const [sortDirection, setSortDirection] = useState<'asc' | 'desc'>('asc');
  const [dataVersion, setDataVersion] = useState(0);
  const fetchedPagesAtVersionRef = useRef<Map<string, number>>(new Map());
  const [debouncedSearch, setDebouncedSearch] = useState('');
  const debounceRef = useRef<ReturnType<typeof setTimeout> | null>(null);
  const [searchResults, setSearchResults] = useState<any[]>([]);
  const searchResultsKeyRef = useRef('');

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
    if (!isPremium || !storeId || debouncedSearch || minAmountFilter || maxAmountFilter || startDateFilter || endDateFilter) return;
    const countKey = `count`;
    const needsServerFetch = (fetchedPagesAtVersionRef.current.get(countKey) ?? -1) < dataVersion;
    const options = needsServerFetch ? { fetchPolicy: 'SERVER_ONLY' as const } : undefined;
    executeQuery(getSalesCountRef(dataConnect, { storeId, type: 'ESTIMATE' }), options)
      .then(res => {
        setTotalItems(res.data.sales.length);
        fetchedPagesAtVersionRef.current.set(countKey, dataVersion);
      })
      .catch(err => console.error('Count fetch error:', err));
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [isPremium, storeId, refreshTrigger, debouncedSearch, statusFilter, minAmountFilter, maxAmountFilter, startDateFilter, endDateFilter]);

  // Fetch paginated quotations
  useEffect(() => {
    if (!isPremium || !storeId) return;
    let isMounted = true;
    const fetchQuotations = async () => {
      setIsLoading(true);
      try {
        const isSearching = debouncedSearch.length >= 3;
        const isFiltering = !!minAmountFilter || !!maxAmountFilter || !!startDateFilter || !!endDateFilter;
        const needsFullFetch = isSearching || isFiltering;
        const currentSearchKey = `${debouncedSearch}-${sortField}-${sortDirection}-${minAmountFilter}-${maxAmountFilter}-${startDateFilter}-${endDateFilter}`;

        if (needsFullFetch && searchResults.length > 0 && searchResultsKeyRef.current === currentSearchKey) { setIsLoading(false); return; }

        const offset = (currentPage - 1) * pageSize;
        const pageKey = `page-${currentPage}-${sortField}-${sortDirection}-${debouncedSearch}-${minAmountFilter}-${maxAmountFilter}-${startDateFilter}-${endDateFilter}`;
        const needsServerFetch = (fetchedPagesAtVersionRef.current.get(pageKey) ?? -1) < dataVersion;
        const options = needsServerFetch ? { fetchPolicy: 'SERVER_ONLY' as const } : undefined;

        const vars: any = { storeId, type: 'ESTIMATE', ...buildSortVars(sortField, sortDirection) };
        if (minAmountFilter) vars.minAmount = Number(minAmountFilter);
        if (maxAmountFilter) vars.maxAmount = Number(maxAmountFilter);
        if (startDateFilter) vars.startDate = new Date(startDateFilter).getTime();
        if (endDateFilter) vars.endDate = new Date(endDateFilter).setHours(23, 59, 59, 999);

        if (needsFullFetch) {
          if (isSearching) vars.searchTerm = debouncedSearch;
        } else {
          vars.limit = pageSize;
          vars.offset = offset;
        }

        const response = await executeQuery(getActiveSalesRef(dataConnect, vars), options);

        if (!isMounted) return;

        fetchedPagesAtVersionRef.current.set(pageKey, dataVersion);

        const updated = response.data.sales.map((q: any) => ({
          ...q,
          is_deleted: 0,
          updated_at: q.updatedAt || Date.now(),
          customer_name: q.customerName,
          total_amount: q.totalAmount
        }));

        if (needsFullFetch) {
          searchResultsKeyRef.current = currentSearchKey;
          setSearchResults(updated);
          setTotalItems(updated.length);
          setQuotations(updated.slice(0, pageSize));
        } else {
          searchResultsKeyRef.current = '';
          setSearchResults([]);
          setQuotations(updated);
        }
      } catch (error) {
        console.error("Data Connect quotations sync error:", error);
      } finally {
        if (isMounted) setIsLoading(false);
      }
    };
    fetchQuotations();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [isPremium, storeId, currentPage, refreshTrigger, dataVersion, debouncedSearch, minAmountFilter, maxAmountFilter, startDateFilter, endDateFilter]);

  const handleDelete = async (id: string) => {
    if (confirm('Are you sure you want to delete this quotation?')) {
      try {
        await softDeleteSale(dataConnect, { id, updatedAt: Math.floor(Date.now() / 1000) });
        invalidateAllPages();
        setCurrentPage(1);
        setRefreshTrigger(prev => prev + 1);
      } catch (err) {
        console.error("Failed to delete quotation:", err);
      }
    }
  };

  const handleConvertToSale = async (quote: any) => {
    if (!confirm('Convert this estimate to a finalized Sale? This will deduct stock from inventory.')) return;
    try {
      // E04-S3: Call server action for proper stock deduction
      const result = await convertQuotationToSale(quote.id);
      if (result.success) {
        alert('Successfully converted to Sale! Inventory has been updated.');
        invalidateAllPages();
        setCurrentPage(1);
        setRefreshTrigger(prev => prev + 1);
      } else {
        alert(`Failed: ${result.error || 'Unknown error'}`);
      }
    } catch (err) {
      console.error("Failed to convert quotation:", err);
      alert('Failed to convert quotation');
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
        {/* E04-S3: Status filter tabs */}
        <div className="p-4 border-b border-gray-100 dark:border-gray-800 flex justify-between items-center bg-gray-50/50 dark:bg-gray-900/50 gap-4">
          <div className="flex space-x-1 p-1 bg-gray-100 dark:bg-gray-800 rounded-lg">
            {(['draft', 'all'] as const).map((tab) => (
              <button
                key={tab}
                onClick={() => { setStatusFilter(tab); setCurrentPage(1); }}
                className={`px-3 py-1.5 text-sm font-medium rounded-md transition-all ${statusFilter === tab
                    ? 'bg-white dark:bg-gray-700 shadow-sm text-purple-600 dark:text-purple-400'
                    : 'text-gray-500 dark:text-gray-400 hover:text-gray-700'
                  }`}
              >
                {tab === 'draft' ? 'Drafts' : 'All'}
              </button>
            ))}
          </div>
        </div>

        <div className="p-4 border-b border-gray-100 dark:border-gray-800 bg-gray-50/50 dark:bg-gray-900/50">
          <div className="flex flex-col md:flex-row gap-4 justify-between items-start md:items-center w-full">
            <div className="relative w-full md:w-64 flex-shrink-0">
              <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                <Search size={16} className="text-gray-400" />
              </div>
              <input aria-label="text"
                type="text"
                value={searchQuery}
                onChange={(e) => handleSearchChange(sanitizeInput(e.target.value))}
                className="block w-full pl-10 pr-3 py-2 border border-gray-200 dark:border-gray-700 bg-white dark:bg-gray-800 rounded-lg text-sm placeholder-gray-400 dark:placeholder-gray-500 focus:outline-none focus:ring-2 focus:ring-teal-500 focus:border-transparent transition-all dark:text-gray-100"
                placeholder="Search quotations by customer..."
              />
            </div>

            <div className="flex flex-wrap items-center gap-3">
              <div className="flex items-center gap-2 bg-white dark:bg-gray-800 border border-gray-200 dark:border-gray-700 rounded-lg px-2 py-1">
                <span className="text-xs text-gray-500 dark:text-gray-400">Date:</span>
                <input
                  type="date"
                  value={startDateFilter}
                  onChange={(e) => { setStartDateFilter(e.target.value); setCurrentPage(1); }}
                  className="w-[110px] px-1 py-1 bg-transparent border-none text-xs text-gray-900 dark:text-white outline-none focus:ring-0"
                />
                <span className="text-gray-300 dark:text-gray-600">-</span>
                <input
                  type="date"
                  value={endDateFilter}
                  onChange={(e) => { setEndDateFilter(e.target.value); setCurrentPage(1); }}
                  className="w-[110px] px-1 py-1 bg-transparent border-none text-xs text-gray-900 dark:text-white outline-none focus:ring-0"
                />
              </div>

              <div className="flex items-center gap-2 bg-white dark:bg-gray-800 border border-gray-200 dark:border-gray-700 rounded-lg px-2 py-1">
                <span className="text-xs text-gray-500 dark:text-gray-400">Total:</span>
                <input
                  type="number"
                  placeholder="Min"
                  value={minAmountFilter}
                  onChange={(e) => { setMinAmountFilter(e.target.value); setCurrentPage(1); }}
                  className="w-16 px-1 py-1 bg-transparent border-none text-xs text-gray-900 dark:text-white outline-none focus:ring-0"
                />
                <span className="text-gray-300 dark:text-gray-600">-</span>
                <input
                  type="number"
                  placeholder="Max"
                  value={maxAmountFilter}
                  onChange={(e) => { setMaxAmountFilter(e.target.value); setCurrentPage(1); }}
                  className="w-16 px-1 py-1 bg-transparent border-none text-xs text-gray-900 dark:text-white outline-none focus:ring-0"
                />
              </div>
            </div>
          </div>
        </div>

        <div className="overflow-x-auto">
          {(() => {
            // E04-S3: Expired check (30+ days old)
            const isExpired = (timestamp: number) => {
              if (!timestamp) return false;
              // eslint-disable-next-line react-hooks/purity
              const ageDays = (Date.now() - timestamp) / (1000 * 60 * 60 * 24);
              return ageDays > 30;
            };

            // E04-S3: Client-side filter for 'draft' tab (exclude expired when draft-only selected)
            const filteredQuotations = statusFilter === 'draft' && !isExpired(quotations.length > 0 ? quotations[0]?.timestamp : 0)
              ? quotations.filter(q => !isExpired(q.timestamp ?? q.updated_at))
              : quotations;

            const columns: TableColumn[] = [
              {
                key: 'id',
                label: 'Estimate ID',
                render: (value, row) => <span className="font-medium text-purple-600 dark:text-purple-400">#{`EST-${value.substring(0, 8)}`}</span>
              },
              {
                key: 'timestamp',
                label: 'Date',
                sortable: true,
                render: (value, row) => formatDate(value || row.updated_at)
              },
              {
                key: 'status',
                label: 'Status',
                render: (_v: any, row: any) => {
                  const exp = isExpired(row.timestamp ?? row.updated_at);
                  return exp ? (
                    <span className="inline-flex items-center px-2 py-0.5 rounded text-xs font-medium bg-red-100 text-red-700 dark:bg-red-900/30 dark:text-red-400">Expired</span>
                  ) : (
                    <span className="inline-flex items-center px-2 py-0.5 rounded text-xs font-medium bg-green-100 text-green-700 dark:bg-green-900/30 dark:text-green-400">Draft</span>
                  );
                }
              },
              {
                key: 'customer_name',
                label: 'Customer',
                sortable: true,
                render: (value) => <span className="font-medium text-gray-900 dark:text-gray-100">{value || 'Walk-in Customer'}</span>
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
                rows={filteredQuotations}
                isLoading={isLoading}
                emptyMessage="No estimates found."
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
          onPageChange={handlePageChange}
        />
      </div>

      {showModal && storeId && (
        <SalesPOS
          storeId={storeId}
          type="ESTIMATE"
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
