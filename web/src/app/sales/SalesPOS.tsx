'use client';

import { useState, useEffect } from 'react';
import { Search, Plus, Minus, Trash2, X, ShoppingCart, Loader2 } from 'lucide-react';
import { dataConnect } from '@/lib/firebase';
import { getActiveItems, syncSale, syncSaleItem, syncItem, syncUdhaar } from '@/dataconnect';
import { FormattedAmount } from '@/components/FormattedAmount';
import { useDispatch, useSelector } from 'react-redux';
import { RootState } from '@/store';
import { sanitizeInput } from '@/lib/sanitize';
import { addToCart, updateQuantity, removeFromCart, clearCart } from '@/store/cartSlice';
import { setInventory } from '@/store/inventorySlice';
import { BillingEngine } from '@/lib/BillingEngine';
import { InvoicePdfGenerator } from '@/lib/InvoicePdfGenerator';

interface Item {
  id: string;
  name: string;
  quantity: number;
  unit: string;
  buyPrice: number;
  sellPrice: number;
  lowStockThreshold: number;
  category: string;
  is_deleted?: number;
  photoPath?: string;
  hsnCode?: string;
  barcode?: string;
  taxRate?: number;
}

interface CartItem {
  item: Item;
  quantity: number;
}

export default function SalesPOS({
  storeId,
  type = 'SALE',
  userRole = 'owner',
  onClose,
  onSuccess
}: {
  storeId: string;
  type?: 'SALE' | 'ESTIMATE';
  userRole?: string;
  onClose: () => void;
  onSuccess: () => void;
}) {
  const dispatch = useDispatch();
  const cachedInventory = useSelector((state: RootState) => state.inventory);
  const cartState = useSelector((state: RootState) => state.cart.items);

  const [items, setItems] = useState<Item[]>(cachedInventory.items || []);
  const [loading, setLoading] = useState(!cachedInventory.items || cachedInventory.items.length === 0);
  const [searchQuery, setSearchQuery] = useState('');
  // Re-map the Redux items to the local CartItem interface shape
  const cart = cartState.map(i => ({
    item: {
      id: i.id,
      name: i.name,
      quantity: i.maxStock,
      unit: i.unit,
      buyPrice: i.buy_price,
      sellPrice: i.sell_price,
      lowStockThreshold: 0,
      category: '',
      taxRate: i.taxRate,
      hsnCode: i.hsnCode
    },
    quantity: i.quantity
  }));

  const [customerName, setCustomerName] = useState('');
  const [notes, setNotes] = useState('');
  const [discount, setDiscount] = useState<number>(0);
  const [isSaving, setIsSaving] = useState(false);

  const [paymentMode, setPaymentMode] = useState<'Cash' | 'UPI' | 'Udhaar' | 'Estimate'>(type === 'ESTIMATE' ? 'Estimate' : 'Cash');
  const [showAdditionalDetails, setShowAdditionalDetails] = useState(false);
  const [customerGstin, setCustomerGstin] = useState('');
  const [customerAddress, setCustomerAddress] = useState('');

  const getUnitIncrement = (unit: string) => {
    console.log("unit", unit);
    const u = (unit || '').toLowerCase();
    if (u === 'grams' || u === 'ml') return 50;
    if (u === 'litre' || u === 'l' || u === 'kg') return 0.5;
    return 1;
  };

  useEffect(() => {
    let isMounted = true;
    const fetchItems = async () => {
      // If we have cached items and they are less than 5 minutes old, skip hard loading state
      const isCacheValid = cachedInventory.items.length > 0 && (Date.now() - cachedInventory.lastSynced < 5 * 60 * 1000);
      if (isCacheValid && isMounted) {
        setItems(cachedInventory.items);
        setLoading(false);
      }

      try {
        const response = await getActiveItems(dataConnect, { storeId });
        if (!isMounted) return;

        const updated = response.data.items.map((item: any) => ({
          ...item,
          buyPrice: item.buyPrice || 0,
          sellPrice: item.sellPrice || 0,
        }));

        // Update local state and Redux cache
        setItems(updated);
        dispatch(setInventory(updated));
      } catch (error) {
        console.error("Failed to fetch items for POS:", error);
      } finally {
        if (isMounted) setLoading(false);
      }
    };
    fetchItems();
    return () => { isMounted = false; };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [storeId, dispatch]);

  // Barcode Scanner listener
  useEffect(() => {
    let barcode = '';
    let lastKeyTime = Date.now();

    const handleKeyDown = (e: KeyboardEvent) => {
      // Ignore if user is typing in an input/textarea
      const target = e.target as HTMLElement;
      if (target.tagName === 'INPUT' || target.tagName === 'TEXTAREA') return;

      const currentTime = Date.now();

      // If time between keystrokes is more than 50ms, reset (it's human typing)
      if (currentTime - lastKeyTime > 50) {
        barcode = '';
      }

      // If Enter is pressed and we have a barcode, process it
      if (e.key === 'Enter' && barcode.length > 2) {
        // Find item by ID or Name or HSN (since we don't have a specific barcode column)
        const scannedItem = items.find(
          item => item.barcode === barcode || item.hsnCode === barcode || item.id === barcode || item.name.toLowerCase() === barcode.toLowerCase()
        );

        if (scannedItem) {
          dispatch(addToCart({
            id: scannedItem.id,
            name: scannedItem.name,
            quantity: 1,
            sell_price: scannedItem.sellPrice,
            buy_price: scannedItem.buyPrice,
            unit: scannedItem.unit || 'pcs',
            maxStock: scannedItem.quantity,
            taxRate: scannedItem.taxRate,
            hsnCode: scannedItem.hsnCode
          }));
          // Play a small beep sound for feedback
          try {
            const audioCtx = new (window.AudioContext || (window as any).webkitAudioContext)();
            const oscillator = audioCtx.createOscillator();
            oscillator.type = 'sine';
            oscillator.frequency.setValueAtTime(800, audioCtx.currentTime);
            oscillator.connect(audioCtx.destination);
            oscillator.start();
            oscillator.stop(audioCtx.currentTime + 0.1);
          } catch (e) {
            console.error("Audio Context Error:", e);
            alert("Error playing beep sound.");
          }
        }
        barcode = '';
      } else if (e.key !== 'Enter' && e.key !== 'Shift') {
        barcode += e.key;
      }

      lastKeyTime = currentTime;
    };

    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [items]);

  const handleAddToCart = (item: Item) => {
    dispatch(addToCart({
      id: item.id,
      name: item.name,
      quantity: getUnitIncrement(item.unit),
      sell_price: item.sellPrice,
      buy_price: item.buyPrice,
      unit: item.unit || 'pcs',
      maxStock: item.quantity,
      taxRate: item.taxRate,
      hsnCode: item.hsnCode
    }));
    setSearchQuery('');
  };

  const handleUpdateQuantity = (itemId: string, currentQty: number, delta: number) => {
    const item = cart.find(c => c.item.id === itemId)?.item;
    const inc = item ? getUnitIncrement(item.unit) : 1;
    const newQ = Math.max(inc, currentQty + (delta * inc));
    dispatch(updateQuantity({ id: itemId, quantity: newQ }));
  };

  const handleRemoveFromCart = (itemId: string) => {
    dispatch(removeFromCart(itemId));
  };

  const taxSummary = BillingEngine.calculateInvoiceTaxes(
    cart.map(c => ({
      id: c.item.id,
      sell_price: c.item.sellPrice,
      quantity: c.quantity,
      taxRate: c.item.taxRate || 0
    })),
    discount || 0,
    '', // storeGstin (can be added later if needed)
    ''  // customerGstin
  );

  const subtotal = taxSummary.subTotal;
  const totalBuyPrice = cart.reduce((sum, c) => sum + (c.item.buyPrice * c.quantity), 0);
  const maxProfitMargin = Math.max(0, subtotal - totalBuyPrice);

  const total = taxSummary.grandTotal;
  const totalTax = taxSummary.totalCgst + taxSummary.totalSgst + taxSummary.totalIgst;

  const filteredItems = items.filter(item =>
    item.name.toLowerCase().includes(searchQuery.toLowerCase()) ||
    (item.category || '').toLowerCase().includes(searchQuery.toLowerCase())
  );

  const handleCheckout = async () => {
    if (cart.length === 0) return;
    if (userRole === 'staff' && discount > maxProfitMargin) {
      alert(`Staff accounts cannot give discounts exceeding the profit margin (Max: ₹${maxProfitMargin.toFixed(2)}).`);
      return;
    }

    setIsSaving(true);

    try {
      const saleId = crypto.randomUUID();
      // eslint-disable-next-line react-hooks/purity
      const now = Date.now();
      const updatedAt = Math.floor(now / 1000);

      // 1. Create the Sale
      const saleType = paymentMode === 'Estimate' ? 'ESTIMATE' : 'SALE';
      await syncSale(dataConnect, {
        id: saleId,
        storeId,
        timestamp: now,
        totalAmount: total,
        discountAmount: discount || 0,
        customerName: customerName,
        type: saleType,
        notes: paymentMode !== 'Estimate' ? paymentMode : '',
        isDeleted: false,
        updatedAt
      });

      // 2. Add Sale Items & Update Inventory
      for (const c of cart) {
        const saleItemId = crypto.randomUUID();
        // Sync sale item
        await syncSaleItem(dataConnect, {
          id: saleItemId,
          storeId,
          saleId,
          itemId: c.item.id,
          itemName: c.item.name,
          unit: c.item.unit || 'pcs',
          quantity: c.quantity,
          sellPrice: c.item.sellPrice,
          buyPrice: c.item.buyPrice,
          isDeleted: false,
          updatedAt
        });

        // Deduct from inventory ONLY if it's a SALE
        if (saleType === 'SALE') {
          const newStock = Math.max(0, c.item.quantity - c.quantity);
          await syncItem(dataConnect, {
            id: c.item.id,
            storeId,
            name: c.item.name,
            quantity: newStock,
            unit: c.item.unit || 'pcs',
            buyPrice: c.item.buyPrice,
            sellPrice: c.item.sellPrice,
            lowStockThreshold: c.item.lowStockThreshold || 0,
            category: c.item.category || '',
            isDeleted: false,
            updatedAt
          });
        }
      }

      // Create Udhaar ledger entry for credit sales
      if (paymentMode === 'Udhaar' && saleType === 'SALE') {
        const udhaarId = crypto.randomUUID();
        await syncUdhaar(dataConnect, {
          id: udhaarId,
          storeId,
          customerName: customerName.trim(),
          amount: total,
          type: 'CREDIT',
          timestamp: now,
          notes: `Credit Sale #${saleId}`,
          isDeleted: false,
          updatedAt
        });
      }

      // Generate PDF Invoice
      try {
        InvoicePdfGenerator.generateInvoicePdf(
          {
            id: saleId,
            timestamp: now,
            totalAmount: total,
            discountAmount: discount || 0,
            customerName: customerName,
            customerGstin: customerGstin,
            customerAddress: customerAddress,
            type: saleType
          },
          cart.map(c => ({
            item: {
              id: c.item.id,
              name: c.item.name,
              unit: c.item.unit || 'pcs',
              sellPrice: c.item.sellPrice,
              buyPrice: c.item.buyPrice,
              taxRate: c.item.taxRate,
              hsnCode: c.item.hsnCode
            },
            quantity: c.quantity
          })),
          'StoreBook POS', // Can be fetched from Redux store settings
          '',
          ''
        );
      } catch (pdfError) {
        console.error("Error generating PDF:", pdfError);
      }

      dispatch(clearCart());
      // BUG-09 FIX: Invalidate dashboard ISR cache after client-side DataConnect mutation
      try {
        await import('@/app/actions').then(m => m.revalidateDashboard() as any);
      } catch (_) { /* best-effort, don't block success path */ }
      onSuccess();
    } catch (error) {
      console.error("Checkout failed:", error);
      alert("Checkout failed. Check console for details.");
      setIsSaving(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 backdrop-blur-sm p-4 md:p-6">
      <div className="bg-white dark:bg-gray-900 rounded-2xl shadow-2xl w-full max-w-6xl h-full max-h-[90vh] flex flex-col overflow-hidden border border-gray-200 dark:border-gray-800">

        {/* Header */}
        <div className="flex justify-between items-center px-6 py-4 border-b border-gray-100 dark:border-gray-800 bg-gray-50/50 dark:bg-gray-900/50">
          <div className="flex items-center space-x-3">
            <div className="p-2 bg-teal-100 dark:bg-teal-900/30 rounded-lg text-teal-600 dark:text-teal-400">
              <ShoppingCart size={24} />
            </div>
            <h2 className="text-xl font-bold text-gray-900 dark:text-white">Point of Sale</h2>
          </div>
          <button onClick={onClose} className="p-2 text-gray-500 hover:bg-gray-100 dark:hover:bg-gray-800 rounded-full transition-colors">
            <X size={20} />
          </button>
        </div>

        {/* Main Content Area */}
        <div className="flex-1 flex flex-col lg:flex-row overflow-hidden">

          {/* Left Column - Product Selection */}
          <div className="flex-1 flex flex-col border-r border-gray-100 dark:border-gray-800 bg-white dark:bg-gray-900">
            <div className="p-4 border-b border-gray-100 dark:border-gray-800">
              <div className="relative">
                <Search className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" size={18} />
                <input aria-label="text"
                  type="text"
                  value={searchQuery}
                  onChange={(e) => setSearchQuery(sanitizeInput(e.target.value))}
                  placeholder="Search products by name or category..."
                  className="w-full pl-10 pr-4 py-3 bg-gray-50 dark:bg-gray-800 border-none rounded-xl focus:ring-2 focus:ring-teal-500 dark:text-white transition-all"
                />
              </div>
            </div>

            <div className="flex-1 overflow-y-auto p-4 bg-gray-50/30 dark:bg-black/20">
              {loading ? (
                <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-4">
                  {[...Array(8)].map((_, i) => (
                    <div key={i} className="bg-white dark:bg-gray-800 border border-gray-100 dark:border-gray-700 rounded-xl p-4 h-32 flex flex-col animate-pulse">
                      <div className="flex justify-between items-start mb-2">
                        <div className="h-4 bg-gray-200 dark:bg-gray-700 rounded w-1/3"></div>
                        <div className="h-3 bg-gray-200 dark:bg-gray-700 rounded w-1/4"></div>
                      </div>
                      <div className="h-5 bg-gray-200 dark:bg-gray-700 rounded w-3/4 mb-2 flex-1 mt-2"></div>
                      <div className="h-6 bg-gray-200 dark:bg-gray-700 rounded w-1/2 mt-auto"></div>
                    </div>
                  ))}
                </div>
              ) : filteredItems.length === 0 ? (
                <div className="flex flex-col items-center justify-center h-full text-gray-500">
                  <p>No products found.</p>
                </div>
              ) : (
                <div className="grid grid-cols-2 sm:grid-cols-3 xl:grid-cols-4 gap-4">
                  {filteredItems.map(item => (
                    <div role="button" tabIndex={0}
                      key={item.id}
                      onClick={() => handleAddToCart(item)}
                      className="bg-white dark:bg-gray-800 border border-gray-100 dark:border-gray-700 rounded-xl p-4 cursor-pointer hover:border-teal-500 hover:shadow-md transition-all active:scale-95 flex flex-col"
                    >
                      <div className="flex justify-between items-start mb-2">
                        <span className="inline-flex px-2 py-0.5 rounded text-[10px] font-bold uppercase tracking-wider bg-gray-100 dark:bg-gray-700 text-gray-600 dark:text-gray-300">
                          {item.category || 'Item'}
                        </span>
                        <span className="text-xs text-gray-500 dark:text-gray-400 font-medium">
                          {item.quantity} {item.unit}
                        </span>
                      </div>
                      <h3 className="font-semibold text-gray-900 dark:text-white text-sm line-clamp-2 mb-2 flex-1">
                        {item.name}
                      </h3>
                      <div className="text-lg font-bold text-teal-600 dark:text-teal-400 mt-auto">
                        <FormattedAmount amount={item.sellPrice} />
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </div>
          </div>

          {/* Right Column - Cart & Checkout */}
          <div className="w-full lg:w-[400px] xl:w-[450px] flex flex-col bg-white dark:bg-gray-900 shadow-[-4px_0_15px_-3px_rgba(0,0,0,0.05)] z-10">
            {/* Cart Items */}
            <div className="flex-1 overflow-y-auto p-4 space-y-3 bg-gray-50/50 dark:bg-gray-900/50">
              {cart.length === 0 ? (
                <div className="flex flex-col items-center justify-center h-full text-gray-400 space-y-4">
                  <ShoppingCart size={48} className="opacity-20" />
                  <p className="font-medium">Cart is empty</p>
                  <p className="text-sm">Click items on the left to add them.</p>
                </div>
              ) : (
                cart.map(c => (
                  <div key={c.item.id} className="flex flex-col bg-white dark:bg-gray-800 p-3 rounded-xl border border-gray-100 dark:border-gray-700 shadow-sm">
                    <div className="flex justify-between items-start mb-2">
                      <h4 className="font-medium text-gray-900 dark:text-white text-sm pr-4 line-clamp-1">{c.item.name}</h4>
                      <button onClick={() => handleRemoveFromCart(c.item.id)} className="text-red-400 hover:text-red-600 transition-colors">
                        <Trash2 size={16} />
                      </button>
                    </div>
                    <div className="flex justify-between items-center mt-2">
                      <div className="font-bold text-gray-900 dark:text-white">
                        <FormattedAmount amount={c.item.sellPrice * c.quantity} />
                      </div>
                      <div className="flex items-center space-x-3 bg-gray-50 dark:bg-gray-900 rounded-lg p-1 border border-gray-200 dark:border-gray-700">
                        <button
                          onClick={() => handleUpdateQuantity(c.item.id, c.quantity, -1)}
                          className="w-7 h-7 flex items-center justify-center bg-white dark:bg-gray-800 rounded shadow-sm text-gray-600 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-700 shrink-0"
                        >
                          <Minus size={14} />
                        </button>
                        <input aria-label="text"
                          type="text"
                          value={c.quantity}
                          readOnly
                          onBlur={(e) => {
                            const val = e.target.value.toLowerCase().trim();
                            let parsed = parseFloat(val) || 0;
                            if (parsed > 0) {
                              const isBaseGram = c.item.unit.toLowerCase().startsWith('g');
                              if (isBaseGram && (val.includes('kg') || val.includes('kilo'))) parsed *= 1000;
                              dispatch(updateQuantity({ id: c.item.id, quantity: parsed }));
                            } else {
                              e.target.value = String(c.quantity); // reset
                            }
                          }}
                          onKeyDown={(e) => { if (e.key === 'Enter') e.currentTarget.blur(); }}
                          className="w-12 text-sm font-bold text-center bg-transparent border-b border-dashed border-gray-400 dark:border-gray-600 focus:outline-none focus:border-teal-500 dark:text-white"
                        />
                        <button
                          onClick={() => handleUpdateQuantity(c.item.id, c.quantity, 1)}
                          className="w-7 h-7 flex items-center justify-center bg-white dark:bg-gray-800 rounded shadow-sm text-gray-600 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-700 shrink-0"
                        >
                          <Plus size={14} />
                        </button>
                      </div>
                    </div>
                  </div>
                ))
              )}
            </div>

            {/* Checkout Form & Totals */}
            <div className="border-t border-gray-200 dark:border-gray-800 p-4 sm:p-6 bg-white dark:bg-gray-900 overflow-y-auto">
              <div className="space-y-4 mb-6">
                <div>
                  <input aria-label="Discount ₹"
                    type="number"
                    placeholder="Discount ₹"
                    value={discount || ''}
                    onChange={e => setDiscount(parseFloat(e.target.value) || 0)}
                    className="w-full px-4 py-3 border border-gray-200 dark:border-gray-700 rounded-xl bg-gray-50 dark:bg-gray-800 dark:text-white text-sm focus:ring-2 focus:ring-teal-500"
                  />
                  {userRole === 'staff' && discount > maxProfitMargin && (
                    <p className="text-xs text-red-500 mt-1 px-1">Exceeds max allowed (₹{maxProfitMargin.toFixed(2)})</p>
                  )}
                </div>

                <div>
                  <label className="block text-xs font-semibold text-gray-500 dark:text-gray-400 mb-2 uppercase tracking-wider">Payment Mode</label>
                  <div className="grid grid-cols-4 gap-2">
                    {['Cash', 'UPI', 'Udhaar', 'Estimate'].map((mode) => (
                      <button
                        key={mode}
                        onClick={() => setPaymentMode(mode as any)}
                        className={`py-2 px-1 rounded-xl text-sm font-medium transition-colors flex items-center justify-center ${paymentMode === mode
                          ? mode === 'Udhaar'
                            ? 'bg-red-500 text-white shadow-md'
                            : 'bg-gray-900 dark:bg-gray-100 text-white dark:text-gray-900 shadow-md'
                          : 'bg-gray-100 dark:bg-gray-800 text-gray-700 dark:text-gray-300 hover:bg-gray-200 dark:hover:bg-gray-700'
                          }`}
                      >
                        {mode === 'Cash' && <span className="mr-1">💵</span>}
                        {mode === 'UPI' && <span className="mr-1">📱</span>}
                        {mode === 'Udhaar' && <span className="mr-1">📖</span>}
                        {mode}
                      </button>
                    ))}
                  </div>
                </div>

                {paymentMode === 'Udhaar' && (
                  <div className="bg-red-50 dark:bg-red-900/20 text-red-600 dark:text-red-400 p-3 rounded-xl flex items-start space-x-2 text-sm border border-red-100 dark:border-red-900/50">
                    <span className="mt-0.5">⚠️</span>
                    <p>Sale will be recorded in Udhaar ledger as credit due.</p>
                  </div>
                )}

                <div>
                  <div className="relative">
                    <span className="absolute left-4 top-1/2 -translate-y-1/2 text-gray-400">
                      👤
                    </span>
                    <input aria-label="Customer Name"
                      type="text"
                      placeholder={paymentMode === 'Udhaar' ? 'Customer Name *' : 'Customer Name (Optional / Credit)'}
                      value={customerName}
                      onChange={e => setCustomerName(sanitizeInput(e.target.value))}
                      className="w-full pl-10 pr-4 py-3 border border-gray-200 dark:border-gray-700 rounded-xl bg-white dark:bg-gray-800 dark:text-white text-sm focus:ring-2 focus:ring-teal-500 shadow-sm"
                    />
                  </div>
                </div>

                <div className="pt-2">
                  <button
                    onClick={() => setShowAdditionalDetails(!showAdditionalDetails)}
                    className="w-full text-center text-sm font-semibold text-gray-900 dark:text-white py-2"
                  >
                    {showAdditionalDetails ? 'Hide Additional Billing Details' : 'Show Additional Billing Details'}
                  </button>

                  {showAdditionalDetails && (
                    <div className="space-y-3 mt-3 animate-in slide-in-from-top-2">
                      <input aria-label="Customer GSTIN"
                        type="text"
                        placeholder="Customer GSTIN (Optional)"
                        value={customerGstin}
                        onChange={e => setCustomerGstin(sanitizeInput(e.target.value))}
                        className="w-full px-4 py-3 border border-gray-200 dark:border-gray-700 rounded-xl bg-white dark:bg-gray-800 dark:text-white text-sm focus:ring-2 focus:ring-teal-500 shadow-sm"
                      />
                      <input aria-label="Customer Address"
                        type="text"
                        placeholder="Customer Address (Optional)"
                        value={customerAddress}
                        onChange={e => setCustomerAddress(sanitizeInput(e.target.value))}
                        className="w-full px-4 py-3 border border-gray-200 dark:border-gray-700 rounded-xl bg-white dark:bg-gray-800 dark:text-white text-sm focus:ring-2 focus:ring-teal-500 shadow-sm"
                      />
                    </div>
                  )}
                </div>
              </div>

              <div className="space-y-3 mb-6 bg-gray-50 dark:bg-gray-800/50 p-4 rounded-xl border border-gray-100 dark:border-gray-800">
                <div className="flex justify-between text-gray-600 dark:text-gray-400 text-sm">
                  <span>Subtotal ({cart.length} items)</span>
                  <span><FormattedAmount amount={subtotal} /></span>
                </div>
                {discount > 0 && (
                  <div className="flex justify-between text-emerald-600 dark:text-emerald-400 text-sm font-medium">
                    <span>Discount</span>
                    <span>-<FormattedAmount amount={discount} /></span>
                  </div>
                )}
                {totalTax > 0 && (
                  <div className="flex justify-between text-gray-600 dark:text-gray-400 text-sm">
                    <span>Tax (GST)</span>
                    <span><FormattedAmount amount={totalTax} /></span>
                  </div>
                )}
                <div className="pt-3 border-t border-gray-200 dark:border-gray-700 flex justify-between items-end">
                  <span className="font-bold text-gray-900 dark:text-white">Total Amount</span>
                  <span className="text-2xl font-black text-teal-600 dark:text-teal-400">
                    <FormattedAmount amount={total} />
                  </span>
                </div>
              </div>

              <div className="space-y-3">
                <button
                  onClick={handleCheckout}
                  disabled={cart.length === 0 || isSaving || (paymentMode === 'Udhaar' && !customerName.trim())}
                  className={`w-full py-4 text-white rounded-xl font-bold text-lg shadow-lg transition-all flex items-center justify-center space-x-2 ${paymentMode === 'Udhaar'
                    ? 'bg-red-500 hover:bg-red-600 shadow-red-500/30'
                    : 'bg-gray-900 hover:bg-black dark:bg-gray-100 dark:text-gray-900 dark:hover:bg-white shadow-gray-900/30 dark:shadow-gray-100/30'
                    } disabled:opacity-50 disabled:cursor-not-allowed`}
                >
                  {isSaving ? (
                    <>
                      <Loader2 className="animate-spin" size={20} />
                      <span>Processing...</span>
                    </>
                  ) : (
                    <>
                      {paymentMode === 'Cash' && <span>Collect Cash / Checkout</span>}
                      {paymentMode === 'UPI' && <span>Confirm UPI Sale</span>}
                      {paymentMode === 'Udhaar' && <span>Record as Udhaar</span>}
                      {paymentMode === 'Estimate' && <span>Save as Estimate</span>}
                    </>
                  )}
                </button>

                {paymentMode !== 'Estimate' && (
                  <button
                    onClick={() => { setPaymentMode('Estimate'); setTimeout(handleCheckout, 0); }}
                    disabled={cart.length === 0 || isSaving}
                    className="w-full py-4 bg-white dark:bg-gray-800 text-gray-900 dark:text-white border border-gray-200 dark:border-gray-700 rounded-xl font-bold text-lg hover:bg-gray-50 dark:hover:bg-gray-700 transition-all flex items-center justify-center space-x-2 disabled:opacity-50 disabled:cursor-not-allowed"
                  >
                    <span>📄 Save as Estimate</span>
                  </button>
                )}
              </div>
            </div>
          </div>

        </div>
      </div>
    </div>
  );
}
