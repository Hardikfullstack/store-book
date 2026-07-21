'use client';

import { useState, useEffect, useRef } from 'react';
import { Plus, Search, UserCheck, UserMinus, Trash2, Loader2, ArrowDownCircle, MessageCircle } from 'lucide-react';
import { fetchMoreData } from '@/app/actions';
import Pagination from '@/app/components/Pagination';
import { dataConnect } from '@/lib/firebase';
import { executeQuery } from 'firebase/data-connect';
import { sanitizeInput } from '@/lib/sanitize';
import { getActiveUdhaarsRef, syncUdhaar, softDeleteUdhaar, getUdhaarEntriesCountRef, OrderDirection } from '@/dataconnect';
import { FormattedAmount } from '@/components/FormattedAmount';
import { useDispatch, useSelector } from 'react-redux';
import { RootState } from '@/store';
import { setUdhaars, removeUdhaarRecord } from '@/store/udhaarSlice';
import DynamicTable, { TableColumn, TableRowAction } from '@/components/DynamicTable';

export default function UdhaarClient({
  initialUdhaar,
  storeName,
  storeId,
  isPremium
}: {
  initialUdhaar: any[],
  storeName?: string,
  storeId?: string,
  isPremium?: boolean
}) {
  const dispatch = useDispatch();
  const cachedUdhaars = useSelector((state: RootState) => state.udhaar.records);
  const lastSynced = useSelector((state: RootState) => state.udhaar.lastSynced);

  const [udhaar, setUdhaar] = useState<any[]>(cachedUdhaars.length > 0 ? cachedUdhaars : initialUdhaar);
  const [refreshTrigger, setRefreshTrigger] = useState(0);
  const [sortField, setSortField] = useState<string | null>(null);
  const [sortDirection, setSortDirection] = useState<'asc' | 'desc'>('asc');
  const [dataVersion, setDataVersion] = useState(0);
  const fetchedPagesAtVersionRef = useRef<Map<string, number>>(new Map());
  const [debouncedSearch, setDebouncedSearch] = useState('');
  const [searchResults, setSearchResults] = useState<any[]>([]);
  const [startDateFilter, setStartDateFilter] = useState('');
  const [endDateFilter, setEndDateFilter] = useState('');
  const [minAmountFilter, setMinAmountFilter] = useState('');
  const [maxAmountFilter, setMaxAmountFilter] = useState('');
  const debounceRef = useRef<ReturnType<typeof setTimeout> | null>(null);
  const searchResultsKeyRef = useRef('');

  const buildSortVars = (sortField: string | null, direction: 'asc' | 'desc') => {
    if (!sortField) return {};
    const dir = direction === 'asc' ? OrderDirection.ASC : OrderDirection.DESC;
    return {
      orderByTimestamp: sortField === 'timestamp' ? dir : undefined,
      orderByCustomerName: sortField === 'customer_name' ? dir : undefined,
      orderByType: sortField === 'type' ? dir : undefined,
      orderByAmount: sortField === 'amount' ? dir : undefined,
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

  const [currentPage, setCurrentPage] = useState(1);
  const pageSize = 10;
  const [totalItems, setTotalItems] = useState(0);
  const [isLoading, setIsLoading] = useState(false);

  // Fetch total count — skip when searching
  useEffect(() => {
    if (!isPremium || !storeId || debouncedSearch || startDateFilter || endDateFilter || minAmountFilter || maxAmountFilter) return;
    const countKey = `count`;
    const needsServerFetch = (fetchedPagesAtVersionRef.current.get(countKey) ?? -1) < dataVersion;
    const options = needsServerFetch ? { fetchPolicy: 'SERVER_ONLY' as const } : undefined;
    const fetchTotal = async () => {
      try {
        const resp = await executeQuery(getUdhaarEntriesCountRef(dataConnect, { storeId }), options);
        setTotalItems(resp.data.udhaarEntries.length);
        fetchedPagesAtVersionRef.current.set(countKey, dataVersion);
      } catch (e) {
        console.error('Count fetch error:', e);
      }
    };
    fetchTotal();
  }, [isPremium, storeId, refreshTrigger, debouncedSearch, startDateFilter, endDateFilter, minAmountFilter, maxAmountFilter, dataVersion]);

  // Fetch paginated udhaar entries
  useEffect(() => {
    if (!isPremium || !storeId) return;
    let isMounted = true;
    const fetchUdhaars = async () => {
      setIsLoading(true);
      try {
        const isSearching = debouncedSearch.length >= 3;
        const isFiltering = !!startDateFilter || !!endDateFilter || !!minAmountFilter || !!maxAmountFilter;
        const needsFullFetch = isSearching || isFiltering;
        const currentSearchKey = `${debouncedSearch}-${sortField}-${sortDirection}-${startDateFilter}-${endDateFilter}-${minAmountFilter}-${maxAmountFilter}`;

        if (needsFullFetch && searchResults.length > 0 && searchResultsKeyRef.current === currentSearchKey) { setIsLoading(false); return; }

        const offset = (currentPage - 1) * pageSize;
        const pageKey = `page-${currentPage}-${sortField}-${sortDirection}-${debouncedSearch}-${startDateFilter}-${endDateFilter}-${minAmountFilter}-${maxAmountFilter}`;
        const needsServerFetch = (fetchedPagesAtVersionRef.current.get(pageKey) ?? -1) < dataVersion;
        const options = needsServerFetch ? { fetchPolicy: 'SERVER_ONLY' as const } : undefined;

        const vars: any = { storeId, ...buildSortVars(sortField, sortDirection) };
        if (startDateFilter) vars.startDate = new Date(startDateFilter).getTime();
        if (endDateFilter) vars.endDate = new Date(endDateFilter).setHours(23, 59, 59, 999);
        if (minAmountFilter) vars.minAmount = Number(minAmountFilter);
        if (maxAmountFilter) vars.maxAmount = Number(maxAmountFilter);

        if (needsFullFetch) {
          if (isSearching) vars.searchTerm = debouncedSearch;
        } else {
          vars.limit = pageSize;
          vars.offset = offset;
        }

        const response = await executeQuery(getActiveUdhaarsRef(dataConnect, vars), options);

        if (!isMounted) return;

        fetchedPagesAtVersionRef.current.set(pageKey, dataVersion);

        const updated = response.data.udhaarEntries.map((record: any) => ({
          ...record,
          is_deleted: 0,
          updated_at: record.updatedAt || Date.now(),
          customer_name: record.customerName
        }));

        if (needsFullFetch) {
          searchResultsKeyRef.current = currentSearchKey;
          setSearchResults(updated);
          setTotalItems(updated.length);
          setUdhaar(updated.slice(0, pageSize));
        } else {
          searchResultsKeyRef.current = '';
          setSearchResults([]);
          setUdhaar(updated);
        }
        dispatch(setUdhaars(updated));
      } catch (error) {
        console.error('Data Connect udhaar sync error:', error);
      } finally {
        if (isMounted) setIsLoading(false);
      }
    };
    fetchUdhaars();
    const intervalId = setInterval(fetchUdhaars, 30000);
    return () => {
      isMounted = false;
      clearInterval(intervalId);
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [isPremium, storeId, currentPage, refreshTrigger, dataVersion, debouncedSearch, startDateFilter, endDateFilter, minAmountFilter, maxAmountFilter]);
  const [showModal, setShowModal] = useState(false);
  const [searchQuery, setSearchQuery] = useState('');

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
    const isFiltering = !!startDateFilter || !!endDateFilter || !!minAmountFilter || !!maxAmountFilter;
    if ((debouncedSearch || isFiltering) && searchResults.length > 0) {
      const start = (page - 1) * pageSize;
      setUdhaar(searchResults.slice(start, start + pageSize));
    }
  };

  const [formData, setFormData] = useState({
    customer_name: '',
    type: 'given',
    amount: 0,
    notes: ''
  });

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      const id = crypto.randomUUID();
      const now = Date.now();
      await syncUdhaar(dataConnect, {
        id,
        storeId: storeId as string,
        customerName: formData.customer_name,
        amount: formData.amount,
        type: formData.type,
        timestamp: now,
        notes: formData.notes,
        isDeleted: false,
        updatedAt: Math.floor(now / 1000)
      });
      setShowModal(false);
      invalidateAllPages();
      setCurrentPage(1);
      setRefreshTrigger(prev => prev + 1);
    } catch (err) {
      console.error("Failed to save udhaar:", err);
    }
  };

  const handleDelete = async (id: string) => {
    if (confirm('Are you sure you want to delete this record?')) {
      try {
        await softDeleteUdhaar(dataConnect, { id, updatedAt: Math.floor(Date.now() / 1000) });
        invalidateAllPages();
        setCurrentPage(1);
        setRefreshTrigger(prev => prev + 1);
      } catch (err) {
        console.error("Failed to delete udhaar:", err);
      }
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center mb-6">
        <div>
          <h1 className="text-2xl font-bold text-gray-900 dark:text-white">Udhaar Ledger</h1>
          <p className="text-gray-500 dark:text-gray-400 text-sm mt-1">Track credit given and taken.</p>
        </div>
        <button
          onClick={() => { setFormData({ customer_name: '', type: 'given', amount: 0, notes: '' }); setShowModal(true); }}
          className="btn-primary flex items-center space-x-2 bg-blue-600 hover:bg-blue-700 shadow-blue-600/30"
        >
          <Plus size={18} />
          <span>New Entry</span>
        </button>
      </div>

      <div className="glass-card overflow-hidden">
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
                className="block w-full pl-10 pr-3 py-2 border border-gray-200 dark:border-gray-700 bg-white dark:bg-gray-800 rounded-lg text-sm placeholder-gray-400 dark:placeholder-gray-500 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all dark:text-gray-100"
                placeholder="Search customer or notes..."
              />
            </div>

            <div className="flex flex-wrap items-center gap-3">
              <div className="flex items-center gap-2 bg-white dark:bg-gray-800 border border-gray-200 dark:border-gray-700 rounded-lg px-2 py-1">
                <span className="text-xs text-gray-500 dark:text-gray-400 mr-1">Amount:</span>
                <input
                  type="number"
                  placeholder="Min"
                  value={minAmountFilter}
                  onChange={(e) => { setMinAmountFilter(e.target.value); setCurrentPage(1); }}
                  className="w-[70px] px-1 py-1 bg-transparent border-none text-xs text-gray-900 dark:text-white outline-none focus:ring-0 placeholder-gray-400"
                />
                <span className="text-gray-300 dark:text-gray-600">-</span>
                <input
                  type="number"
                  placeholder="Max"
                  value={maxAmountFilter}
                  onChange={(e) => { setMaxAmountFilter(e.target.value); setCurrentPage(1); }}
                  className="w-[70px] px-1 py-1 bg-transparent border-none text-xs text-gray-900 dark:text-white outline-none focus:ring-0 placeholder-gray-400"
                />
              </div>
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
            </div>
          </div>
        </div>

        <div className="overflow-x-auto">
          {(() => {
            const columns: TableColumn[] = [
              {
                key: 'timestamp',
                label: 'Date',
                sortable: true,
                render: (value, row) => new Date(value || row.updated_at).toLocaleDateString('en-IN')
              },
              {
                key: 'customer_name',
                label: 'Customer Name',
                sortable: true,
                render: (value) => <span className="font-medium text-gray-900 dark:text-gray-100">{value}</span>
              },
              {
                key: 'type',
                label: 'Type',
                sortable: true,
                render: (value) => {
                  if (value?.toLowerCase() === 'given') {
                    return (
                      <span className="inline-flex items-center px-2 py-1 rounded-md text-xs font-medium bg-red-50 dark:bg-red-900/30 text-red-700 dark:text-red-400">
                        <UserMinus size={14} className="mr-1" /> Given
                      </span>
                    );
                  }
                  return (
                    <span className="inline-flex items-center px-2 py-1 rounded-md text-xs font-medium bg-teal-50 dark:bg-teal-900/30 text-teal-700 dark:text-teal-400">
                      <UserCheck size={14} className="mr-1" /> Received
                    </span>
                  );
                }
              },
              {
                key: 'notes',
                label: 'Notes',
                className: 'text-gray-500 dark:text-gray-400 truncate max-w-xs',
                render: (value) => value || '-'
              },
              {
                key: 'amount',
                label: 'Amount',
                sortable: true,
                textAlign: 'right',
                render: (value, row) => (
                  <span className={`font-bold ${row.type?.toLowerCase() === 'given' ? 'text-red-600 dark:text-red-400' : 'text-teal-600 dark:text-teal-400'}`}>
                    <FormattedAmount amount={value} />
                  </span>
                )
              }
            ];

            const rowActions: TableRowAction[] = [
              {
                label: 'Remind',
                icon: <MessageCircle size={14} />,
                onClick: (record) => {
                  const text = encodeURIComponent(storeName ? `Hi ${record.customer_name}, this is a reminder regarding your pending Udhaar balance of Rs. ${record.amount} at ${storeName}. Please clear your dues at the earliest. Thank you!` : `Hi ${record.customer_name}, this is a reminder regarding your pending Udhaar balance of Rs. ${record.amount}. Please clear your dues at the earliest. Thank you!`);
                  window.open(`https://wa.me/?text=${text}`, '_blank');
                },
                className: 'inline-flex items-center space-x-1 bg-green-100 dark:bg-green-900/30 text-green-700 dark:text-green-400 hover:bg-green-200 dark:hover:bg-green-900/50 px-2.5 py-1.5 rounded-lg font-medium text-xs transition-colors border border-green-200 dark:border-green-800',
                title: 'Send WhatsApp Reminder'
              },
              {
                icon: <Trash2 size={16} />,
                onClick: (record) => handleDelete(record.id),
                className: 'p-2 text-red-500 hover:bg-red-50 dark:hover:bg-red-900/30 rounded-lg transition-colors',
                title: 'Delete Record'
              }
            ];

            return (
              <DynamicTable
                columns={columns}
                rows={udhaar}
                isLoading={isLoading}
                emptyMessage="No udhaar records found matching your search."
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

      {showModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 backdrop-blur-sm">
          <div className="bg-white dark:bg-gray-900 rounded-xl shadow-xl w-full max-w-md p-6">
            <h2 className="text-xl font-bold mb-4 dark:text-white">New Udhaar</h2>
            <form onSubmit={handleSubmit} className="space-y-4">
              <div>
                <label className="block text-sm font-medium dark:text-gray-300">Customer Name</label>
                <input aria-label="text" required type="text" value={formData.customer_name} onChange={e => setFormData({ ...formData, customer_name: sanitizeInput(e.target.value) })} className="mt-1 w-full p-2 border dark:border-gray-700 rounded dark:bg-gray-800 dark:text-white" />
              </div>
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-medium dark:text-gray-300">Type</label>
                  <select value={formData.type} onChange={e => setFormData({ ...formData, type: sanitizeInput(e.target.value) })} className="mt-1 w-full p-2 border dark:border-gray-700 rounded dark:bg-gray-800 dark:text-white">
                    <option value="given">Given</option>
                    <option value="received">Received</option>
                  </select>
                </div>
                <div>
                  <label className="block text-sm font-medium dark:text-gray-300">Amount</label>
                  <input aria-label="number" required type="number" step="any" value={formData.amount} onChange={e => setFormData({ ...formData, amount: parseFloat(e.target.value) })} className="mt-1 w-full p-2 border dark:border-gray-700 rounded dark:bg-gray-800 dark:text-white" />
                </div>
              </div>
              <div>
                <label className="block text-sm font-medium dark:text-gray-300">Notes</label>
                <input aria-label="text" type="text" value={formData.notes} onChange={e => setFormData({ ...formData, notes: sanitizeInput(e.target.value) })} className="mt-1 w-full p-2 border dark:border-gray-700 rounded dark:bg-gray-800 dark:text-white" />
              </div>
              <div className="flex justify-end space-x-3 mt-6">
                <button type="button" onClick={() => setShowModal(false)} className="px-4 py-2 text-gray-500 hover:text-gray-700 dark:text-gray-400 dark:hover:text-gray-200">Cancel</button>
                <button type="submit" className="btn-primary bg-blue-600 hover:bg-blue-700">Save</button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
