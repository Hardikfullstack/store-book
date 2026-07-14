'use client';

import { useState, useEffect } from 'react';
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from 'recharts';
import { sanitizeInput } from '@/lib/sanitize';
import { getSalesTrendData } from '@/app/actions';

interface TrendPoint {
  date: string;
  totalValue: number;
  transactionCount: number;
}

type PeriodKey = '7d' | '30d' | '90d';

const PERIOD_MAP: Record<PeriodKey, { label: string; days: number }> = {
  '7d': { label: 'Last 7 Days', days: 7 },
  '30d': { label: 'Last 30 Days', days: 30 },
  '90d': { label: 'Last 90 Days', days: 90 },
};

const PERIOD_OPTIONS = ['7d' as PeriodKey, '30d' as PeriodKey, '90d' as PeriodKey];

export default function SalesTrendChart({ storeId }: { storeId?: string }) {
  const [period, setPeriod] = useState<PeriodKey>('30d');
  const [chartData, setChartData] = useState<TrendPoint[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!storeId) return;

    let cancelled = false;
    setLoading(true);

    getSalesTrendData(storeId, PERIOD_MAP[period].days).then((result) => {
      if (cancelled) return;
      if (result.success && result.data) {
        setChartData(result.data);
      } else {
        setError(result.error ?? 'Failed to load trend data');
        setChartData([]);
      }
      setLoading(false);
    });

    return () => { cancelled = true; };
  }, [storeId, period]);

  const formatCurrency = (val: number) => `₹${val.toLocaleString('en-IN', { minimumFractionDigits: 2 })}`;

// Simple JS date formatters to avoid adding date-fns dependency
const formatDateShort = (d: Date) => d.toLocaleDateString('en-IN', { day: 'numeric', month: 'short' });
const formatDateLong = (d: Date) => d.toLocaleDateString('en-IN', { weekday: 'short', day: 'numeric', month: 'short', year: 'numeric' });

  if (!storeId) return <div className="text-gray-400 p-8">Select a store to view trends.</div>;

  return (
    <div className="w-full flex flex-col gap-3">
      <div className="flex items-center justify-between">
        <h3 className="font-bold text-lg text-gray-900 dark:text-white">Sales Trend</h3>
        <select
          value={period}
          onChange={(e) => setPeriod(sanitizeInput(e.target.value) as PeriodKey)}
          className="text-sm border border-gray-200 dark:border-gray-700 bg-white dark:bg-gray-800 rounded-lg text-gray-700 dark:text-gray-200 px-3 py-1 outline-none"
        >
          {PERIOD_OPTIONS.map((key) => (
            <option key={key} value={key}>{PERIOD_MAP[key].label}</option>
          ))}
        </select>
      </div>

      {/* Summary strip */}
      {!loading && chartData.length > 0 && (
        <div className="flex gap-4 text-sm">
          <span className="text-gray-600 dark:text-gray-300">
            <strong>{chartData[chartData.length - 1].totalValue.toLocaleString('en-IN')}</strong> total revenue ({formatCurrency(chartData.reduce((s, d) => s + d.totalValue, 0))}) over {PERIOD_MAP[period].label.toLowerCase()}
          </span>
        </div>
      )}

      {/* Chart area */}
      <div className="h-72 w-full">
        {loading ? (
          <div className="flex h-full items-center justify-center text-gray-400 animate-pulse">Loading chart...</div>
        ) : error && chartData.length === 0 ? (
          <div className="flex h-full items-center justify-center text-red-400">{error}</div>
        ) : chartData.length === 0 ? (
          <div className="flex h-full items-center justify-center text-gray-400">No sales data in this period</div>
        ) : (
          <ResponsiveContainer width="100%" height="100%">
            <LineChart data={chartData} margin={{ top: 5, right: 20, left: 12, bottom: 5 }}>
              <CartesianGrid strokeDasharray="3 3" opacity={0.6} />
              <XAxis
                dataKey="date"
                tickFormatter={(d) => formatDateShort(new Date(d))}
                fontSize={11}
              />
              <YAxis tickFormatter={formatCurrency} fontSize={11} />
              <Tooltip
                content={({ active, payload }) => {
                  if (!active || !payload?.length) return null;
                  const pt = payload[0].payload as TrendPoint;
                  return (
                    <div className="bg-white dark:bg-gray-800 border border-gray-200 dark:border-gray-700 rounded-lg shadow-lg p-3 text-sm space-y-1">
                      <p className="font-semibold">{formatDateLong(new Date(pt.date))}</p>
                      <p className="text-emerald-600 font-medium">Revenue: {formatCurrency(pt.totalValue)}</p>
                      <p className="text-blue-500">{pt.transactionCount} transaction{pt.transactionCount !== 1 ? 's' : ''}</p>
                    </div>
                  );
                }}
              />
              <Line
                type="monotone"
                dataKey="totalValue"
                stroke="#10b981"
                strokeWidth={2.5}
                dot={{ fill: '#10b981', r: 3 }}
                activeDot={{ r: 6, fill: '#059669' }}
              />
            </LineChart>
          </ResponsiveContainer>
        )}
      </div>
    </div>
  );
}
