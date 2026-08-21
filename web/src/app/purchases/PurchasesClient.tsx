"use client";

import React, { useState, useEffect, useRef, useCallback } from "react";
import {
  Search,
  ChevronDown,
  ChevronRight,
  Loader2,
  ShoppingBag,
} from "lucide-react";
import { dataConnect } from "@/lib/firebase";
import { executeQuery } from "firebase/data-connect";
import {
  listPurchasesRef,
  getPurchasesCountRef,
  getPurchaseDetailsRef,
  OrderDirection,
  updatePurchaseItemPrice,
  updatePurchaseTotalAmount
} from "@/dataconnect";
import { recalculateItemFIFO } from '@/lib/fifoCalculator';
import { FormattedAmount } from "@/components/FormattedAmount";
import Pagination from "@/app/components/Pagination";

interface PurchaseItem {
  id: string;
  itemId: string;
  itemName: string;
  quantity: number;
  unit: string;
  buyPrice: number;
}

interface PurchaseEntry {
  id: string;
  supplierName: string;
  totalAmount: number;
  timestamp: number;
  notes?: string | null;
  purchaseItemDetails_on_purchase: PurchaseItem[];
}

export default function PurchasesClient({
  storeId,
  isPremium,
}: {
  storeId?: string;
  isPremium?: boolean;
}) {
  const [purchases, setPurchases] = useState<PurchaseEntry[]>([]);

  const [currentPage, setCurrentPage] = useState(1);
  const [totalItems, setTotalItems] = useState(0);
  const [isLoading, setIsLoading] = useState(true);
  const pageSize = 10;

  const [searchQuery, setSearchQuery] = useState("");
  const [minAmountFilter, setMinAmountFilter] = useState("");
  const [maxAmountFilter, setMaxAmountFilter] = useState("");

  const handleSearchChange = (val: string) => {
    setSearchQuery(val.replace(/[<>{}]/g, ""));
    setCurrentPage(1);
  };

  const [sortField, setSortField] = useState<string | null>("timestamp");
  const [sortDirection, setSortDirection] = useState<"asc" | "desc">("desc");
  const [reloadTrigger, setReloadTrigger] = useState(0);
  const [editingItem, setEditingItem] = useState<{ purchaseId: string; item: PurchaseItem } | null>(null);
  const [newPrice, setNewPrice] = useState<string>('');
  const [isUpdating, setIsUpdating] = useState(false);

  const handleEditPriceClick = (purchaseId: string, item: PurchaseItem) => {
    setEditingItem({ purchaseId, item });
    setNewPrice(item.buyPrice.toString());
  };

  const handleSavePrice = async () => {
    if (!editingItem || !newPrice || isNaN(parseFloat(newPrice))) return;
    setIsUpdating(true);
    try {
      const parsedPrice = parseFloat(newPrice);
      const { item, purchaseId } = editingItem;

      // 1. Update purchase item price in DB
      await updatePurchaseItemPrice(dataConnect, {
        id: item.id,
        buyPrice: parsedPrice,
        updatedAt: Date.now()
      });

      // 2. Compute and update parent purchase totalAmount
      const parentPurchase = purchases.find(p => p.id === purchaseId);
      if (parentPurchase) {
        const otherItemsTotal = (parentPurchase.purchaseItemDetails_on_purchase || [])
          .filter((i: PurchaseItem) => i.id !== item.id)
          .reduce((sum: number, i: PurchaseItem) => sum + (i.buyPrice * i.quantity), 0);
        const newTotalAmount = otherItemsTotal + (parsedPrice * item.quantity);

        await updatePurchaseTotalAmount(dataConnect, {
          id: purchaseId,
          totalAmount: newTotalAmount,
          updatedAt: Date.now()
        });
      }

      // 3. Recalculate FIFO cost allocation for this item
      await recalculateItemFIFO(storeId || '', item.itemId, dataConnect, purchaseId);

      // Optimistically update local purchases state immediately so UI refreshes without manual reload
      setPurchases(prevPurchases =>
        prevPurchases.map(p => {
          if (p.id === purchaseId) {
            const updatedItems = (p.purchaseItemDetails_on_purchase || []).map((pi: PurchaseItem) =>
              pi.id === item.id ? { ...pi, buyPrice: parsedPrice } : pi
            );
            const otherItemsTotal = (p.purchaseItemDetails_on_purchase || [])
              .filter((i: PurchaseItem) => i.id !== item.id)
              .reduce((sum: number, i: PurchaseItem) => sum + (i.buyPrice * i.quantity), 0);
            const newTotalAmount = otherItemsTotal + (parsedPrice * item.quantity);
            return {
              ...p,
              totalAmount: newTotalAmount,
              purchaseItemDetails_on_purchase: updatedItems
            };
          }
          return p;
        })
      );

      // Refresh data from server
      setReloadTrigger(prev => prev + 1);
      setEditingItem(null);
    } catch (e) {
      console.error('Failed to update purchase price:', e);
      alert('Error updating purchase price. Please try again.');
    } finally {
      setIsUpdating(false);
    }
  };

  const buildSortVars = (field: string | null, direction: "asc" | "desc") => {
    if (!field) return { orderByTimestamp: OrderDirection.DESC };
    const dir =
      direction === "asc" ? OrderDirection.ASC : OrderDirection.DESC;
    return {
      orderByTimestamp: field === "timestamp" ? dir : undefined,
      orderBySupplierName: field === "supplier_name" ? dir : undefined,
      orderByTotalAmount: field === "total_amount" ? dir : undefined,
    };
  };

  const handleSort = (field: string) => {
    setCurrentPage(1);
    if (field === sortField) {
      setSortDirection((prev) => (prev === "asc" ? "desc" : "asc"));
    } else {
      setSortField(field);
      setSortDirection("asc");
    }
  };

  // Fetch count
  useEffect(() => {
    if (!storeId) return;

    const fetchTotal = async () => {
      try {
        const resp = await executeQuery(
          getPurchasesCountRef(dataConnect, { storeId }),
          { fetchPolicy: 'SERVER_ONLY' as const }
        );
        setTotalItems(resp.data.purchases.length);
      } catch (e) {
        console.error("Count fetch error:", e);
      }
    };
    fetchTotal();
  }, [storeId, reloadTrigger]);

  // Fetch purchases for current page
  useEffect(() => {
    if (!storeId) return;
    let isMounted = true;

    const fetchPurchases = async () => {
      setIsLoading(true);
      try {
        const resp = await executeQuery(
          listPurchasesRef(dataConnect, {
            storeId,
            limit: pageSize,
            offset: (currentPage - 1) * pageSize,
            ...buildSortVars(sortField, sortDirection),
          }),
          { fetchPolicy: 'SERVER_ONLY' as const }
        );

        if (isMounted) {
          setPurchases(resp.data.purchases);
        }
      } catch (err) {
        console.error("Error fetching purchases:", err);
      } finally {
        if (isMounted) {
          setIsLoading(false);
        }
      }
    };

    fetchPurchases();
    return () => {
      isMounted = false;
    };
  }, [storeId, currentPage, sortField, sortDirection, reloadTrigger]);

  const filteredPurchases = purchases.filter((p) => {
    if (searchQuery) {
      const q = searchQuery.toLowerCase();
      const matchesSupplier = p.supplierName?.toLowerCase().includes(q);
      const matchesNotes = p.notes?.toLowerCase().includes(q);
      if (!matchesSupplier && !matchesNotes) return false;
    }
    if (minAmountFilter && p.totalAmount < parseFloat(minAmountFilter))
      return false;
    if (maxAmountFilter && p.totalAmount > parseFloat(maxAmountFilter))
      return false;
    return true;
  });

  return (
    <div className="flex-1 overflow-y-auto p-4 md:p-6 bg-gray-50 dark:bg-gray-900">
      <div className="mb-6 flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold text-gray-900 dark:text-white flex items-center gap-2">
            <ShoppingBag className="h-6 w-6 text-teal-600 dark:text-teal-400" />
            Purchase History
          </h1>
          <p className="text-sm text-gray-500 dark:text-gray-400 mt-1">
            View history of all items added or restocked
          </p>
        </div>
      </div>

      <div className="glass-card overflow-hidden flex flex-col min-h-[500px]">
        <div className="p-4 border-b border-gray-100 dark:border-gray-800 bg-gray-50/50 dark:bg-gray-900/50">
          <div className="flex flex-col sm:flex-row gap-4 justify-between items-start sm:items-center w-full">
            <div className="relative w-64">
              <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                <Search size={16} className="text-gray-400" />
              </div>
              <input
                aria-label="text"
                type="text"
                value={searchQuery}
                onChange={(e) =>
                  handleSearchChange(e.target.value)
                }
                className="block w-full pl-10 pr-3 py-2 border border-gray-200 dark:border-gray-700 bg-white dark:bg-gray-800 rounded-lg text-sm placeholder-gray-400 dark:placeholder-gray-500 focus:outline-none focus:ring-2 focus:ring-teal-500 focus:border-transparent transition-all dark:text-gray-100"
                placeholder="Search by supplier or notes..."
              />
            </div>

            <div className="flex flex-wrap items-center gap-4">
              <div className="flex items-center gap-2 bg-white dark:bg-gray-800 border border-gray-200 dark:border-gray-700 rounded-lg px-2 py-1">
                <span className="text-xs text-gray-500 dark:text-gray-400 mr-1">
                  Amount Range:
                </span>
                <input
                  type="number"
                  placeholder="Min"
                  value={minAmountFilter}
                  onChange={(e) => {
                    setMinAmountFilter(e.target.value);
                    setCurrentPage(1);
                  }}
                  className="w-16 px-1 py-1 bg-transparent border-none text-sm text-gray-900 dark:text-white outline-none focus:ring-0"
                />
                <span className="text-gray-300 dark:text-gray-600">
                  -
                </span>
                <input
                  type="number"
                  placeholder="Max"
                  value={maxAmountFilter}
                  onChange={(e) => {
                    setMaxAmountFilter(e.target.value);
                    setCurrentPage(1);
                  }}
                  className="w-16 px-1 py-1 bg-transparent border-none text-sm text-gray-900 dark:text-white outline-none focus:ring-0"
                />
              </div>
            </div>
          </div>
        </div>

        {isLoading ? (
          <div className="flex-1 flex items-center justify-center p-12">
            <Loader2 className="w-8 h-8 text-teal-500 animate-spin" />
          </div>
        ) : filteredPurchases.length === 0 ? (
          <div className="flex-1 flex flex-col items-center justify-center p-12 text-gray-500 dark:text-gray-400">
            <ShoppingBag className="w-12 h-12 mb-4 text-gray-300 dark:text-gray-600" />
            <p className="text-lg font-medium">
              No purchases found
            </p>
            <p className="text-sm">
              Try adjusting your search or filters.
            </p>
          </div>
        ) : (
          <>
            <div className="overflow-x-auto">
              <table className="w-full text-left border-collapse">
                <thead>
                  <tr className="bg-gray-50 dark:bg-gray-900/50 border-b border-gray-200 dark:border-gray-700">
                    <th className="px-4 py-3 text-xs font-semibold text-gray-500 dark:text-gray-400 uppercase tracking-wider">
                      <button
                        type="button"
                        onClick={() =>
                          handleSort("timestamp")
                        }
                        className="inline-flex items-center gap-1 hover:text-gray-700 dark:hover:text-gray-200"
                      >
                        Date{" "}
                        {sortField === "timestamp"
                          ? sortDirection === "asc"
                            ? "▲"
                            : "▼"
                          : "↕"}
                      </button>
                    </th>
                    <th className="px-4 py-3 text-xs font-semibold text-gray-500 dark:text-gray-400 uppercase tracking-wider">
                      <button
                        type="button"
                        onClick={() =>
                          handleSort("supplier_name")
                        }
                        className="inline-flex items-center gap-1 hover:text-gray-700 dark:hover:text-gray-200"
                      >
                        Supplier{" "}
                        {sortField === "supplier_name"
                          ? sortDirection === "asc"
                            ? "▲"
                            : "▼"
                          : "↕"}
                      </button>
                    </th>
                    <th className="px-4 py-3 text-xs font-semibold text-gray-500 dark:text-gray-400 uppercase tracking-wider">
                      Items
                    </th>
                    <th className="px-4 py-3 text-xs font-semibold text-gray-500 dark:text-gray-400 uppercase tracking-wider text-right">
                      <button
                        type="button"
                        onClick={() =>
                          handleSort("total_amount")
                        }
                        className="inline-flex items-center justify-end gap-1 w-full hover:text-gray-700 dark:hover:text-gray-200"
                      >
                        Total Amount{" "}
                        {sortField === "total_amount"
                          ? sortDirection === "asc"
                            ? "▲"
                            : "▼"
                          : "↕"}
                      </button>
                    </th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-gray-200 dark:divide-gray-700">
                  {filteredPurchases.map((purchase) => {
                    const items =
                      purchase.purchaseItemDetails_on_purchase ||
                      [];

                    return (
                      <tr key={purchase.id} className="hover:bg-gray-50 dark:hover:bg-gray-800/50 transition-colors">
                        <td className="px-4 py-4 whitespace-nowrap text-sm text-gray-900 dark:text-gray-200">
                          {new Date(purchase.timestamp).toLocaleDateString()} {new Date(purchase.timestamp).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                        </td>
                        <td className="px-4 py-4 whitespace-nowrap">
                          <div className="text-sm font-medium text-gray-900 dark:text-gray-100">{purchase.supplierName}</div>
                          {purchase.notes && (
                            <div className="text-xs text-gray-500 dark:text-gray-400">{purchase.notes}</div>
                          )}
                        </td>
                        <td className="px-4 py-4">
                          <div className="flex flex-col gap-2">
                            {items.map((item: PurchaseItem) => (
                              <div key={item.id} className="text-sm text-gray-700 dark:text-gray-300 flex items-center justify-between gap-4 bg-gray-50 dark:bg-gray-800/40 p-2 rounded-lg border border-gray-100 dark:border-gray-800">
                                <div>
                                  <span className="font-medium text-gray-900 dark:text-white">{item.itemName}</span>{' '}
                                  <span className="text-gray-400 dark:text-gray-500 text-xs">({item.quantity} {item.unit})</span>
                                </div>
                                <div className="flex items-center gap-2">
                                  <span className="text-xs font-semibold text-teal-600 dark:text-teal-400 bg-teal-50 dark:bg-teal-950/30 px-2 py-0.5 rounded">
                                    <FormattedAmount amount={item.buyPrice} />
                                  </span>
                                  <button
                                    type="button"
                                    onClick={() => handleEditPriceClick(purchase.id, item)}
                                    className="text-xs text-gray-500 hover:text-teal-600 dark:text-gray-400 dark:hover:text-teal-400 transition-colors bg-white dark:bg-gray-700 hover:bg-teal-50 dark:hover:bg-teal-950/20 px-2 py-1 rounded border border-gray-200 dark:border-gray-600 font-medium"
                                  >
                                    Edit Price
                                  </button>
                                </div>
                              </div>
                            ))}
                          </div>
                        </td>
                        <td className="px-4 py-4 whitespace-nowrap text-right font-medium text-gray-900 dark:text-gray-100">
                          <FormattedAmount
                            amount={
                              purchase.totalAmount
                            }
                          />
                        </td>
                      </tr>
                    );
                  })}
                </tbody>
              </table>
            </div>

            <div className="mt-auto border-t border-gray-200 dark:border-gray-700 p-4">
              <Pagination
                currentPage={currentPage}
                totalItems={totalItems}
                pageSize={pageSize}
                onPageChange={setCurrentPage}
              />
            </div>
          </>
        )}
      </div>
      {editingItem && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/60 backdrop-blur-sm">
          <div className="bg-white dark:bg-gray-800 rounded-xl max-w-md w-full shadow-2xl border border-gray-200 dark:border-gray-700 overflow-hidden p-6">
            <h3 className="text-lg font-bold text-gray-900 dark:text-white mb-2">
              Edit Purchase Price
            </h3>
            <p className="text-xs text-gray-500 dark:text-gray-400 mb-4">
              Editing the purchase price for <span className="font-semibold text-gray-800 dark:text-gray-200">{editingItem.item.itemName}</span>.
              This will run a FIFO cost recalculation for all matching sales of this item.
            </p>

            <div className="mb-6">
              <label className="block text-xs font-medium text-gray-500 dark:text-gray-400 mb-2">
                New Purchase Price per {editingItem.item.unit}
              </label>
              <input
                type="number"
                step="0.01"
                value={newPrice}
                onChange={(e) => setNewPrice(e.target.value)}
                className="block w-full px-3 py-2 border border-gray-200 dark:border-gray-700 bg-white dark:bg-gray-900 rounded-lg text-sm placeholder-gray-400 dark:placeholder-gray-500 focus:outline-none focus:ring-2 focus:ring-teal-500 focus:border-transparent transition-all dark:text-gray-100"
                placeholder="0.00"
                disabled={isUpdating}
                autoFocus
              />
            </div>

            <div className="flex justify-end gap-3">
              <button
                type="button"
                onClick={() => setEditingItem(null)}
                className="px-4 py-2 text-sm font-medium text-gray-600 dark:text-gray-300 bg-gray-100 hover:bg-gray-200 dark:bg-gray-700 dark:hover:bg-gray-600 rounded-lg transition-colors"
                disabled={isUpdating}
              >
                Cancel
              </button>
              <button
                type="button"
                onClick={handleSavePrice}
                className="px-4 py-2 text-sm font-medium text-white bg-teal-600 hover:bg-teal-700 dark:bg-teal-500 dark:hover:bg-teal-600 rounded-lg transition-colors flex items-center gap-2"
                disabled={isUpdating || !newPrice || isNaN(parseFloat(newPrice))}
              >
                {isUpdating ? (
                  <>
                    <Loader2 size={16} className="animate-spin" />
                    Updating...
                  </>
                ) : (
                  'Save Changes'
                )}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
