'use client';

import { useState, useEffect } from 'react';
import { Package, TrendingUp, Users, Receipt, ArrowUpRight, ArrowDownRight, Store } from 'lucide-react';
import DashboardCharts from './DashboardCharts';
import { db } from '@/lib/firebase';
import { collection, onSnapshot } from 'firebase/firestore';

interface Stats {
  totalItems: number;
  totalSales: number;
  totalUdhaar: number;
  totalExpenses: number;
  totalStores: number;
  salesData: any[];
  itemsData: any[];
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

  useEffect(() => {
    if (!isPremium || !storeId) return;

    let itemsList: any[] = [];
    let salesList: any[] = [];
    let udhaarList: any[] = [];
    let expensesList: any[] = [];

    const updateStats = () => {
      const totalItems = itemsList.length;
      const totalSales = salesList.reduce((acc, doc) => acc + (doc.total_amount || 0), 0);
      const totalUdhaar = udhaarList.reduce((acc, doc) => acc + (doc.amount || 0), 0);
      const totalExpenses = expensesList.reduce((acc, doc) => acc + (doc.amount || 0), 0);

      setStats(prev => ({
        ...prev,
        totalItems,
        totalSales,
        totalUdhaar,
        totalExpenses,
        salesData: [...salesList],
        itemsData: [...itemsList]
      }));
    };

    const unsubItems = onSnapshot(collection(db, 'stores', storeId, 'items'), (snap) => {
      itemsList = snap.docs
        .map(d => {
          const data = d.data();
          if (userRole === 'staff') delete data.buy_price;
          return { id: d.id, ...data };
        })
        .filter((item: any) => item.is_deleted !== 1);
      updateStats();
    }, (error) => {
      console.error("Dashboard items sync error:", error);
    });

    const unsubSales = onSnapshot(collection(db, 'stores', storeId, 'sales'), (snap) => {
      salesList = snap.docs
        .map(d => {
          const data = d.data();
          if (userRole === 'staff' && Array.isArray(data.items)) {
            data.items.forEach((i: any) => { delete i.buy_price; });
          }
          return { id: d.id, ...data };
        })
        .filter((sale: any) => sale.is_deleted !== 1);
      updateStats();
    }, (error) => {
      console.error("Dashboard sales sync error:", error);
    });

    const unsubUdhaar = onSnapshot(collection(db, 'stores', storeId, 'udhaar'), (snap) => {
      udhaarList = snap.docs
        .map(d => ({ id: d.id, ...d.data() }))
        .filter((u: any) => u.is_deleted !== 1);
      updateStats();
    }, (error) => {
      console.error("Dashboard udhaar sync error:", error);
    });

    const unsubExpenses = onSnapshot(collection(db, 'stores', storeId, 'expenses'), (snap) => {
      expensesList = snap.docs
        .map(d => ({ id: d.id, ...d.data() }))
        .filter((e: any) => e.is_deleted !== 1);
      updateStats();
    }, (error) => {
      console.error("Dashboard expenses sync error:", error);
    });

    return () => {
      unsubItems();
      unsubSales();
      unsubUdhaar();
      unsubExpenses();
    };
  }, [isPremium, storeId, userRole]);

  const formatCurrency = (amount: number) => {
    return new Intl.NumberFormat('en-IN', {
      style: 'currency',
      currency: 'INR',
      maximumFractionDigits: 0
    }).format(amount || 0);
  };

  const statCards = [
    {
      title: 'Total Sales',
      value: formatCurrency(stats.totalSales),
      icon: <TrendingUp className="text-white" size={24} />,
      color: 'bg-teal-500',
      trend: '+12.5%',
      trendUp: true
    },
    {
      title: 'Total Udhaar',
      value: formatCurrency(stats.totalUdhaar),
      icon: <Users className="text-white" size={24} />,
      color: 'bg-blue-500',
      trend: '+5.2%',
      trendUp: false
    },
    {
      title: 'Total Expenses',
      value: formatCurrency(stats.totalExpenses),
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
      value: stats.totalStores.toString(),
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
          <p className="text-gray-500 dark:text-gray-400 text-sm mt-1">Here's what's happening with your store today. (SSR via Firebase)</p>
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
            <DashboardCharts salesData={stats.salesData} itemsData={stats.itemsData} />
        </div>
        <div className="glass-card p-6 h-80 flex items-center justify-center border-dashed dark:border-gray-800">
            <p className="text-gray-400 dark:text-gray-500 font-medium">Recent Activity Placeholder</p>
        </div>
      </div>
    </div>
  );
}
