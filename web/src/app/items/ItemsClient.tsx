'use client';

import { useState, useEffect } from 'react';
import { Plus, Search, Trash2, Edit2, Loader2, ArrowDownCircle } from 'lucide-react';
import { addItem, updateItem, deleteItem, fetchMoreData } from '@/app/actions';
import ExportButtons from '@/app/ExportButtons';
import { dataConnect } from '@/lib/firebase';
import { getActiveItems, syncItem, softDeleteItem } from '@/dataconnect';
import { FormattedAmount } from '@/components/FormattedAmount';
import RestockQuantity from '@/components/models/RestockQuantity';

type UnitOption = 'pcs' | 'kg' | 'g' | 'litre' | 'ml' | 'dozen' | 'box' | 'packet';

type ItemFormData = {
  name: string;
  category: string;
  quantity: number;
  unit: UnitOption;
  buy_price: number;
  sell_price: number;
  low_stock_threshold: number;

  hsn_code: string;
  tax_rate: number;

  batch_lot_number: string;
  expiry_date: string; // yyyy-mm-dd
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
    hsn_code: '',
    tax_rate: 0,
    batch_lot_number: '',
    expiry_date: ''
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
  const [items, setItems] = useState(initialItems);

  useEffect(() => {
    if (!isPremium || !storeId) return;

    let isMounted = true;
    const fetchItems = async () => {
      try {
        const response = await getActiveItems(dataConnect, { storeId });
        if (!isMounted) return;
        
        const updated = response.data.items.map((item: any) => ({
          ...item,
          is_deleted: 0,
          updated_at: item.updatedAt || Date.now(),
          buy_price: item.buyPrice,
          sell_price: item.sellPrice
        }));

        if (userRole === 'staff') {
          updated.forEach((item: any) => {
            if (item.buy_price !== undefined) delete item.buy_price;
          });
        }

        setItems(updated);
      } catch (error) {
        console.error("Data Connect items sync error:", error);
      }
    };

    fetchItems();
    // Optional: poll every 30 seconds for new items
    const intervalId = setInterval(fetchItems, 30000);

    return () => {
      isMounted = false;
      clearInterval(intervalId);
    };
  }, [isPremium, storeId, userRole]);
  const [showModal, setShowModal] = useState(false);
  const [editingId, setEditingId] = useState<string | null>(null);
  const [searchQuery, setSearchQuery] = useState('');
  const [loadingMore, setLoadingMore] = useState(false);
  const [hasMore, setHasMore] = useState(initialItems.length === 20);
  const [formData, setFormData] = useState<ItemFormData>(() => emptyFormData());
  const [showAdvanced, setShowAdvanced] = useState(false);
  const [reStockQuantity, setReStockQuantity] = useState<any | null>(null);

  const handleLoadMore = async () => {
    if (loadingMore || !hasMore || items.length === 0) return;
    setLoadingMore(true);
    try {
      const lastItem = items[items.length - 1];
      const nextBatch = await fetchMoreData('items', lastItem.updated_at || lastItem.timestamp || Date.now(), 20);
      if (nextBatch.length < 20) {
        setHasMore(false);
      }
      setItems(prev => [...prev, ...nextBatch]);
    } catch (error) {
      console.error("Load more failed", error);
    } finally {
      setLoadingMore(false);
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      const payload = !showAdvanced ? {
          hsn_code : '',
          tax_rate : 0,
          batch_lot_number : '',
          expiry_date : '',
        } : {};
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
      setShowModal(false);
      // Let polling or a forced refetch handle it, but for simple UX:
      window.location.reload(); 
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

      hsn_code: item.hsn_code || '',
      tax_rate: item.tax_rate || 0,

      batch_lot_number: item.batch_lot_number || '',
      expiry_date: item.expiry_date || ''
    };

    setFormData(next);
    setEditingId(item.id);

    const shouldShow =
      !!next.hsn_code ||
      !!next.tax_rate ||
      !!next.batch_lot_number ||
      !!next.expiry_date;

    setShowAdvanced(shouldShow);
    setShowModal(true);
  };

  const handleDelete = async (id: string) => {
    if (confirm('Are you sure you want to delete this item?')) {
      try {
        await softDeleteItem(dataConnect, { id, updatedAt: Math.floor(Date.now() / 1000) });
        window.location.reload();
      } catch (err) {
        console.error("Failed to delete item:", err);
      }
    }
  };

  const openCreate = () => {
    setEditingId(null);
    setFormData(emptyFormData());
    setShowAdvanced(false);
    setShowModal(true);
  };

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center mb-6">
        <div>
          <h1 className="text-2xl font-bold text-gray-900 dark:text-white">Inventory Items</h1>
          <p className="text-gray-500 dark:text-gray-400 text-sm mt-1">Manage your store's products and stock levels.</p>
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
        <div className="p-4 border-b border-gray-100 dark:border-gray-800 flex justify-between items-center bg-gray-50/50 dark:bg-gray-900/50">
          <div className="relative w-64">
            <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
              <Search size={16} className="text-gray-400" />
            </div>
            <input
              type="text"
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              className="block w-full pl-10 pr-3 py-2 border border-gray-200 dark:border-gray-700 bg-white dark:bg-gray-800 rounded-lg text-sm placeholder-gray-400 dark:placeholder-gray-500 focus:outline-none focus:ring-2 focus:ring-teal-500 focus:border-transparent transition-all dark:text-gray-100"
              placeholder="Search items by name or category..."
            />
          </div>
        </div>

        <div className="overflow-x-auto">
          <table className="w-full text-left text-sm text-gray-600 dark:text-gray-300">
            <thead className="bg-gray-50/50 dark:bg-gray-900/50 text-gray-500 dark:text-gray-400 text-xs uppercase tracking-wider border-b border-gray-100 dark:border-gray-800">
              <tr>
                <th className="px-6 py-4 text-left text-xs font-semibold text-gray-500 dark:text-gray-400 uppercase tracking-wider">Item Name</th>
                <th className="px-6 py-4 text-left text-xs font-semibold text-gray-500 dark:text-gray-400 uppercase tracking-wider">Category</th>
                <th className="px-6 py-4 text-left text-xs font-semibold text-gray-500 dark:text-gray-400 uppercase tracking-wider">Stock Level</th>
                {userRole !== 'staff' && (
                  <th className="px-6 py-4 text-left text-xs font-semibold text-gray-500 dark:text-gray-400 uppercase tracking-wider">Buy Price</th>
                )}
                <th className="px-6 py-4 text-left text-xs font-semibold text-gray-500 dark:text-gray-400 uppercase tracking-wider">Sell Price</th>
                {userRole !== 'staff' && (
                  <th className="px-6 py-4 text-left text-xs font-semibold text-gray-500 dark:text-gray-400 uppercase tracking-wider">Margin</th>
                )}
                <th className="px-6 py-4 text-right text-xs font-semibold text-gray-500 dark:text-gray-400 uppercase tracking-wider">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-50 dark:divide-gray-800">
              {(() => {
                const filteredItems = items.filter((item: any) => 
                  (item.name || '').toLowerCase().includes(searchQuery.toLowerCase()) || 
                  (item.category || '').toLowerCase().includes(searchQuery.toLowerCase())
                );

                if (filteredItems.length === 0) {
                  return (
                    <tr>
                      <td colSpan={7} className="px-6 py-8 text-center text-gray-500 dark:text-gray-400">
                        No items found matching your search.
                      </td>
                    </tr>
                  );
                }

                return (
                  <>
                    {filteredItems.map((item: any) => (
                      <tr key={item.id} className="hover:bg-gray-50/50 dark:hover:bg-gray-800/50 transition-colors">
                  <td className="px-6 py-4 font-medium text-gray-900 dark:text-gray-100">{item.name}</td>
                  <td className="px-6 py-4">
                    <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-gray-100 dark:bg-gray-800 text-gray-800 dark:text-gray-300 border border-gray-200 dark:border-gray-700">
                      {item.category || 'Uncategorized'}
                    </span>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900 dark:text-white">
                    {item.quantity} <span className="text-gray-400 dark:text-gray-500 text-xs">{item.unit}</span>
                  </td>
                  {userRole !== 'staff' && (
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900 dark:text-white">
                        <FormattedAmount amount={item.buy_price} />
                      </td>
                  )}
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900 dark:text-white font-medium">
                    <FormattedAmount amount={item.sell_price} />
                  </td>
                  {userRole !== 'staff' && (
                      <td className="px-6 py-4 whitespace-nowrap text-sm">
                        {item.buy_price > 0 ? (
                          <span className={`px-2 py-1 rounded text-xs font-bold ${((item.sell_price - item.buy_price) / item.buy_price * 100) >= 0 ? 'bg-emerald-100 text-emerald-700 dark:bg-emerald-900/30 dark:text-emerald-400' : 'bg-red-100 text-red-700 dark:bg-red-900/30 dark:text-red-400'}`}>
                            {(((item.sell_price - item.buy_price) / item.buy_price) * 100).toFixed(0)}%
                          </span>
                        ) : '-'}
                      </td>
                  )}
                  <td className="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
                    <button 
                      onClick={() => setReStockQuantity(item)}
                      className="text-green-600 dark:text-green-400 hover:text-green-900 dark:hover:text-green-300 mr-4 transition-colors"
                      title="Restock"
                    >
                      <Plus size={18} />
                    </button>
                    <button 
                      onClick={() => handleEdit(item)}
                      className="text-indigo-600 dark:text-indigo-400 hover:text-indigo-900 dark:hover:text-indigo-300 mr-4 transition-colors"
                      title="Edit"
                    >
                      <Edit2 size={18} />
                    </button>
                    {userRole !== 'staff' && (
                        <button 
                          onClick={() => handleDelete(item.id)}
                          className="text-red-500 hover:text-red-700 transition-colors"
                          title="Delete"
                        >
                          <Trash2 size={18} />
                        </button>
                    )}
                  </td>
                </tr>
                    ))}
                  </>
                );
              })()}
            </tbody>
          </table>
        </div>
        
        {hasMore && !searchQuery && (
          <div className="p-4 border-t border-gray-100 dark:border-gray-800 flex justify-center">
            <button 
              onClick={handleLoadMore} 
              disabled={loadingMore}
              className="px-6 py-2 bg-gray-100 dark:bg-gray-800 text-gray-700 dark:text-gray-300 rounded-xl text-sm font-medium hover:bg-gray-200 dark:hover:bg-gray-700 transition-colors flex items-center gap-2"
            >
              {loadingMore ? <Loader2 size={16} className="animate-spin" /> : <ArrowDownCircle size={16} />}
              Load More from Server
            </button>
          </div>
        )}
      </div>

      {showModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 backdrop-blur-sm">
          <div className="bg-white dark:bg-gray-900 rounded-xl shadow-xl w-full max-w-md p-6">
            <h2 className="text-xl font-bold mb-4 dark:text-white">{editingId ? 'Edit Item' : 'Add New Item'}</h2>
            <form onSubmit={handleSubmit} className="space-y-4">
              <div>
                <label className="block text-sm font-medium dark:text-gray-300">Name</label>
                <input required type="text" value={formData.name} onChange={e => setFormData({...formData, name: e.target.value})} className="mt-1 w-full p-2 border dark:border-gray-700 rounded dark:bg-gray-800 dark:text-white" />
              </div>
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-medium dark:text-gray-300">Category</label>
                  <input type="text" value={formData.category} onChange={e => setFormData({...formData, category: e.target.value})} className="mt-1 w-full p-2 border dark:border-gray-700 rounded dark:bg-gray-800 dark:text-white" />
                </div>
                <div>
                  <label className="block text-sm font-medium dark:text-gray-300">Unit</label>
                  <select
                    required
                    value={formData.unit}
                    onChange={(e) => setFormData({ ...formData, unit: e.target.value as UnitOption })}
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
                  <input required type="number" step="any" value={formData.quantity} onChange={e => setFormData({...formData, quantity: parseFloat(e.target.value)})} className="mt-1 w-full p-2 border dark:border-gray-700 rounded dark:bg-gray-800 dark:text-white" />
                </div>
                <div>
                  <label className="block text-sm font-medium dark:text-gray-300">Low Stock Alert</label>
                  <input required type="number" step="any" value={formData.low_stock_threshold} onChange={e => setFormData({...formData, low_stock_threshold: parseFloat(e.target.value)})} className="mt-1 w-full p-2 border dark:border-gray-700 rounded dark:bg-gray-800 dark:text-white" />
                </div>
              </div>
              <div className="grid grid-cols-2 gap-4">
                {userRole !== 'staff' && (
                  <div>
                    <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">Buy Price (₹)</label>
                    <input 
                      type="number" 
                      step="0.01"
                      required 
                      value={formData.buy_price || ''} 
                      onChange={(e) => setFormData({...formData, buy_price: parseFloat(e.target.value) || 0})}
                      className="mt-1 w-full p-2 border dark:border-gray-700 rounded dark:bg-gray-800 dark:text-white"
                      placeholder="0.00"
                    />
                  </div>
                )}
                <div>
                  <label className="block text-sm font-medium dark:text-gray-300">Sell Price</label>
                  <input
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
                      <input
                        type="text"
                        value={formData.hsn_code}
                        onChange={(e) => setFormData({ ...formData, hsn_code: e.target.value })}
                        className="mt-1 w-full p-2 border dark:border-gray-700 rounded dark:bg-gray-800 dark:text-white"
                      />
                    </div>
                    <div>
                      <label className="block text-sm font-medium dark:text-gray-300">Tax Rate (%)</label>
                      <input
                        type="number"
                        step="any"
                        value={formData.tax_rate}
                        onChange={(e) => setFormData({ ...formData, tax_rate: parseFloat(e.target.value) || 0 })}
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
                        <input
                          type="text"
                          value={formData.batch_lot_number}
                          onChange={(e) =>
                            setFormData({ ...formData, batch_lot_number: e.target.value })
                          }
                          className="mt-1 w-full p-2 border dark:border-gray-700 rounded dark:bg-gray-800 dark:text-white"
                        />
                      </div>
                      <div>
                        <label className="block text-sm font-medium dark:text-gray-300">Expiry Date</label>
                        <input
                          type="date"
                          value={formData.expiry_date}
                          onChange={(e) =>
                            setFormData({
                              ...formData,
                              expiry_date: e.target.value
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
      {/* <RestockQuantity
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
            updated_at: Date.now(),
          };

          setItems((prev) => prev.map((item: any) => item.id === reStockQuantity.id ? updatedItem : item));
          await updateItem(reStockQuantity.id, updatedItem);
          setReStockQuantity(null);
        }}
      /> */}
    </div>
  );
}
