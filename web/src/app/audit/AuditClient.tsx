'use client';

import { useState, useEffect, useRef } from 'react';
import { Search, Loader2 } from 'lucide-react';
import { dataConnect } from '@/lib/firebase';
import { executeQuery } from 'firebase/data-connect';
import { sanitizeInput } from '@/lib/sanitize';
import { getStockAdjustmentsRef, getStockAdjustmentsCountRef } from '@/dataconnect';
import Pagination from '@/app/components/Pagination';
import DynamicTable, { TableColumn } from '@/components/DynamicTable';

export default function AuditClient({
  initialAdjustments,
  storeId,
  isPremium
}: {
  initialAdjustments: any[],
  storeId?: string,
  isPremium?: boolean
}) {
  const [adjustments, setAdjustments] = useState(initialAdjustments);
  const [currentPage, setCurrentPage] = useState(1);
  const pageSize = 10;
  const [totalItems, setTotalItems] = useState(0);
  const [isLoading, setIsLoading] = useState(false);
  const [refreshTrigger, setRefreshTrigger] = useState(0);

  const [debouncedSearch, setDebouncedSearch] = useState('');
  const [searchQuery, setSearchQuery] = useState('');
  const [reasonFilter, setReasonFilter] = useState('');
  const [startDateFilter, setStartDateFilter] = useState('');
  const [endDateFilter, setEndDateFilter] = useState('');

  const [searchResults, setSearchResults] = useState<any[]>([]);
  const searchResultsKeyRef = useRef('');
  const fetchedPagesAtVersionRef = useRef<Map<string, number>>(new Map());
  const [dataVersion, setDataVersion] = useState(0);
  const debounceRef = useRef<ReturnType<typeof setTimeout> | null>(null);

  const invalidateAllPages = () => {
    setDataVersion(v => v + 1);
    fetchedPagesAtVersionRef.current = new Map();
  };

  const handleSearchChange = (value: string) => {
    setSearchQuery(value);
    if (debounceRef.current) clearTimeout(debounceRef.current);
    const trimmed = value.trim();
    if (trimmed.length === 0) {
      setDebouncedSearch('');
      setCurrentPage(1);
      return;
    }
    if (trimmed.length < 3) return;
    debounceRef.current = setTimeout(() => {
      setDebouncedSearch(trimmed);
      setCurrentPage(1);
    }, 400);
  };

  // Fetch total count — skip when searching or filtering
  useEffect(() => {
    if (!isPremium || !storeId || debouncedSearch || reasonFilter || startDateFilter || endDateFilter) return;
    const countKey = `count`;
    const needsServerFetch = (fetchedPagesAtVersionRef.current.get(countKey) ?? -1) < dataVersion;
    const options = needsServerFetch ? { fetchPolicy: 'SERVER_ONLY' as const } : undefined;
    const fetchTotal = async () => {
      try {
        const resp = await executeQuery(getStockAdjustmentsCountRef(dataConnect, { storeId }), options);
        setTotalItems(resp.data.stockAdjustments.length);
        fetchedPagesAtVersionRef.current.set(countKey, dataVersion);
      } catch (e) {
        console.error('Count fetch error:', e);
      }
    };
    fetchTotal();
  }, [isPremium, storeId, refreshTrigger, debouncedSearch, reasonFilter, startDateFilter, endDateFilter, dataVersion]);

  useEffect(() => {
    if (!isPremium || !storeId) return;

    let isMounted = true;
    const fetchAdjustments = async () => {
      setIsLoading(true);
      try {
        const isSearching = debouncedSearch.length >= 3;
        const isFiltering = !!reasonFilter || !!startDateFilter || !!endDateFilter;
        const needsFullFetch = isSearching || isFiltering;
        const currentSearchKey = `${debouncedSearch}-${reasonFilter}-${startDateFilter}-${endDateFilter}`;

        if (needsFullFetch && searchResults.length > 0 && searchResultsKeyRef.current === currentSearchKey) {
          setIsLoading(false);
          return;
        }

        const offset = (currentPage - 1) * pageSize;
        const pageKey = `page-${currentPage}-${debouncedSearch}-${reasonFilter}-${startDateFilter}-${endDateFilter}`;
        const needsServerFetch = (fetchedPagesAtVersionRef.current.get(pageKey) ?? -1) < dataVersion;
        const options = needsServerFetch ? { fetchPolicy: 'SERVER_ONLY' as const } : undefined;

        const vars: any = { storeId };
        if (debouncedSearch) vars.searchTerm = debouncedSearch;
        if (reasonFilter) vars.reason = reasonFilter;
        if (startDateFilter) vars.startDate = new Date(startDateFilter).getTime();
        if (endDateFilter) vars.endDate = new Date(endDateFilter).setHours(23, 59, 59, 999);

        if (!needsFullFetch) {
          vars.limit = pageSize;
          vars.offset = offset;
        }

        const response = await executeQuery(getStockAdjustmentsRef(dataConnect, vars), options);

        if (!isMounted) return;

        fetchedPagesAtVersionRef.current.set(pageKey, dataVersion);

        const updated = response.data.stockAdjustments.map((record: any) => ({
          ...record,
          updated_at: record.updatedAt || Date.now()
        }));

        if (needsFullFetch) {
          searchResultsKeyRef.current = currentSearchKey;
          setSearchResults(updated);
          setTotalItems(updated.length);
          setAdjustments(updated.slice(0, pageSize));
        } else {
          searchResultsKeyRef.current = '';
          setSearchResults([]);
          setAdjustments(updated);
        }
      } catch (error) {
        console.error("Data Connect stock adjustments fetch error:", error);
      } finally {
        if (isMounted) setIsLoading(false);
      }
    };

    fetchAdjustments();
    const intervalId = setInterval(fetchAdjustments, 30000);

    return () => {
      isMounted = false;
      clearInterval(intervalId);
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [isPremium, storeId, currentPage, refreshTrigger, dataVersion, debouncedSearch, reasonFilter, startDateFilter, endDateFilter]);

  const handlePageChange = (page: number) => {
    setCurrentPage(page);
    const isFiltering = !!reasonFilter || !!startDateFilter || !!endDateFilter;
    if ((debouncedSearch || isFiltering) && searchResults.length > 0) {
      const start = (page - 1) * pageSize;
      setAdjustments(searchResults.slice(start, start + pageSize));
    }
  };

  const formatDate = (timestamp: number) => {
    if (!timestamp) return '-';
    return new Date(timestamp).toLocaleString('en-IN', {
      day: 'numeric',
      month: 'short',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  };

  const columns: TableColumn[] = [
    {
      key: 'timestamp',
      label: 'Date & Time',
      render: (value) => <span className="text-sm font-medium text-gray-900 dark:text-gray-100">{formatDate(Number(value))}</span>
    },
    {
      key: 'itemName',
      label: 'Item Name',
      render: (value) => <span className="text-sm text-gray-900 dark:text-white">{value}</span>
    },
    {
      key: 'reason',
      label: 'Reason',
      render: (value) => {
        const reasonColors: Record<string, string> = {
          'Damage': 'bg-red-50 text-red-700 dark:bg-red-900/30 dark:text-red-400 border-red-100 dark:border-red-800',
          'Expiry': 'bg-orange-50 text-orange-700 dark:bg-orange-900/30 dark:text-orange-400 border-orange-100 dark:border-orange-800',
          'Loss': 'bg-rose-50 text-rose-700 dark:bg-rose-900/30 dark:text-rose-400 border-rose-100 dark:border-rose-800',
          'Restock': 'bg-emerald-50 text-emerald-700 dark:bg-emerald-900/30 dark:text-emerald-400 border-emerald-100 dark:border-emerald-800',
          'Count Correction': 'bg-blue-50 text-blue-700 dark:bg-blue-900/30 dark:text-blue-400 border-blue-100 dark:border-blue-800'
        };
        const colorClass = reasonColors[value as string] || 'bg-gray-50 text-gray-700 dark:bg-gray-800 dark:text-gray-300 border-gray-200';
        return (
          <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium border ${colorClass}`}>
            {value}
          </span>
        );
      }
    },
    {
      key: 'delta',
      label: 'Stock Delta',
      render: (value) => {
        const num = Number(value);
        const isPositive = num > 0;
        return (
          <span className={`text-sm font-semibold ${isPositive ? 'text-emerald-600 dark:text-emerald-400' : 'text-red-600 dark:text-red-400'}`}>
            {isPositive ? `+${num}` : num}
          </span>
        );
      }
    }
  ];

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-gray-900 dark:text-white">Stock Audit Report</h1>
        <p className="text-gray-500 dark:text-gray-400 text-sm mt-1">Timeline of all manual stock adjustments and discrepancies.</p>
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
                placeholder="Search items by name..."
              />
            </div>

            <div className="flex flex-wrap items-center gap-3 w-full md:w-auto justify-end">
              <div className="flex items-center gap-2 bg-white dark:bg-gray-800 border border-gray-200 dark:border-gray-700 rounded-lg px-3 h-9">
                <span className="text-xs text-gray-500 dark:text-gray-400">Reason:</span>
                <select
                  value={reasonFilter}
                  onChange={(e) => { setReasonFilter(e.target.value); setCurrentPage(1); }}
                  className="bg-transparent border-none text-xs text-gray-900 dark:text-white outline-none focus:ring-0 cursor-pointer pr-8 py-0"
                >
                  <option className="bg-white dark:bg-gray-800 text-gray-900 dark:text-white" value="">All Reasons</option>
                  <option className="bg-white dark:bg-gray-800 text-gray-900 dark:text-white" value="Count Correction">Count Correction</option>
                  <option className="bg-white dark:bg-gray-800 text-gray-900 dark:text-white" value="Damage">Damage</option>
                  <option className="bg-white dark:bg-gray-800 text-gray-900 dark:text-white" value="Expiry">Expiry</option>
                  <option className="bg-white dark:bg-gray-800 text-gray-900 dark:text-white" value="Loss">Loss</option>
                  <option className="bg-white dark:bg-gray-800 text-gray-900 dark:text-white" value="Restock">Restock</option>
                </select>
              </div>

              <div className="flex items-center gap-2 bg-white dark:bg-gray-800 border border-gray-200 dark:border-gray-700 rounded-lg px-3 h-9">
                <span className="text-xs text-gray-500 dark:text-gray-400">Date:</span>
                <input
                  type="date"
                  value={startDateFilter}
                  onChange={(e) => { setStartDateFilter(e.target.value); setCurrentPage(1); }}
                  className="w-[110px] px-1 py-0 bg-transparent border-none text-xs text-gray-900 dark:text-white outline-none focus:ring-0"
                />
                <span className="text-gray-300 dark:text-gray-600">-</span>
                <input
                  type="date"
                  value={endDateFilter}
                  onChange={(e) => { setEndDateFilter(e.target.value); setCurrentPage(1); }}
                  className="w-[110px] px-1 py-0 bg-transparent border-none text-xs text-gray-900 dark:text-white outline-none focus:ring-0"
                />
              </div>
            </div>
          </div>
        </div>

        <div className="overflow-x-auto">
          <DynamicTable
            columns={columns}
            rows={adjustments}
            isLoading={isLoading}
            emptyMessage="No stock adjustments found"
            rowKey="id"
          />
        </div>

        <Pagination
          currentPage={currentPage}
          pageSize={pageSize}
          totalItems={totalItems}
          isLoading={isLoading}
          onPageChange={handlePageChange}
        />
      </div>
    </div>
  );
}
