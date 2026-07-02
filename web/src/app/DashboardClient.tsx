'use client';

import { useState, useEffect } from 'react';
import { Package, TrendingUp, Users, Receipt, ArrowUpRight, ArrowDownRight, Store } from 'lucide-react';
import DashboardCharts from './DashboardCharts';
import { dataConnect, rtdb } from '@/lib/firebase';
import { ref, onValue } from 'firebase/database';
import { get, set } from 'idb-keyval';
import { sanitizeInput } from '@/lib/sanitize';
import {
  getActiveItems, getActiveSales, getActiveUdhaars, getActiveExpenses, getActiveSaleItems,
  syncItems, syncSales, syncUdhaars, syncExpenses, syncSaleItems
} from '@/dataconnect';
import { FormattedAmount } from '@/components/FormattedAmount';

interface Stats {
  totalItems: number;
  totalSales: number;
  totalUdhaar: number;
  totalExpenses: number;
  totalStores: number;
  salesData: any[];
  itemsData: any[];
  saleItemsData: any[];
}

export default function DashboardClient({
  initialStats,
  userRole,
  storeId,
  isPremium
}: {
  initialStats: Stats;
  userRole: string;
  storeId?: string;
  isPremium?: boolean;
}) {
  const [stats, setStats] = useState<Stats>(initialStats);
  const [dateRange, setDateRange] = useState<'today' | 'week' | 'month' | 'all'>('all');
  const [rawSales, setRawSales] = useState<any[]>([]);
  const [rawUdhaars, setRawUdhaars] = useState<any[]>([]);
  const [rawExpenses, setRawExpenses] = useState<any[]>([]);
  const [rawSaleItems, setRawSaleItems] = useState<any[]>([]);
  const [itemsList, setItemsList] = useState<any[]>([]);

  useEffect(() => {
    if (!isPremium || !storeId) return;

    const updateStats = () => {
      const now = Date.now();
      let cutoff = 0;
      if (dateRange === 'today') cutoff = now - 24 * 60 * 60 * 1000;
      if (dateRange === 'week') cutoff = now - 7 * 24 * 60 * 60 * 1000;
      if (dateRange === 'month') cutoff = now - 30 * 24 * 60 * 60 * 1000;

      const filteredSales = rawSales.filter(s => ((s.timestamp || s.updated_at) * 1000) >= cutoff && !s.isDeleted);
      const filteredUdhaar = rawUdhaars.filter(u => ((u.timestamp || u.updated_at) * 1000) >= cutoff && !u.isDeleted);
      const filteredExpenses = rawExpenses.filter(e => ((e.timestamp || e.updated_at) * 1000) >= cutoff && !e.isDeleted);
      const activeItems = itemsList.filter(i => !i.isDeleted);

      const totalItems = activeItems.length;
      const totalSales = filteredSales.reduce((acc, doc) => acc + (doc.total_amount || 0), 0);
      const totalUdhaar = filteredUdhaar.reduce((acc, doc) => acc + (doc.amount || 0), 0);
      const totalExpenses = filteredExpenses.reduce((acc, doc) => acc + (doc.amount || 0), 0);

      setStats(prev => ({
        ...prev,
        totalItems,
        totalSales,
        totalUdhaar,
        totalExpenses,
        salesData: [...filteredSales],
        itemsData: [...activeItems],
        saleItemsData: [...rawSaleItems.filter(si => !si.isDeleted)]
      }));
    };

    updateStats();
  }, [dateRange, rawSales, rawUdhaars, rawExpenses, rawSaleItems, itemsList]);

  // Delta Sync & Cache Logic
  useEffect(() => {
    if (!isPremium || !storeId) return;

    let isMounted = true;
    const cacheKey = `dashboard_data_${storeId}`;
    let isFetching = false;

    const loadFromCache = async () => {
      try {
        const cached = await get(cacheKey);
        if (cached && isMounted) {
          setItemsList(cached.items || []);
          setRawSales(cached.sales || []);
          setRawUdhaars(cached.udhaars || []);
          setRawExpenses(cached.expenses || []);
          setRawSaleItems(cached.saleItems || []);
          return cached.lastSync || 0;
        }
      } catch (e) {
        console.error("Cache read error", e);
      }
      return 0;
    };

    const mergeDelta = (oldArr: any[], newArr: any[]) => {
      const newMap = new Map(newArr.map(i => [i.id, i]));
      const merged = oldArr.map(i => newMap.has(i.id) ? newMap.get(i.id) : i);
      newArr.forEach(i => {
        if (!oldArr.find(o => o.id === i.id)) merged.push(i);
      });
      // Purge soft-deleted records from persistent cache to prevent bloat
      return merged.filter(i => !i.isDeleted);
    };

    let syncRequested = false;

    const performSync = async (lastSync: number) => {
      if (isFetching) {
        syncRequested = true;
        return;
      }
      isFetching = true;
      syncRequested = false;

      try {
        if (lastSync === 0) {
          // Full initial fetch
          const [itemsRes, salesRes, udhaarsRes, expensesRes, saleItemsRes] = await Promise.all([
            getActiveItems(dataConnect, { storeId }),
            getActiveSales(dataConnect, { storeId }),
            getActiveUdhaars(dataConnect, { storeId }),
            getActiveExpenses(dataConnect, { storeId }),
            getActiveSaleItems(dataConnect, { storeId })
          ]);

          if (!isMounted) return;

          const parsedItems = itemsRes.data.items.map((item: any) => {
            if (userRole === 'staff') delete item.buyPrice;
            return { id: item.id, ...item };
          });
          const parsedSales = salesRes.data.sales.filter((sale: any) => sale.type === 'SALE')
            .map((sale: any) => ({ id: sale.id, ...sale, total_amount: sale.totalAmount, updated_at: sale.updatedAt }));
          const parsedUdhaars = udhaarsRes.data.udhaarEntries.map((u: any) => ({ id: u.id, ...u, updated_at: u.updatedAt }));
          const parsedExpenses = expensesRes.data.expenseEntries.map((e: any) => ({ id: e.id, ...e, updated_at: e.updatedAt }));
          const parsedSaleItems = saleItemsRes.data.saleItemDetails;

          const newData = {
            items: parsedItems, sales: parsedSales, udhaars: parsedUdhaars, expenses: parsedExpenses, saleItems: parsedSaleItems,
            lastSync: Date.now() // Use milliseconds matching Android System.currentTimeMillis()
          };

          await set(cacheKey, newData);
          setItemsList(parsedItems);
          setRawSales(parsedSales);
          setRawUdhaars(parsedUdhaars);
          setRawExpenses(parsedExpenses);
          setRawSaleItems(parsedSaleItems);

        } else {
          // Delta fetch
          const [itemsRes, salesRes, udhaarsRes, expensesRes, saleItemsRes] = await Promise.all([
            syncItems(dataConnect, { storeId, lastSync }),
            syncSales(dataConnect, { storeId, lastSync }),
            syncUdhaars(dataConnect, { storeId, lastSync }),
            syncExpenses(dataConnect, { storeId, lastSync }),
            syncSaleItems(dataConnect, { storeId, lastSync })
          ]);

          if (!isMounted) return;

          const cached = await get(cacheKey) || { items: [], sales: [], udhaars: [], expenses: [], saleItems: [] };

          const pItems = itemsRes.data.items.map((item: any) => {
            if (userRole === 'staff') delete item.buyPrice;
            return { id: item.id, ...item };
          });
          const pSales = salesRes.data.sales.filter((sale: any) => sale.type === 'SALE')
            .map((sale: any) => ({ id: sale.id, ...sale, total_amount: sale.totalAmount, updated_at: sale.updatedAt }));
          const pUdhaars = udhaarsRes.data.udhaarEntries.map((u: any) => ({ id: u.id, ...u, updated_at: u.updatedAt }));
          const pExpenses = expensesRes.data.expenseEntries.map((e: any) => ({ id: e.id, ...e, updated_at: e.updatedAt }));
          const pSaleItems = saleItemsRes.data.saleItemDetails;

          const newItems = mergeDelta(cached.items, pItems);
          const newSales = mergeDelta(cached.sales, pSales);
          const newUdhaars = mergeDelta(cached.udhaars, pUdhaars);
          const newExpenses = mergeDelta(cached.expenses, pExpenses);
          const newSaleItems = mergeDelta(cached.saleItems, pSaleItems);

          const newData = {
            items: newItems, sales: newSales, udhaars: newUdhaars, expenses: newExpenses, saleItems: newSaleItems,
            lastSync: Date.now() // Use milliseconds
          };

          await set(cacheKey, newData);
          setItemsList(newItems);
          setRawSales(newSales);
          setRawUdhaars(newUdhaars);
          setRawExpenses(newExpenses);
          setRawSaleItems(newSaleItems);
        }
      } catch (error) {
        console.error("Dashboard DataConnect sync error:", error);
      } finally {
        isFetching = false;
        if (syncRequested) {
          performSync(localLastSync).then(() => { localLastSync = Date.now(); });
        }
      }
    };

    let localLastSync = 0;

    // Init Cache & RTDB Listener
    loadFromCache().then((syncTime) => {
      localLastSync = syncTime;
      // If cache is empty, fetch immediately
      if (syncTime === 0) performSync(0);

      // Setup RTDB Ping Listener
      const updateRef = ref(rtdb, `store_updates/${storeId}/last_update`);
      onValue(updateRef, (snapshot) => {
        const serverUpdate = snapshot.val() || 0;
        // serverUpdate is in ms, localLastSync is in ms
        if (serverUpdate > localLastSync) {
          performSync(localLastSync).then(() => {
            localLastSync = Date.now();
          });
        }
      });
    });

    return () => {
      isMounted = false;
    };
  }, [isPremium, storeId, userRole]);

  const statCards = [
    {
      title: 'Total Sales',
      value: <FormattedAmount amount={stats.totalSales} />,
      icon: <TrendingUp className="text-white" size={24} />,
      color: 'bg-teal-500',
      trend: '+12.5%',
      trendUp: true
    },
    {
      title: 'Total Udhaar',
      value: <FormattedAmount amount={stats.totalUdhaar} />,
      icon: <Users className="text-white" size={24} />,
      color: 'bg-blue-500',
      trend: '+5.2%',
      trendUp: false
    },
    {
      title: 'Total Expenses',
      value: <FormattedAmount amount={stats.totalExpenses} />,
      icon: <Receipt className="text-white" size={24} />,
      color: 'bg-orange-500',
      trend: '-2.4%',
      trendUp: true
    },
    {
      title: 'Inventory Items',
      value: stats.totalItems,
      icon: <Package className="text-white" size={24} />,
      color: 'bg-indigo-500',
      trend: '+18 new',
      trendUp: true
    }
  ];

  if (userRole === 'admin') {
    statCards.unshift({
      title: 'Total Stores',
      value: stats.totalStores as any,
      icon: <Store className="text-white" size={24} />,
      color: 'bg-purple-500',
      trend: '+2 new',
      trendUp: true
    });
  }

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center mb-8">
        <div>
          <h1 className="text-2xl font-bold text-gray-900 dark:text-white">Dashboard Overview</h1>
          <p className="text-gray-500 dark:text-gray-400 text-sm mt-1">Real-time sync via Data Connect Delta.</p>
        </div>
        <div>
          <select
            value={dateRange}
            onChange={e => setDateRange(sanitizeInput(e.target.value) as any)}
            className="px-4 py-2 bg-white dark:bg-gray-800 border border-gray-200 dark:border-gray-700 rounded-xl text-sm font-medium focus:ring-2 focus:ring-teal-500"
          >
            <option value="all">All Time</option>
            <option value="today">Today</option>
            <option value="week">Last 7 Days</option>
            <option value="month">Last 30 Days</option>
          </select>
        </div>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
        {statCards.map((stat, i) => (
          <div key={i} className="glass-card p-6 flex flex-col justify-between hover:shadow-md transition-shadow duration-300">
            <div className="flex justify-between items-start mb-4">
              <div className={`p-3 rounded-xl shadow-sm ${stat.color}`}>
                {stat.icon}
              </div>
              <div className={`flex items-center space-x-1 text-sm font-medium px-2 py-1 rounded-full ${stat.trendUp ? 'bg-emerald-50 dark:bg-emerald-900/30 text-emerald-600 dark:text-emerald-400' : 'bg-red-50 dark:bg-red-900/30 text-red-600 dark:text-red-400'}`}>
                {stat.trendUp ? <ArrowUpRight size={16} /> : <ArrowDownRight size={16} />}
                <span>{stat.trend}</span>
              </div>
            </div>
            <div>
              <p className="text-sm font-medium text-gray-500 dark:text-gray-400 mb-1">{stat.title}</p>
              <h3 className="text-3xl font-bold text-gray-900 dark:text-white tracking-tight">{stat.value}</h3>
            </div>
          </div>
        ))}
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6 mt-8">
        <div className="lg:col-span-2 glass-card p-6 h-80 flex items-center justify-center border-dashed dark:border-gray-800">
          <DashboardCharts salesData={stats.salesData} itemsData={stats.itemsData} saleItemsData={stats.saleItemsData} />
        </div>
        <div className="glass-card p-6 h-80 flex items-center justify-center border-dashed dark:border-gray-800">
          <p className="text-gray-400 dark:text-gray-500 font-medium">Recent Activity Placeholder</p>
        </div>
      </div>
    </div>
  );
}
