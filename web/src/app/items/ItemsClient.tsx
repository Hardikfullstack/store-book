'use client';

import { useState, useEffect, useRef } from 'react';
import { Plus, Search, Trash2, Edit2, Loader2, ArrowDownCircle } from 'lucide-react';
import { fetchMoreData } from '@/app/actions';
import ExportButtons from '@/app/ExportButtons';
import { sanitizeInput } from '@/lib/sanitize';
import { dataConnect } from '@/lib/firebase';
import { executeQuery } from 'firebase/data-connect';
import { getActiveItemsRef, syncItem, softDeleteItem, getItemsCountRef, OrderDirection, syncStockAdjustment, syncPurchase, syncPurchaseItem, syncItemBatch } from '@/dataconnect';
import { FormattedAmount } from '@/components/FormattedAmount';
import RestockQuantity from '@/components/models/RestockQuantity';
import { useDispatch, useSelector } from 'react-redux';
import { RootState } from '@/store';
import { setInventory, updateInventoryItem } from '@/store/inventorySlice';
import Pagination from '../components/Pagination';
import DynamicTable, { TableColumn, TableRowAction } from '@/components/DynamicTable';

type UnitOption = 'pcs' | 'kg' | 'g' | 'litre' | 'ml' | 'dozen' | 'box' | 'packet';

type ItemFormData = {
  name: string;
  category: string;
  quantity: number;
  unit: UnitOption;
  buy_price: number;
  sell_price: number;
  low_stock_threshold: number;

  hsnCode: string;
  taxRate: number;

  batchLotNumber: string;
  expiryDate: string; // yyyy-mm-dd
};

const UNIT_OPTIONS: UnitOption[] = ['pcs', 'kg', 'g', 'litre', 'ml', 'dozen', 'box', 'packet'];

function emptyFormData(): ItemFormData {
  return {
    name: '',
    category: '',
    quantity: 0,
    unit: 'pcs',
    buy_price: 0,
    sell_price: 0,
    low_stock_threshold: 0,
    hsnCode: '',
    taxRate: 0,
    batchLotNumber: '',
    expiryDate: ''
  };
}

