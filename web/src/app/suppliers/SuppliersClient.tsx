'use client';

import React, { useState, useEffect, useMemo, useRef } from 'react';
import {
  Search,
  Building2,
  Phone,
  MapPin,
  RefreshCw,
  Plus,
  Edit2,
  BookOpen,
} from 'lucide-react';
import { dataConnect } from '@/lib/firebase';
import { executeQuery } from 'firebase/data-connect';
import {
  syncSuppliersRef,
  syncPurchasesRef,
  getSuppliersCountRef,
  OrderDirection,
  SyncSuppliersVariables,
} from '@/dataconnect';
import { FormattedAmount } from '@/components/FormattedAmount';
import { sanitizeInput } from '@/lib/sanitize';
import Pagination from '@/app/components/Pagination';
import DynamicTable, { TableColumn } from '@/components/DynamicTable';
import AddEditSupplierModal, {
  SupplierFormInitialData,
} from '@/components/suppliers/AddEditSupplierModal';
import SupplierLedgerModal from '@/components/suppliers/SupplierLedgerModal';
import { RecordedPaymentData } from '@/components/suppliers/RecordSupplierPaymentModal';

interface SupplierData {
  id: string;
  name: string;
  phone?: string | null;
  gstin?: string | null;
  address?: string | null;
  updatedAt: number;
}

interface PurchaseData {
  id: string;
  supplierId: string;
  supplierName: string;
  totalAmount: number;
  taxAmount?: number;
  type: string;
  timestamp: number;
  notes?: string | null;
  isDeleted?: boolean;
}

interface SupplierRow extends Record<string, unknown> {
  id: string;
  supplierId: string;
  supplierName: string;
  phone: string;
  gstin: string;
  address: string;
  totalPurchases: number;
  totalPaid: number;
  netBalance: number;
  updatedAt: number;
  billsCount: number;
  paymentsCount: number;
}

