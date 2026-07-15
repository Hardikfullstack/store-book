'use client';

import { useState, useEffect, useRef } from 'react';
import { Plus, Search, Tag, Trash2, Loader2, ArrowDownCircle } from 'lucide-react';
import { fetchMoreData } from '@/app/actions';
import { dataConnect } from '@/lib/firebase';
import { executeQuery } from 'firebase/data-connect';
import { sanitizeInput } from '@/lib/sanitize';
import { getActiveExpensesRef, syncExpense, softDeleteExpense, getExpenseEntriesCountRef, OrderDirection } from '@/dataconnect';
import { FormattedAmount } from '@/components/FormattedAmount';
import Pagination from '@/app/components/Pagination';
import DynamicTable, { TableColumn, TableRowAction } from '@/components/DynamicTable';

export default function ExpensesClient({
  initialExpenses,
  storeId,
  isPremium
}: {
  initialExpenses: any[],
  storeId?: string,
  isPremium?: boolean
}) {
  const [expenses, setExpenses] = useState(initialExpenses);
  const [currentPage, setCurrentPage] = useState(1);
  const pageSize = 10;
  const [totalItems, setTotalItems] = useState(0);
  const [isLoading, setIsLoading] = useState(false);
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
      orderByType: sortField === 'type' ? dir : undefined,
      orderBySupplierName: sortField === 'supplier_name' ? dir : undefined,
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
    setSearchResults([]);
    if (field === sortField) {
      setSortDirection((prev) => (prev === 'asc' ? 'desc' : 'asc'));
    } else {
      setSortField(field);
      setSortDirection('asc');
    }
  };

  // Fetch total count — skip when searching
  useEffect(() => {
    if (!isPremium || !storeId || debouncedSearch || startDateFilter || endDateFilter || minAmountFilter || maxAmountFilter) return;
    const countKey = `count`;
    const needsServerFetch = (fetchedPagesAtVersionRef.current.get(countKey) ?? -1) < dataVersion;
    const options = needsServerFetch ? { fetchPolicy: 'SERVER_ONLY' as const } : undefined;
    const fetchTotal = async () => {
      try {
        const resp = await executeQuery(getExpenseEntriesCountRef(dataConnect, { storeId }), options);
        setTotalItems(resp.data.expenseEntries.length);
        fetchedPagesAtVersionRef.current.set(countKey, dataVersion);
      } catch (e) {
        console.error('Count fetch error:', e);
      }
    };
    fetchTotal();
  }, [isPremium, storeId, refreshTrigger, debouncedSearch, startDateFilter, endDateFilter, minAmountFilter, maxAmountFilter]);

  useEffect(() => {
    if (!isPremium || !storeId) return;

    let isMounted = true;
    const fetchExpenses = async () => {
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

        const response = await executeQuery(getActiveExpensesRef(dataConnect, vars), options);

        if (!isMounted) return;

        fetchedPagesAtVersionRef.current.set(pageKey, dataVersion);

        const updated = response.data.expenseEntries.map((record: any) => ({
          ...record,
          is_deleted: 0,
          updated_at: record.updatedAt || Date.now(),
          supplier_name: record.supplierName
        }));

        if (needsFullFetch) {
          searchResultsKeyRef.current = currentSearchKey;
          setSearchResults(updated);
          setTotalItems(updated.length);
          setExpenses(updated.slice(0, pageSize));
        } else {
          searchResultsKeyRef.current = '';
          setSearchResults([]);
          setExpenses(updated);
        }
      } catch (error) {
        console.error("Data Connect expense sync error:", error);
      } finally {
        if (isMounted) setIsLoading(false);
      }
    };

    fetchExpenses();
    const intervalId = setInterval(fetchExpenses, 30000);

    return () => {
      isMounted = false;
      clearInterval(intervalId);
    };
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
      setExpenses(searchResults.slice(start, start + pageSize));
    }
  };
  const [formData, setFormData] = useState({
    type: 'supplies',
    description: '',
    amount: 0,
    supplier_name: ''
  });

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      const id = crypto.randomUUID();
      const now = Date.now();
      await syncExpense(dataConnect, {
        id,
        storeId: storeId as string,
        type: formData.type,
        description: formData.description,
        amount: formData.amount,
        timestamp: now,
        supplierName: formData.supplier_name,
        isDeleted: false,
        updatedAt: Math.floor(now / 1000)
      });
      setShowModal(false);
      invalidateAllPages();
      setCurrentPage(1);
      setRefreshTrigger(prev => prev + 1);
    } catch (err) {
      console.error("Failed to save expense:", err);
    }
  };

  const handleDelete = async (id: string) => {
    if (confirm('Are you sure you want to delete this expense?')) {
      try {
        await softDeleteExpense(dataConnect, { id, updatedAt: Math.floor(Date.now() / 1000) });
        invalidateAllPages();
        setCurrentPage(1);
        setRefreshTrigger(prev => prev + 1);
      } catch (err) {
        console.error("Failed to delete expense:", err);
      }
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center mb-6">
        <div>
          <h1 className="text-2xl font-bold text-gray-900 dark:text-white">Expenses Tracker</h1>
          <p className="text-gray-500 dark:text-gray-400 text-sm mt-1">Keep an eye on your business spending.</p>
        </div>
        <button
          onClick={() => { setFormData({ type: 'supplies', description: '', amount: 0, supplier_name: '' }); setShowModal(true); }}
          className="btn-primary flex items-center space-x-2 bg-orange-600 hover:bg-orange-700 shadow-orange-600/30"
        >
          <Plus size={18} />
          <span>Add Expense</span>
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
                className="block w-full pl-10 pr-3 py-2 border border-gray-200 dark:border-gray-700 bg-white dark:bg-gray-800 rounded-lg text-sm placeholder-gray-400 dark:placeholder-gray-500 focus:outline-none focus:ring-2 focus:ring-teal-500 focus:border-transparent transition-all dark:text-gray-100"
                placeholder="Search expenses by description, type or supplier..."
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
                key: 'type',
                label: 'Type',
                sortable: true,
                render: (value) => (
                  <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-gray-100 dark:bg-gray-800 text-gray-800 dark:text-gray-300 border border-gray-200 dark:border-gray-700">
                    {value}
                  </span>
                )
              },
              {
                key: 'description',
                label: 'Description',
                render: (value) => <span className="text-gray-900 dark:text-gray-100">{value}</span>
              },
              {
                key: 'supplier_name',
                label: 'Supplier',
                sortable: true,
                render: (value) => <span className="text-gray-500 dark:text-gray-400">{value || '-'}</span>
              },
              {
                key: 'amount',
                label: 'Amount',
                sortable: true,
                textAlign: 'right',
                className: 'font-bold text-orange-600 dark:text-orange-400',
                render: (value) => <FormattedAmount amount={value} />
              }
            ];

            const rowActions: TableRowAction[] = [
              {
                icon: <Trash2 size={16} />,
                onClick: (expense) => handleDelete(expense.id),
                className: 'text-red-500 hover:text-red-700 transition-colors',
                title: 'Delete Expense'
              }
            ];

            return (
              <DynamicTable
                columns={columns}
                rows={expenses}
                isLoading={isLoading}
                emptyMessage="No expenses found matching your search."
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
            <h2 className="text-xl font-bold mb-4 dark:text-white">Add Expense</h2>
            <form onSubmit={handleSubmit} className="space-y-4">
              <div>
                <label className="block text-sm font-medium dark:text-gray-300">Description</label>
                <input aria-label="text" required type="text" value={formData.description} onChange={e => setFormData({ ...formData, description: sanitizeInput(e.target.value) })} className="mt-1 w-full p-2 border dark:border-gray-700 rounded dark:bg-gray-800 dark:text-white" />
              </div>
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-medium dark:text-gray-300">Type/Category</label>
                  <input aria-label="text" type="text" value={formData.type} onChange={e => setFormData({ ...formData, type: sanitizeInput(e.target.value) })} className="mt-1 w-full p-2 border dark:border-gray-700 rounded dark:bg-gray-800 dark:text-white" />
                </div>
                <div>
                  <label className="block text-sm font-medium dark:text-gray-300">Amount</label>
                  <input aria-label="number" required type="number" step="any" value={formData.amount} onChange={e => setFormData({ ...formData, amount: parseFloat(e.target.value) })} className="mt-1 w-full p-2 border dark:border-gray-700 rounded dark:bg-gray-800 dark:text-white" />
                </div>
              </div>
              <div>
                <label className="block text-sm font-medium dark:text-gray-300">Supplier (Optional)</label>
                <input aria-label="text" type="text" value={formData.supplier_name} onChange={e => setFormData({ ...formData, supplier_name: sanitizeInput(e.target.value) })} className="mt-1 w-full p-2 border dark:border-gray-700 rounded dark:bg-gray-800 dark:text-white" />
              </div>
              <div className="flex justify-end space-x-3 mt-6">
                <button type="button" onClick={() => setShowModal(false)} className="px-4 py-2 text-gray-500 hover:text-gray-700 dark:text-gray-400 dark:hover:text-gray-200">Cancel</button>
                <button type="submit" className="btn-primary bg-orange-600 hover:bg-orange-700">Save</button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