export default function ItemsClient({
  initialItems,
  userRole,
  storeId,
  isPremium
}: {
  initialItems: any[],
  userRole: string,
  storeId?: string,
  isPremium?: boolean
}) {
  const dispatch = useDispatch();
  const cachedItems = useSelector((state: RootState) => state.inventory.items);
  const lastSynced = useSelector((state: RootState) => state.inventory.lastSynced);

  const [items, setItems] = useState<any[]>(cachedItems.length > 0 ? cachedItems : initialItems);
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
  const [searchQuery, setSearchQuery] = useState('');
  const [minPriceFilter, setMinPriceFilter] = useState('');
  const [maxPriceFilter, setMaxPriceFilter] = useState('');
  const debounceRef = useRef<ReturnType<typeof setTimeout> | null>(null);
  const searchResultsKeyRef = useRef('');

  const buildSortVars = (sortField: string | null, direction: 'asc' | 'desc') => {
    if (!sortField) return {};
    const dir = direction === 'asc' ? OrderDirection.ASC : OrderDirection.DESC;
    return {
      orderByName: sortField === 'name' ? dir : undefined,
      orderByQuantity: sortField === 'quantity' ? dir : undefined,
      orderByBuyPrice: sortField === 'buy_price' ? dir : undefined,
      orderBySellPrice: sortField === 'sell_price' ? dir : undefined,
      orderByCategory: sortField === 'category' ? dir : undefined,
      orderByUpdatedAt: sortField === 'updatedAt' ? dir : undefined,
    };
  };

  const invalidateAllPages = () => {
    setDataVersion(v => v + 1);
    fetchedPagesAtVersionRef.current = new Map();
    searchResultsKeyRef.current = '';
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

  // Fetch total count — skip when searching or filtering (client-side manages total)
  useEffect(() => {
    if (!isPremium || !storeId || debouncedSearch || minPriceFilter || maxPriceFilter) return;
    const countKey = `count`;
    const needsServerFetch = (fetchedPagesAtVersionRef.current.get(countKey) ?? -1) < dataVersion;
    const options = needsServerFetch ? { fetchPolicy: 'SERVER_ONLY' as const } : undefined;
    executeQuery(getItemsCountRef(dataConnect, { storeId }), options)
      .then(res => {
        if (res.data?.items) setTotalItems(res.data.items.length);
        fetchedPagesAtVersionRef.current.set(countKey, dataVersion);
      })
      .catch(err => console.error('Count fetch error:', err));
      // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [isPremium, storeId, refreshTrigger, debouncedSearch, minPriceFilter, maxPriceFilter]);

  // Fetch paginated items whenever page changes and poll regularly
  useEffect(() => {
    if (!isPremium || !storeId) return;
    let isMounted = true;
    const fetchItems = async () => {
      setIsLoading(true);
      try {
        const isSearching = debouncedSearch.length >= 3;
        const isFiltering = minPriceFilter || maxPriceFilter;
        const needsFullFetch = isSearching || isFiltering;
        const currentSearchKey = `${debouncedSearch}-${sortField}-${sortDirection}-${minPriceFilter}-${maxPriceFilter}`;

        if (needsFullFetch && searchResults.length > 0 && searchResultsKeyRef.current === currentSearchKey) { setIsLoading(false); return; }

        const offset = (currentPage - 1) * pageSize;
        const pageKey = `page-${currentPage}-${sortField}-${sortDirection}-${debouncedSearch}-${minPriceFilter}-${maxPriceFilter}`;
        const needsServerFetch = (fetchedPagesAtVersionRef.current.get(pageKey) ?? -1) < dataVersion;
        const options = needsServerFetch ? { fetchPolicy: 'SERVER_ONLY' as const } : undefined;

        const vars: any = { storeId, ...buildSortVars(sortField, sortDirection) };
        if (minPriceFilter) vars.minPrice = Number(minPriceFilter);
        if (maxPriceFilter) vars.maxPrice = Number(maxPriceFilter);

        if (needsFullFetch) {
          if (isSearching) vars.searchTerm = debouncedSearch;
        } else {
          vars.limit = pageSize;
          vars.offset = offset;
        }

        const response = await executeQuery(getActiveItemsRef(dataConnect, vars), options);

        if (!isMounted) return;

        fetchedPagesAtVersionRef.current.set(pageKey, dataVersion);

        let updated = response.data.items.map((item: any) => ({
          ...item,
          is_deleted: 0,
          updated_at: item.updatedAt || Date.now(),
          buy_price: item.buyPrice,
          sell_price: item.sellPrice,
          low_stock_threshold: item.lowStockThreshold
        }));

        if (userRole === 'staff') {
          updated.forEach((item: any) => {
            if (item.buy_price !== undefined) delete item.buy_price;
          });
        }

        if (needsFullFetch) {
          searchResultsKeyRef.current = currentSearchKey;
          setSearchResults(updated);
          setTotalItems(updated.length);
          setItems(updated.slice(0, pageSize));
        } else {
          searchResultsKeyRef.current = '';
          setSearchResults([]);
          setItems(updated);
        }
        dispatch(setInventory(updated));
      } catch (error) {
        console.error('Data Connect items fetch error:', error);
      } finally {
        if (isMounted) setIsLoading(false);
      }
    };
    // Initial fetch for current page
    fetchItems();
    // Poll every 30 seconds for updates
    const intervalId = setInterval(fetchItems, 30000);
    return () => {
      isMounted = false;
      clearInterval(intervalId);
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [isPremium, storeId, userRole, currentPage, refreshTrigger, dataVersion, debouncedSearch, minPriceFilter, maxPriceFilter]);
  const [showModal, setShowModal] = useState(false);
  const [editingId, setEditingId] = useState<string | null>(null);
  const [originalItem, setOriginalItem] = useState<any | null>(null);
  const [adjustmentReason, setAdjustmentReason] = useState<string>('Count Correction');

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

  const handlePageChange = (page: number) => {
    setCurrentPage(page);
    const isFiltering = minPriceFilter || maxPriceFilter;
    if ((debouncedSearch || isFiltering) && searchResults.length > 0) {
      const start = (page - 1) * pageSize;
      setItems(searchResults.slice(start, start + pageSize));
    }
  };
  const [formData, setFormData] = useState<ItemFormData>(() => emptyFormData());
  const [showAdvanced, setShowAdvanced] = useState(false);
  const [reStockQuantity, setReStockQuantity] = useState<any | null>(null);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      const payload = !showAdvanced ? {
        hsnCode: '',
        taxRate: 0,
        batchLotNumber: '',
        expiryDate: ''
      } : {
        hsnCode: formData.hsnCode,
        taxRate: formData.taxRate,
        batchLotNumber: formData.batchLotNumber,
        expiryDate: formData.expiryDate
      };
      const id = editingId || crypto.randomUUID();
      await syncItem(dataConnect, {
        id,
        storeId: storeId as string,
        name: formData.name,
        quantity: formData.quantity,
        unit: formData.unit,
        buyPrice: formData.buy_price,
        sellPrice: formData.sell_price,
        lowStockThreshold: formData.low_stock_threshold,
        category: formData.category,
        isDeleted: false,
        updatedAt: Math.floor(Date.now() / 1000),
        ...payload,
      });

      if (editingId && originalItem && formData.quantity !== originalItem.quantity) {
        const delta = formData.quantity - originalItem.quantity;
        await syncStockAdjustment(dataConnect, {
          id: crypto.randomUUID(),
          storeId: storeId as string,
          itemId: id,
          itemName: formData.name,
          reason: adjustmentReason,
          delta: delta,
          timestamp: Date.now(),
          isDeleted: false,
          updatedAt: Date.now()
        });
      }

      setShowModal(false);
      if (editingId) {
        const updatedItem = {
          id,
          name: formData.name,
          quantity: formData.quantity,
          unit: formData.unit,
          buy_price: formData.buy_price,
          sell_price: formData.sell_price,
          low_stock_threshold: formData.low_stock_threshold,
          category: formData.category,
          ...payload,
        };
        setItems(prev => {
          const idx = prev.findIndex(i => i.id === id);
          if (idx > -1) {
            const next = [...prev];
            next[idx] = { ...next[idx], ...updatedItem };
            return next;
          }
          return [updatedItem, ...prev];
        });
        dispatch(updateInventoryItem(updatedItem));
      } else {
        invalidateAllPages();
        setCurrentPage(1);
        setRefreshTrigger(prev => prev + 1);
      }
    } catch (err) {
      console.error("Failed to save item:", err);
    }
  };

  const handleEdit = (item: any) => {
    const next: ItemFormData = {
      name: item.name || '',
      category: item.category || '',
      quantity: item.quantity || 0,
      unit: (item.unit || 'pcs') as UnitOption,
      buy_price: item.buy_price || 0,
      sell_price: item.sell_price || 0,
      low_stock_threshold: item.low_stock_threshold || 0,

      hsnCode: item.hsnCode || '',
      taxRate: item.taxRate || 0,

      batchLotNumber: item.batchLotNumber || '',
      expiryDate: item.expiryDate || ''
    };

    setFormData(next);
    setEditingId(item.id);
    setOriginalItem(item);
    setAdjustmentReason('Count Correction');

    const shouldShow =
      !!next.hsnCode ||
      !!next.taxRate ||
      !!next.batchLotNumber ||
      !!next.expiryDate;

    setShowAdvanced(shouldShow);
    setShowModal(true);
  };

  const handleDelete = async (id: string) => {
    if (confirm('Are you sure you want to delete this item?')) {
      try {
        await softDeleteItem(dataConnect, { id, updatedAt: Date.now() });
        invalidateAllPages();
        setCurrentPage(1);
        setRefreshTrigger(prev => prev + 1);
      } catch (err) {
        console.error("Failed to delete item:", err);
      }
    }
  };

  const openCreate = () => {
    setEditingId(null);
    setOriginalItem(null);
    setFormData(emptyFormData());
    setShowAdvanced(false);
    setShowModal(true);
  };

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center mb-6">
        <div>
          <h1 className="text-2xl font-bold text-gray-900 dark:text-white">Inventory Items</h1>
          <p className="text-gray-500 dark:text-gray-400 text-sm mt-1">Manage your store&apos;s products and stock levels.</p>
        </div>
        <div className="flex items-center space-x-3">
          <ExportButtons data={items} type="items" columns={['name', 'category', 'quantity', 'unit', 'buy_price', 'sell_price']} />
          <button
            onClick={openCreate}
            className="btn-primary flex items-center space-x-2"
          >
            <Plus size={18} />
            <span>Add Item</span>
          </button>
        </div>
      </div>

      <div className="glass-card overflow-hidden">
        <div className="p-4 border-b border-gray-100 dark:border-gray-800 bg-gray-50/50 dark:bg-gray-900/50">
          <div className="flex flex-col sm:flex-row gap-4 justify-between items-start sm:items-center w-full">
            <div className="relative w-64">
              <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                <Search size={16} className="text-gray-400" />
              </div>
              <input aria-label="text"
                type="text"
                value={searchQuery}
                onChange={(e) => handleSearchChange(sanitizeInput(e.target.value))}
                className="block w-full pl-10 pr-3 py-2 border border-gray-200 dark:border-gray-700 bg-white dark:bg-gray-800 rounded-lg text-sm placeholder-gray-400 dark:placeholder-gray-500 focus:outline-none focus:ring-2 focus:ring-teal-500 focus:border-transparent transition-all dark:text-gray-100"
                placeholder="Search items by name or category..."
              />
            </div>

            <div className="flex flex-wrap items-center gap-4">
              <div className="flex items-center gap-2 bg-white dark:bg-gray-800 border border-gray-200 dark:border-gray-700 rounded-lg px-2 py-1">
                <span className="text-xs text-gray-500 dark:text-gray-400 mr-1">Price Range:</span>
                <input
                  type="number"
                  placeholder="Min"
                  value={minPriceFilter}
                  onChange={(e) => {
                    setMinPriceFilter(e.target.value);
                    setCurrentPage(1);
                  }}
                  className="w-16 px-1 py-1 bg-transparent border-none text-sm text-gray-900 dark:text-white outline-none focus:ring-0"
                />
                <span className="text-gray-300 dark:text-gray-600">-</span>
                <input
                  type="number"
                  placeholder="Max"
                  value={maxPriceFilter}
                  onChange={(e) => {
                    setMaxPriceFilter(e.target.value);
                    setCurrentPage(1);
                  }}
                  className="w-16 px-1 py-1 bg-transparent border-none text-sm text-gray-900 dark:text-white outline-none focus:ring-0"
                />
              </div>
            </div>
          </div>
        </div>

        <div className="overflow-x-auto">
          {
            (() => {
              const columns: TableColumn[] = [
                {
                  key: 'name',
                  label: 'Item Name',
                  sortable: true,
                  render: (value, row) => (
                    <div>
                      <div className="font-medium text-gray-900 dark:text-gray-100">{value}</div>
                      {row.expiryDate && (
                        <div className="text-[10px] text-gray-500 dark:text-gray-400 mt-1">Exp: {row.expiryDate}</div>
                      )}
                    </div>
                  )
                },
                {
                  key: 'category',
                  label: 'Category',
                  sortable: true,
                  render: (value) => (
                    <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-gray-100 dark:bg-gray-800 text-gray-800 dark:text-gray-300 border border-gray-200 dark:border-gray-700">
                      {value || 'Uncategorized'}
                    </span>
                  )
                },
                {
                  key: 'quantity',
                  label: 'Stock Level',
                  sortable: true,
                  render: (value, row) => (
                    <span className="text-sm text-gray-900 dark:text-white">
                      {value} <span className="text-gray-400 dark:text-gray-500 text-xs">{row.unit}</span>
                    </span>
                  )
                },
                ...(userRole !== 'staff' ? [{
                  key: 'buy_price',
                  label: 'Buy Price',
                  sortable: true,
                  render: (value: any) => <FormattedAmount amount={value} />
                }] : []),
                {
                  key: 'sell_price',
                  label: 'Sell Price',
                  sortable: true,
                  className: 'font-medium',
                  render: (value: any) => <FormattedAmount amount={value} />
                },
                ...(userRole !== 'staff' ? [{
                  key: 'buy_price',
                  label: 'Margin',
                  render: (value: any, row: any) => {
                    if (value > 0) {
                      const marginPercent = (((row.sell_price - value) / value) * 100).toFixed(0);
                      const isPositive = Number(marginPercent) >= 0;
                      return (
                        <span className={`px-2 py-1 rounded text-xs font-bold ${isPositive ? 'bg-emerald-100 text-emerald-700 dark:bg-emerald-900/30 dark:text-emerald-400' : 'bg-red-100 text-red-700 dark:bg-red-900/30 dark:text-red-400'}`}>
                          {marginPercent}%
                        </span>
                      );
                    }
                    return '-';
                  }
                }] : [])
              ];

              const rowActions: TableRowAction[] = [
                {
                  icon: <Plus size={18} />,
                  onClick: (item) => setReStockQuantity(item),
                  className: 'text-green-600 dark:text-green-400 hover:text-green-900 dark:hover:text-green-300 transition-colors',
                  title: 'Restock'
                },
                {
                  icon: <Edit2 size={18} />,
                  onClick: (item) => handleEdit(item),
                  className: 'text-indigo-600 dark:text-indigo-400 hover:text-indigo-900 dark:hover:text-indigo-300 transition-colors',
                  title: 'Edit'
                },
                ...(userRole !== 'staff' ? [{
                  icon: <Trash2 size={18} />,
                  onClick: (item: any) => handleDelete(item.id),
                  className: 'text-red-500 hover:text-red-700 transition-colors',
                  title: 'Delete'
                }] : [])
              ];

              return (
                <DynamicTable
                  columns={columns}
                  rows={items}
                  isLoading={isLoading}
                  emptyMessage="No items records found"
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
            <h2 className="text-xl font-bold mb-4 dark:text-white">{editingId ? 'Edit Item' : 'Add New Item'}</h2>
            <form onSubmit={handleSubmit} className="space-y-4">
              <div>
                <label className="block text-sm font-medium dark:text-gray-300">Name</label>
                <input aria-label="text" required type="text" value={formData.name} onChange={e => setFormData({ ...formData, name: sanitizeInput(e.target.value) })} className="mt-1 w-full p-2 border dark:border-gray-700 rounded dark:bg-gray-800 dark:text-white" />
              </div>
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-medium dark:text-gray-300">Category</label>
                  <input aria-label="text" type="text" value={formData.category} onChange={e => setFormData({ ...formData, category: sanitizeInput(e.target.value) })} className="mt-1 w-full p-2 border dark:border-gray-700 rounded dark:bg-gray-800 dark:text-white" />
                </div>
                <div>
                  <label className="block text-sm font-medium dark:text-gray-300">Unit</label>
                  <select
                    required
                    value={formData.unit}
                    onChange={(e) => setFormData({ ...formData, unit: sanitizeInput(e.target.value) as UnitOption })}
                    className="mt-1 w-full p-2 border dark:border-gray-700 rounded dark:bg-gray-800 dark:text-white"
                  >
                    {UNIT_OPTIONS.map((u) => (
                      <option key={u} value={u}>
                        {u}
                      </option>
                    ))}
                  </select>
                </div>
              </div>
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-medium dark:text-gray-300">Quantity</label>
                  <input aria-label="number" required type="number" step="any" value={formData.quantity} onChange={e => setFormData({ ...formData, quantity: parseFloat(e.target.value) })} className="mt-1 w-full p-2 border dark:border-gray-700 rounded dark:bg-gray-800 dark:text-white" />
                </div>
                <div>
                  <label className="block text-sm font-medium dark:text-gray-300">Low Stock Alert</label>
                  <input aria-label="number" required type="number" step="any" value={formData.low_stock_threshold} onChange={e => setFormData({ ...formData, low_stock_threshold: parseFloat(e.target.value) })} className="mt-1 w-full p-2 border dark:border-gray-700 rounded dark:bg-gray-800 dark:text-white" />
                </div>
              </div>

              {editingId && originalItem && formData.quantity !== originalItem.quantity && (
                <div>
                  <label className="block text-sm font-medium dark:text-gray-300">Reason for Stock Adjustment</label>
                  <select
                    required
                    value={adjustmentReason}
                    onChange={(e) => setAdjustmentReason(e.target.value)}
                    className="mt-1 w-full p-2 border dark:border-gray-700 rounded dark:bg-gray-800 dark:text-white"
                  >
                    <option value="Count Correction">Count Correction</option>
                    <option value="Damage">Damage</option>
                    <option value="Expiry">Expiry</option>
                    <option value="Loss">Loss</option>
                    <option value="Restock">Restock</option>
                  </select>
                </div>
              )}
              <div className="grid grid-cols-2 gap-4">
                {userRole !== 'staff' && (
                  <div>
                    <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">Buy Price (₹)</label>
                    <input aria-label="number"
                      type="number"
                      step="0.01"
                      required
                      value={formData.buy_price || ''}
                      onChange={(e) => setFormData({ ...formData, buy_price: parseFloat(e.target.value) || 0 })}
                      className="mt-1 w-full p-2 border dark:border-gray-700 rounded dark:bg-gray-800 dark:text-white"
                      placeholder="0.00"
                    />
                  </div>
                )}
                <div>
                  <label className="block text-sm font-medium dark:text-gray-300">Sell Price</label>
                  <input aria-label="number"
                    required
                    type="number"
                    step="any"
                    value={formData.sell_price}
                    onChange={(e) => setFormData({ ...formData, sell_price: parseFloat(e.target.value) })}
                    className="mt-1 w-full p-2 border dark:border-gray-700 rounded dark:bg-gray-800 dark:text-white"
                  />
                </div>
              </div>

              <div>
                <button
                  type="button"
                  onClick={() => setShowAdvanced((s) => !s)}
                  className="w-full flex items-center justify-between px-3 py-2 rounded-lg bg-gray-50 dark:bg-gray-800 border border-gray-200 dark:border-gray-700 text-sm font-medium text-gray-700 dark:text-gray-200 hover:bg-gray-100 dark:hover:bg-gray-700"
                >
                  <span>Show Advanced Options</span>
                  <span>{showAdvanced ? '▲' : '▼'}</span>
                </button>
              </div>

              {showAdvanced && (
                <div className="space-y-4">
                  <div className="grid grid-cols-2 gap-4">
                    <div>
                      <label className="block text-sm font-medium dark:text-gray-300">HSN/SAC Code</label>
                      <input aria-label="text"
                        type="text"
                        value={formData.hsnCode}
                        onChange={(e) => setFormData({ ...formData, hsnCode: sanitizeInput(e.target.value) })}
                        className="mt-1 w-full p-2 border dark:border-gray-700 rounded dark:bg-gray-800 dark:text-white"
                      />
                    </div>
                    <div>
                      <label className="block text-sm font-medium dark:text-gray-300">Tax Rate (%)</label>
                      <input aria-label="number"
                        type="number"
                        step="any"
                        value={formData.taxRate}
                        onChange={(e) => setFormData({ ...formData, taxRate: parseFloat(e.target.value) || 0 })}
                        className="mt-1 w-full p-2 border dark:border-gray-700 rounded dark:bg-gray-800 dark:text-white"
                      />
                    </div>
                  </div>

                  <div>
                    <span>Batch & Expiry Tracking (Optional)</span>
                  </div>

                  <div className="grid grid-cols-2 gap-4">
                    <div>
                      <label className="block text-sm font-medium dark:text-gray-300">Batch/Lot Number</label>
                      <input aria-label="text"
                        type="text"
                        value={formData.batchLotNumber}
                        onChange={(e) =>
                          setFormData({ ...formData, batchLotNumber: sanitizeInput(e.target.value) })
                        }
                        className="mt-1 w-full p-2 border dark:border-gray-700 rounded dark:bg-gray-800 dark:text-white"
                      />
                    </div>
                    <div>
                      <label className="block text-sm font-medium dark:text-gray-300">Expiry Date</label>
                      <input aria-label="date"
                        type="date"
                        value={formData.expiryDate}
                        onChange={(e) =>
                          setFormData({
                            ...formData,
                            expiryDate: sanitizeInput(e.target.value)
                          })
                        }
                        className="mt-1 w-full p-2 border dark:border-gray-700 rounded dark:bg-gray-800 dark:text-white"
                      />
                    </div>
                  </div>
                </div>
              )}
              <div className="flex justify-end space-x-3 mt-6">
                <button type="button" onClick={() => setShowModal(false)} className="px-4 py-2 text-gray-500 hover:text-gray-700 dark:text-gray-400 dark:hover:text-gray-200">Cancel</button>
                <button type="submit" className="btn-primary">Save</button>
              </div>
            </form>
          </div>
        </div>
      )}
      <RestockQuantity
        open={Boolean(reStockQuantity)}
        item={reStockQuantity}
        userRole={userRole}
        storeId={storeId}
        onClose={() => setReStockQuantity(null)}
        onConfirm={async (payload) => {
          if (!reStockQuantity) return;

          const nextQuantity = Number(reStockQuantity.quantity || 0) + payload.quantity;
          const updatedItem = {
            ...reStockQuantity,
            quantity: nextQuantity,
            ...(userRole !== 'staff' && { buy_price: payload.buyPrice ?? reStockQuantity.buy_price ?? 0 }),
            batchLotNumber: payload.batchNumber || reStockQuantity.batchLotNumber,
            expiryDate: payload.expiryDate || reStockQuantity.expiryDate,
            updated_at: Date.now(),
          };

          // Optimistic update
          setItems((prev) => prev.map((item: any) => item.id === reStockQuantity.id ? updatedItem : item));
          dispatch(updateInventoryItem(updatedItem));

          await syncItem(dataConnect, {
            id: reStockQuantity.id,
            storeId: storeId as string,
            name: updatedItem.name,
            quantity: updatedItem.quantity,
            unit: updatedItem.unit || 'pcs',
            buyPrice: updatedItem.buy_price,
            sellPrice: updatedItem.sell_price,
            lowStockThreshold: updatedItem.low_stock_threshold,
            category: updatedItem.category,
            batchLotNumber: updatedItem.batchLotNumber,
            expiryDate: updatedItem.expiryDate,
            isDeleted: false,
            updatedAt: updatedItem.updated_at
          });

          await syncStockAdjustment(dataConnect, {
            id: crypto.randomUUID(),
            storeId: storeId as string,
            itemId: reStockQuantity.id,
            itemName: updatedItem.name,
            reason: 'Restock',
            delta: payload.quantity,
            timestamp: Date.now(),
            isDeleted: false,
            updatedAt: Date.now()
          });

          // Create Purchase & PurchaseItem records to match Android behavior
          const purchaseId = crypto.randomUUID();
          const totalAmount = payload.quantity * (payload.buyPrice || 0);
          const taxAmount = totalAmount * ((reStockQuantity.taxRate || 0) / 100);

          await syncPurchase(dataConnect, {
            id: purchaseId,
            storeId: storeId as string,
            supplierId: payload.supplierId || '',
            supplierName: payload.supplierName || 'Cash / Anonymous',
            totalAmount,
            taxAmount,
            type: 'BILL',
            timestamp: Date.now(),
            notes: `Refill stock for ${reStockQuantity.name}`,
            isDeleted: false,
            updatedAt: Math.floor(Date.now() / 1000)
          });

          await syncPurchaseItem(dataConnect, {
            id: crypto.randomUUID(),
            storeId: storeId as string,
            purchaseId: purchaseId,
            itemId: reStockQuantity.id,
            itemName: reStockQuantity.name,
            quantity: payload.quantity,
            unit: reStockQuantity.unit || 'pcs',
            buyPrice: payload.buyPrice || 0,
            isDeleted: false,
            updatedAt: Math.floor(Date.now() / 1000)
          });

          if (payload.batchNumber || payload.expiryDate) {
            await syncItemBatch(dataConnect, {
              id: crypto.randomUUID(),
              storeId: storeId as string,
              itemId: reStockQuantity.id,
              batchNumber: payload.batchNumber || null,
              expiryDate: payload.expiryDate ? new Date(payload.expiryDate).getTime() : null,
              quantity: payload.quantity,
              costPrice: payload.buyPrice || 0,
              timestamp: Date.now(),
              notes: 'Added via restock',
              isDeleted: false,
              updatedAt: Math.floor(Date.now() / 1000)
            });
          }

          invalidateAllPages();
          setCurrentPage(1);
          setRefreshTrigger(prev => prev + 1);
          setReStockQuantity(null);
        }}
      />
    </div>
  );
}