export default function SuppliersClient({
  storeId,
  isPremium,
}: {
  storeId?: string;
  isPremium?: boolean;
}) {
  const [suppliers, setSuppliers] = useState<SupplierData[]>([]);
  const [allPurchases, setAllPurchases] = useState<PurchaseData[]>([]);
  const [isLoading, setIsLoading] = useState<boolean>(true);
  const [isRefreshing, setIsRefreshing] = useState<boolean>(false);
  const [refreshTrigger, setRefreshTrigger] = useState<number>(0);

  // Search, Sort & Backend Pagination state (matching Items module architecture)
  const [searchQuery, setSearchQuery] = useState<string>('');
  const [debouncedSearch, setDebouncedSearch] = useState<string>('');
  const [sortField, setSortField] = useState<string>('updatedAt');
  const [sortDirection, setSortDirection] = useState<'asc' | 'desc'>('desc');
  const [currentPage, setCurrentPage] = useState<number>(1);
  const pageSize = 10;
  const [totalItems, setTotalItems] = useState<number>(0);

  // Modal states for Epic 48
  const [isAddEditModalOpen, setIsAddEditModalOpen] = useState(false);
  const [editingSupplier, setEditingSupplier] = useState<SupplierFormInitialData | null>(null);
  const [selectedLedgerSupplier, setSelectedLedgerSupplier] = useState<SupplierRow | null>(null);
  const [successToast, setSuccessToast] = useState<string | null>(null);

  const [dataVersion, setDataVersion] = useState<number>(0);
  const fetchedPagesAtVersionRef = useRef<Map<string, number>>(new Map());
  const debounceRef = useRef<ReturnType<typeof setTimeout> | null>(null);

  const invalidateAllPages = () => {
    setDataVersion((v) => v + 1);
    fetchedPagesAtVersionRef.current = new Map();
  };

  const showToast = (message: string) => {
    setSuccessToast(message);
    setTimeout(() => {
      setSuccessToast(null);
    }, 3500);
  };

  // Debounced search (300ms) - only for supplier name
  const handleSearchChange = (val: string) => {
    setSearchQuery(val);
    if (debounceRef.current) clearTimeout(debounceRef.current);
    debounceRef.current = setTimeout(() => {
      invalidateAllPages();
      setDebouncedSearch(val.trim());
      setCurrentPage(1);
    }, 300);
  };

  // Backend sorting on table header click (only for updatedAt)
  const handleSort = (field: string) => {
    invalidateAllPages();
    setCurrentPage(1);
    if (field === sortField) {
      setSortDirection((prev) => (prev === 'asc' ? 'desc' : 'asc'));
    } else {
      setSortField(field);
      setSortDirection('desc');
    }
  };

  const handleRefresh = () => {
    invalidateAllPages();
    setIsRefreshing(true);
    setRefreshTrigger((prev) => prev + 1);
  };

  // Reset state when active store changes
  useEffect(() => {
    setCurrentPage(1);
    setSuppliers([]);
    setAllPurchases([]);
    setTotalItems(0);
    invalidateAllPages();
  }, [storeId]);

  // 1. Fetch total supplier count from backend API for pagination (same as Items module getItemsCount)
  useEffect(() => {
    if (!storeId || debouncedSearch) return;

    const countKey = `count-${storeId}`;
    const needsServerFetch =
      (fetchedPagesAtVersionRef.current.get(countKey) ?? -1) < dataVersion;
    const options = needsServerFetch
      ? { fetchPolicy: 'SERVER_ONLY' as const }
      : undefined;

    executeQuery(getSuppliersCountRef(dataConnect, { storeId }), options)
      .then((res) => {
        if (res.data?.suppliers) {
          setTotalItems(res.data.suppliers.length);
        }
        fetchedPagesAtVersionRef.current.set(countKey, dataVersion);
      })
      .catch((err) => console.error('Count fetch error:', err));
  }, [storeId, refreshTrigger, debouncedSearch, dataVersion, isPremium]);

  // 2. Fetch purchases once for Top Total Net Payable calculation
  useEffect(() => {
    if (!storeId) return;
    let isMounted = true;

    const fetchPurchasesData = async () => {
      try {
        const purchasesRes = await executeQuery(
          syncPurchasesRef(dataConnect, {
            storeId,
            lastSync: -1,
          }),
          { fetchPolicy: 'SERVER_ONLY' as const }
        );

        if (isMounted) {
          const validPurchases = (purchasesRes.data?.purchases || []).filter(
            (p: { isDeleted?: boolean }) => !p.isDeleted
          );
          setAllPurchases(validPurchases as PurchaseData[]);
        }
      } catch (err) {
        console.error('Failed to load purchases summary data:', err);
      }
    };

    fetchPurchasesData();
    return () => {
      isMounted = false;
    };
  }, [storeId, refreshTrigger]);

  // 3. Single Fetch for paginated / searched suppliers from backend API with limit & offset always passed
  useEffect(() => {
    if (!storeId) return;
    let isMounted = true;

    const fetchSuppliers = async () => {
      setIsLoading(true);
      try {
        const isSearching = debouncedSearch.length > 0;
        const pageKey = `page-${storeId}-${currentPage}-${sortField}-${sortDirection}-${debouncedSearch}`;
        const needsServerFetch =
          (fetchedPagesAtVersionRef.current.get(pageKey) ?? -1) < dataVersion;
        const options = needsServerFetch
          ? { fetchPolicy: 'SERVER_ONLY' as const }
          : undefined;

        const offset = (currentPage - 1) * pageSize;
        const dir = sortDirection === 'asc' ? OrderDirection.ASC : OrderDirection.DESC;

        // Pass only orderByUpdatedAt, limit and offset always passed
        const vars: SyncSuppliersVariables = {
          storeId,
          lastSync: -1,
          limit: pageSize,
          offset: offset,
          orderByUpdatedAt: dir,
        };

        if (isSearching) {
          vars.searchTerm = debouncedSearch;
        }

        const response = await executeQuery(
          syncSuppliersRef(dataConnect, vars),
          options
        );

        if (!isMounted) return;

        fetchedPagesAtVersionRef.current.set(pageKey, dataVersion);

        const rawList = (response.data?.suppliers || []).filter(
          (s: { isDeleted?: boolean }) => !s.isDeleted
        );

        setSuppliers(rawList as SupplierData[]);
      } catch (err) {
        console.error('Data Connect suppliers fetch error:', err);
      } finally {
        if (isMounted) {
          setIsLoading(false);
          setIsRefreshing(false);
        }
      }
    };

    fetchSuppliers();
    return () => {
      isMounted = false;
    };
  }, [
    storeId,
    currentPage,
    sortField,
    sortDirection,
    debouncedSearch,
    dataVersion,
    refreshTrigger,
  ]);

  // Compute Top Summary Metric: Total Net Payable across ALL suppliers
  const totalNetPayable = useMemo(() => {
    const purchasesBySupplier = new Map<string, number>();

    for (const p of allPurchases) {
      const sId = String(p.supplierId || '').trim();
      if (!sId) continue;
      const amt = Number(p.totalAmount) || 0;
      const pType = String(p.type || '').toUpperCase();
      const current = purchasesBySupplier.get(sId) || 0;
      if (pType === 'PAYMENT') {
        purchasesBySupplier.set(sId, current - amt);
      } else {
        purchasesBySupplier.set(sId, current + amt);
      }
    }

    let payableSum = 0;
    for (const balance of purchasesBySupplier.values()) {
      if (balance > 0) {
        payableSum += balance;
      }
    }

    return payableSum;
  }, [allPurchases]);

  // Build rows for current paginated suppliers
  const supplierRows: SupplierRow[] = useMemo(() => {
    const purchasesBySupplier = new Map<string, PurchaseData[]>();
    for (const p of allPurchases) {
      const sId = String(p.supplierId || '').trim();
      if (sId) {
        if (!purchasesBySupplier.has(sId)) {
          purchasesBySupplier.set(sId, []);
        }
        purchasesBySupplier.get(sId)!.push(p);
      }
    }

    return suppliers.map((supplier) => {
      const sId = String(supplier.id).trim();
      const sPurchases = purchasesBySupplier.get(sId) || [];

      let totalPurchases = 0;
      let totalPaid = 0;
      let latestActivityTs = Number(supplier.updatedAt) || 0;
      let billsCount = 0;
      let paymentsCount = 0;

      for (const p of sPurchases) {
        const amt = Number(p.totalAmount) || 0;
        const pType = String(p.type || '').toUpperCase();
        const pTs = Number(p.timestamp) || 0;
        const normalizedTs = pTs < 100000000000 ? pTs * 1000 : pTs;

        if (normalizedTs > latestActivityTs) {
          latestActivityTs = normalizedTs;
        }

        if (pType === 'PAYMENT') {
          totalPaid += amt;
          paymentsCount += 1;
        } else {
          totalPurchases += amt;
          billsCount += 1;
        }
      }

      const netBalance = totalPurchases - totalPaid;

      return {
        id: sId,
        supplierId: sId,
        supplierName: supplier.name || 'Unnamed Supplier',
        phone: supplier.phone || '',
        gstin: supplier.gstin || '',
        address: supplier.address || '',
        totalPurchases,
        totalPaid,
        netBalance,
        updatedAt: latestActivityTs,
        billsCount,
        paymentsCount,
      };
    });
  }, [suppliers, allPurchases]);

  const formatDate = (ts: unknown) => {
    const num = Number(ts) || 0;
    if (!num) return '-';
    const raw = num < 100000000000 ? num * 1000 : num;
    const d = new Date(raw);
    if (isNaN(d.getTime())) return '-';
    return d.toLocaleDateString('en-IN', {
      day: '2-digit',
      month: 'short',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  // Handlers for Add/Edit Supplier & Payment
  const handleOpenAddSupplier = () => {
    setEditingSupplier(null);
    setIsAddEditModalOpen(true);
  };

  const handleOpenEditSupplier = (row: SupplierRow) => {
    setEditingSupplier({
      id: row.supplierId,
      name: row.supplierName,
      phone: row.phone,
      gstin: row.gstin,
      address: row.address,
      updatedAt: row.updatedAt,
    });
    setIsAddEditModalOpen(true);
  };

  const handleOpenLedgerModal = (row: SupplierRow) => {
    setSelectedLedgerSupplier(row);
  };

  const handleSupplierSaved = (savedSupplier: SupplierFormInitialData) => {
    showToast(
      editingSupplier
        ? `Supplier "${savedSupplier.name}" updated successfully!`
        : `Supplier "${savedSupplier.name}" created successfully!`
    );
    handleRefresh();
  };

  const handlePaymentLogged = (newPayment: RecordedPaymentData) => {
    // Add to allPurchases to immediately reflect across all cards, table rows, and active modal
    setAllPurchases((prev) => [newPayment, ...prev]);
    showToast(
      `Payment of ₹${newPayment.totalAmount.toLocaleString('en-IN')} recorded successfully!`
    );
  };

  // Get active purchases for the currently opened ledger modal
  const selectedSupplierPurchases = useMemo(() => {
    if (!selectedLedgerSupplier) return [];
    return allPurchases.filter(
      (p) => String(p.supplierId || '').trim() === selectedLedgerSupplier.supplierId
    );
  }, [selectedLedgerSupplier, allPurchases]);

  // Table Columns matching Items module layout + Actions column
  const columns: TableColumn<SupplierRow>[] = [
    {
      key: 'supplierName',
      label: 'Supplier Name',
      sortable: false,
      render: (_val, row) => (
        <div>
          <div className="font-medium text-gray-900 dark:text-gray-100">
            {row.supplierName}
          </div>
        </div>
      ),
    },
    {
      key: 'phone',
      label: 'Phone Number',
      render: (_val, row) =>
        row.phone ? (
          <a
            href={`tel:${row.phone}`}
            className="inline-flex items-center space-x-1 text-teal-600 dark:text-teal-400 hover:underline"
          >
            <Phone size={13} className="shrink-0" />
            <span>{row.phone}</span>
          </a>
        ) : (
          <span className="text-gray-400 dark:text-gray-500">-</span>
        ),
    },
    {
      key: 'gstin',
      label: 'GSTIN',
      render: (_val, row) =>
        row.gstin ? (
          <span className="font-medium text-sm text-gray-500 dark:text-gray-400">
            {row.gstin}
          </span>
        ) : (
          <span className="text-gray-400 dark:text-gray-500">-</span>
        ),
    },
    {
      key: 'updatedAt',
      label: 'Last Activity',
      sortable: true,
      render: (_val, row) => (
        <span className="text-xs text-gray-500 dark:text-gray-400 whitespace-nowrap">
          {formatDate(row.updatedAt)}
        </span>
      ),
    },
    {
      key: 'netBalance',
      label: 'Outstanding Balance',
      textAlign: 'right',
      render: (_val, row) => {
        if (row.netBalance > 0) {
          return (
            <div className="space-y-0.5">
              <div className="font-bold text-sm text-red-600 dark:text-red-400">
                +<FormattedAmount amount={row.netBalance} />
              </div>
              <span className="inline-block px-2 py-0.5 rounded text-[10px] font-bold text-red-700 dark:text-red-300 bg-red-50 dark:bg-red-950/40 border border-red-100 dark:border-red-900/50">
                Pending to Pay
              </span>
            </div>
          );
        }
        if (row.netBalance < 0) {
          return (
            <div className="space-y-0.5">
              <div className="font-bold text-sm text-emerald-600 dark:text-emerald-400">
                <FormattedAmount amount={Math.abs(row.netBalance)} />
              </div>
              <span className="inline-block px-2 py-0.5 rounded text-[10px] font-bold text-emerald-700 dark:text-emerald-300 bg-emerald-50 dark:bg-emerald-950/40 border border-emerald-100 dark:border-emerald-900/50">
                Advance Balance
              </span>
            </div>
          );
        }
        return (
          <div className="space-y-0.5">
            <div className="font-medium text-sm text-gray-500 dark:text-gray-400">
              ₹0.00
            </div>
            <span className="inline-block px-2 py-0.5 rounded text-[10px] font-medium text-gray-600 dark:text-gray-400 bg-gray-100 dark:bg-gray-800 border border-gray-200 dark:border-gray-700">
              Settled
            </span>
          </div>
        );
      },
    },
    {
      key: 'actions',
      label: 'Actions',
      textAlign: 'right',
      render: (_val, row) => (
        <div className="flex items-center justify-end space-x-2">
          <button
            type="button"
            onClick={(e) => {
              e.stopPropagation();
              handleOpenLedgerModal(row);
            }}
            className="inline-flex items-center space-x-1 px-2.5 py-1 text-xs font-semibold text-emerald-700 dark:text-emerald-300 bg-emerald-50 dark:bg-emerald-950/50 border border-emerald-200 dark:border-emerald-900/50 rounded-lg hover:bg-emerald-100 dark:hover:bg-emerald-900/70 transition-colors shadow-2xs"
            title="Record payment or view transaction history"
          >
            <BookOpen size={13} />
            <span>View Ledger</span>
          </button>

          <button
            type="button"
            onClick={(e) => {
              e.stopPropagation();
              handleOpenEditSupplier(row);
            }}
            className="p-1.5 text-gray-500 hover:text-teal-600 dark:text-gray-400 dark:hover:text-teal-400 hover:bg-gray-100 dark:hover:bg-gray-800 rounded-lg transition-colors"
            title="Edit supplier details"
          >
            <Edit2 size={14} />
          </button>
        </div>
      ),
    },
  ];

  return (
    <div className="space-y-6">
      {/* Toast Notification */}
      {successToast && (
        <div className="fixed bottom-6 right-6 z-50 px-4 py-3 bg-gray-900 dark:bg-gray-100 text-white dark:text-gray-900 rounded-xl shadow-xl text-sm font-medium animate-in fade-in slide-in-from-bottom-3 duration-200 flex items-center space-x-2">
          <span>{successToast}</span>
        </div>
      )}

      {/* 1. Header (Same styling as Items module) */}
      <div className="flex justify-between items-center mb-6">
        <div>
          <h1 className="text-2xl font-bold text-gray-900 dark:text-white">
            Suppliers
          </h1>
          <p className="text-gray-500 dark:text-gray-400 text-sm mt-1">
            Manage your store&apos;s suppliers, purchase bills, and ledger balances.
          </p>
        </div>

        <div className="flex items-center space-x-3">
          <button
            type="button"
            onClick={handleRefresh}
            disabled={isRefreshing || isLoading}
            className="flex items-center space-x-2 px-3.5 py-2 text-sm font-medium text-gray-700 dark:text-gray-200 bg-white dark:bg-gray-800 border border-gray-200 dark:border-gray-700 rounded-lg hover:bg-gray-50 dark:hover:bg-gray-750 shadow-sm transition-colors disabled:opacity-50"
          >
            <RefreshCw size={15} className={isRefreshing ? 'animate-spin text-teal-600' : ''} />
            <span>Refresh</span>
          </button>

          {storeId && (
            <button
              type="button"
              onClick={handleOpenAddSupplier}
              className="flex items-center space-x-1.5 px-4 py-2 text-sm font-bold text-white bg-teal-600 hover:bg-teal-700 rounded-lg shadow-sm hover:shadow transition-all"
            >
              <Plus size={16} />
              <span>Add Supplier</span>
            </button>
          )}
        </div>
      </div>

      {/* 2. Top Banner — ONLY Total Net Payable to Suppliers (as requested) */}
      <div className="glass-card p-5 border border-red-100 dark:border-red-900/40 bg-white dark:bg-gray-800 rounded-xl shadow-sm flex items-center justify-between">
        <div className="space-y-1">
          <div className="text-xs font-bold text-gray-500 dark:text-gray-400 uppercase tracking-wider">
            Total Net Payable to Suppliers
          </div>
          <div className="text-3xl font-black text-red-600 dark:text-red-400">
            <FormattedAmount amount={totalNetPayable} />
          </div>
          <div className="text-xs text-gray-400 dark:text-gray-500 font-medium">
            Sum of all outstanding balances owed to suppliers
          </div>
        </div>
        <div className="w-12 h-12 rounded-xl bg-red-50 dark:bg-red-950/40 text-red-600 dark:text-red-400 flex items-center justify-center shrink-0">
          <Building2 size={26} />
        </div>
      </div>

      {/* 3. Main Glass Card Container with Search Bar & DynamicTable (Same design as Items module) */}
      <div className="glass-card overflow-hidden">
        {/* Filter Bar (Search only by supplier name) */}
        <div className="p-4 border-b border-gray-100 dark:border-gray-800 bg-gray-50/50 dark:bg-gray-900/50">
          <div className="flex flex-col sm:flex-row gap-4 justify-between items-start sm:items-center w-full">
            <div className="relative w-72">
              <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                <Search size={16} className="text-gray-400" />
              </div>
              <input
                aria-label="Search suppliers by name"
                type="text"
                value={searchQuery}
                onChange={(e) => handleSearchChange(sanitizeInput(e.target.value))}
                className="block w-full pl-10 pr-3 py-2 border border-gray-200 dark:border-gray-700 bg-white dark:bg-gray-800 rounded-lg text-sm placeholder-gray-400 dark:placeholder-gray-500 focus:outline-none focus:ring-2 focus:ring-teal-500 focus:border-transparent transition-all dark:text-gray-100"
                placeholder="Search supplier name..."
              />
            </div>
          </div>
        </div>

        {/* Dynamic Table with backend sorting */}
        <DynamicTable
          columns={columns}
          rows={supplierRows}
          isLoading={isLoading}
          emptyMessage={
            debouncedSearch
              ? `No suppliers found matching "${debouncedSearch}".`
              : 'No suppliers registered in your store.'
          }
          sortField={sortField}
          sortDirection={sortDirection}
          onSort={handleSort}
        />

        {/* Pagination Footer */}
        {!isLoading && totalItems > 0 && (
          <Pagination
            currentPage={currentPage}
            pageSize={pageSize}
            totalItems={totalItems}
            onPageChange={(page) => setCurrentPage(page)}
          />
        )}
      </div>

      {/* Add / Edit Supplier Modal */}
      {storeId && isAddEditModalOpen && (
        <AddEditSupplierModal
          isOpen={isAddEditModalOpen}
          onClose={() => setIsAddEditModalOpen(false)}
          storeId={storeId}
          supplier={editingSupplier}
          onSuccess={handleSupplierSaved}
        />
      )}

      {/* Supplier Ledger & Transaction History Modal */}
      {storeId && selectedLedgerSupplier && (
        <SupplierLedgerModal
          isOpen={Boolean(selectedLedgerSupplier)}
          onClose={() => setSelectedLedgerSupplier(null)}
          storeId={storeId}
          supplier={{
            id: selectedLedgerSupplier.supplierId,
            name: selectedLedgerSupplier.supplierName,
            phone: selectedLedgerSupplier.phone,
            gstin: selectedLedgerSupplier.gstin,
            address: selectedLedgerSupplier.address,
          }}
          initialPurchases={selectedSupplierPurchases}
          onPaymentLogged={handlePaymentLogged}
        />
      )}
    </div>
  );
}
