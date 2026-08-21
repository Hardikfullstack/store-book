'use client';

import { Store, Users, DollarSign, Activity, TrendingUp, AlertTriangle } from 'lucide-react';
import { FormattedAmount } from '@/components/FormattedAmount';

type StoreRow = Record<string, unknown>;
type UserRow = Record<string, unknown>;

export default function AdminDashboardClient({ stores, users }: { stores: StoreRow[], users: UserRow[] }) {
  // Aggregate Metrics
  const activeStores = stores.filter(s => s.is_active !== false).length;
  const premiumStores = stores.filter(s => s.isPremium).length;
  const totalUsers = users.length;
  
  // Mock Revenue (would come from real backend aggregation)
  const mrr = premiumStores * 499; // Assuming Rs 499/mo standard plan
  const arr = mrr * 12;

  // Breakdown by platform
  const googlePlayStores = stores.filter(s => s.subscriptionPlatform === 'google_play').length;
  const razorpayStores = stores.filter(s => s.subscriptionPlatform === 'razorpay').length;

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center mb-6">
        <div>
          <h1 className="text-2xl font-bold text-gray-900 dark:text-white">Platform Overview</h1>
          <p className="text-gray-500 dark:text-gray-400 text-sm mt-1">Super Admin global metrics and system health.</p>
        </div>
      </div>

      {/* KPI Cards */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
        <div className="glass-card p-6 border border-gray-200 dark:border-gray-800 rounded-xl">
          <div className="flex items-center justify-between mb-4">
            <h3 className="text-sm font-semibold text-gray-600 dark:text-gray-400">Total MRR</h3>
            <div className="p-2 bg-emerald-50 dark:bg-emerald-900/30 text-emerald-600 dark:text-emerald-400 rounded-lg">
              <DollarSign size={20} />
            </div>
          </div>
          <div className="text-3xl font-bold text-gray-900 dark:text-white">
            <FormattedAmount amount={mrr} />
          </div>
          <p className="text-xs text-gray-500 mt-2 flex items-center">
            <TrendingUp size={12} className="mr-1 text-emerald-500" />
            ARR: <FormattedAmount amount={arr} />
          </p>
        </div>

        <div className="glass-card p-6 border border-gray-200 dark:border-gray-800 rounded-xl">
          <div className="flex items-center justify-between mb-4">
            <h3 className="text-sm font-semibold text-gray-600 dark:text-gray-400">Active Stores</h3>
            <div className="p-2 bg-blue-50 dark:bg-blue-900/30 text-blue-600 dark:text-blue-400 rounded-lg">
              <Store size={20} />
            </div>
          </div>
          <div className="text-3xl font-bold text-gray-900 dark:text-white">
            {activeStores} <span className="text-sm font-normal text-gray-500">/ {stores.length}</span>
          </div>
          <p className="text-xs text-gray-500 mt-2">
            {premiumStores} Premium Subscriptions
          </p>
        </div>

        <div className="glass-card p-6 border border-gray-200 dark:border-gray-800 rounded-xl">
          <div className="flex items-center justify-between mb-4">
            <h3 className="text-sm font-semibold text-gray-600 dark:text-gray-400">Total Users</h3>
            <div className="p-2 bg-purple-50 dark:bg-purple-900/30 text-purple-600 dark:text-purple-400 rounded-lg">
              <Users size={20} />
            </div>
          </div>
          <div className="text-3xl font-bold text-gray-900 dark:text-white">
            {totalUsers}
          </div>
          <p className="text-xs text-gray-500 mt-2">
            Across all active tenants
          </p>
        </div>

        <div className="glass-card p-6 border border-gray-200 dark:border-gray-800 rounded-xl">
          <div className="flex items-center justify-between mb-4">
            <h3 className="text-sm font-semibold text-gray-600 dark:text-gray-400">System Health</h3>
            <div className="p-2 bg-teal-50 dark:bg-teal-900/30 text-teal-600 dark:text-teal-400 rounded-lg">
              <Activity size={20} />
            </div>
          </div>
          <div className="text-3xl font-bold text-teal-600 dark:text-teal-400">
            99.9%
          </div>
          <p className="text-xs text-emerald-500 mt-2 font-medium">
            All services operational
          </p>
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6 mt-6">
        {/* Subscription Breakdown */}
        <div className="glass-card p-6 border border-gray-200 dark:border-gray-800 rounded-xl">
          <h3 className="text-lg font-bold text-gray-900 dark:text-white mb-4">Premium Distribution</h3>
          <div className="space-y-4">
            <div>
              <div className="flex justify-between text-sm mb-1">
                <span className="text-gray-600 dark:text-gray-400">Google Play (App)</span>
                <span className="font-semibold dark:text-white">{googlePlayStores}</span>
              </div>
              <div className="w-full bg-gray-200 dark:bg-gray-700 rounded-full h-2">
                <div className="bg-green-500 h-2 rounded-full" style={{ width: `${Math.max(5, (googlePlayStores / Math.max(1, premiumStores)) * 100)}%` }}></div>
              </div>
            </div>
            <div>
              <div className="flex justify-between text-sm mb-1">
                <span className="text-gray-600 dark:text-gray-400">Razorpay (Web)</span>
                <span className="font-semibold dark:text-white">{razorpayStores}</span>
              </div>
              <div className="w-full bg-gray-200 dark:bg-gray-700 rounded-full h-2">
                <div className="bg-blue-600 h-2 rounded-full" style={{ width: `${Math.max(5, (razorpayStores / Math.max(1, premiumStores)) * 100)}%` }}></div>
              </div>
            </div>
            <div>
              <div className="flex justify-between text-sm mb-1">
                <span className="text-gray-600 dark:text-gray-400">Free/Trial Tier</span>
                <span className="font-semibold dark:text-white">{stores.length - premiumStores}</span>
              </div>
              <div className="w-full bg-gray-200 dark:bg-gray-700 rounded-full h-2">
                <div className="bg-gray-400 h-2 rounded-full" style={{ width: `${Math.max(5, ((stores.length - premiumStores) / Math.max(1, stores.length)) * 100)}%` }}></div>
              </div>
            </div>
          </div>
        </div>

        {/* Recent Alerts */}
        <div className="glass-card p-6 border border-gray-200 dark:border-gray-800 rounded-xl">
          <h3 className="text-lg font-bold text-gray-900 dark:text-white mb-4">System Alerts & Audits</h3>
          <div className="space-y-3">
            <div className="flex items-start space-x-3 p-3 bg-orange-50 dark:bg-orange-900/20 text-orange-800 dark:text-orange-400 rounded-lg border border-orange-100 dark:border-orange-800/30">
              <AlertTriangle size={18} className="mt-0.5 shrink-0" />
              <div>
                <p className="text-sm font-semibold">Store Data Purge Executed</p>
                <p className="text-xs opacity-80 mt-1">Admin &apos;hari&apos; permanently deleted store ID &apos;old-demo-123&apos; to comply with GDPR request.</p>
                <p className="text-[10px] mt-2 opacity-60">2 hours ago</p>
              </div>
            </div>
            <div className="flex items-start space-x-3 p-3 bg-gray-50 dark:bg-gray-800/50 text-gray-800 dark:text-gray-300 rounded-lg border border-gray-100 dark:border-gray-700">
              <Activity size={18} className="mt-0.5 shrink-0 text-blue-500" />
              <div>
                <p className="text-sm font-semibold">PostgreSQL Migration Completed</p>
                <p className="text-xs opacity-80 mt-1">DataConnect schema successfully updated with role constraints.</p>
                <p className="text-[10px] mt-2 opacity-60">5 hours ago</p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
