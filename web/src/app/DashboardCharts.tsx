import { sanitizeInput } from '@/lib/sanitize';
'use client';

import { useState } from 'react';
import { PieChart, Pie, Cell, ResponsiveContainer, Tooltip, Legend } from 'recharts';

const COLORS = ['#0088FE', '#00C49F', '#FFBB28', '#FF8042', '#8884d8', '#82ca9d'];

export default function DashboardCharts({ 
  salesData = [], 
  itemsData = [],
  saleItemsData = []
}: { 
  salesData: any[], 
  itemsData: any[],
  saleItemsData?: any[]
}) {
  const [chartType, setChartType] = useState<'customer' | 'product' | 'fast' | 'dead' | 'profit'>('customer');

  const customerData = salesData.reduce((acc: any, sale: any) => {
    const name = sale.customer_name || 'Walk-in';
    const existing = acc.find((item: any) => item.name === name);
    if (existing) {
      existing.value += sale.total_amount || 0;
    } else {
      acc.push({ name, value: sale.total_amount || 0 });
    }
    return acc;
  }, []).sort((a: any, b: any) => b.value - a.value).slice(0, 5);

  const productData = itemsData.reduce((acc: any, item: any) => {
    const category = item.category || 'Uncategorized';
    const existing = acc.find((i: any) => i.name === category);
    if (existing) {
      existing.value += item.quantity || 0;
    } else {
      acc.push({ name: category, value: item.quantity || 0 });
    }
    return acc;
  }, []).sort((a: any, b: any) => b.value - a.value).slice(0, 5);

  // Fast Moving Items
  const fastMovingData = (saleItemsData || []).reduce((acc: any, item: any) => {
    const existing = acc.find((i: any) => i.name === item.itemName);
    if (existing) {
      existing.value += item.quantity || 0;
    } else {
      acc.push({ name: item.itemName, value: item.quantity || 0 });
    }
    return acc;
  }, []).sort((a: any, b: any) => b.value - a.value).slice(0, 5);

  // Profit Margin Items
  const profitMarginData = (saleItemsData || []).reduce((acc: any, item: any) => {
    const profit = (item.sellPrice - item.buyPrice) * item.quantity;
    if (profit > 0) {
      const existing = acc.find((i: any) => i.name === item.itemName);
      if (existing) {
        existing.value += profit;
      } else {
        acc.push({ name: item.itemName, value: profit });
      }
    }
    return acc;
  }, []).sort((a: any, b: any) => b.value - a.value).slice(0, 5);

  // Dead Stock Items (High quantity, low sales)
  const deadStockData = itemsData
    .map((item: any) => ({ name: item.name, value: item.quantity || 0 }))
    .sort((a: any, b: any) => b.value - a.value)
    .slice(0, 5); // Simplistic dead stock: just highest raw quantity for demo

  let data = [];
  let title = '';
  if (chartType === 'customer') { data = customerData; title = 'Top Revenue by Customer'; }
  if (chartType === 'product') { data = productData; title = 'Inventory by Category'; }
  if (chartType === 'fast') { data = fastMovingData; title = 'Fast Moving Products'; }
  if (chartType === 'profit') { data = profitMarginData; title = 'Top Profit Margin Items'; }
  if (chartType === 'dead') { data = deadStockData; title = 'Dead Stock Alert (High Qty)'; }

  return (
    <div className="w-full h-full flex flex-col">
      <div className="flex justify-between items-center mb-4">
        <h3 className="font-bold text-gray-900 dark:text-white">{title}</h3>
        <select 
          value={chartType} 
          onChange={(e) => setChartType(sanitizeInput(e.target.value) as any)}
          className="text-sm border-gray-200 dark:border-gray-700 bg-white dark:bg-gray-800 rounded-lg dark:text-gray-200 p-1 outline-none"
        >
          <option value="customer">By Customer</option>
          <option value="product">By Category</option>
          <option value="fast">Fast Moving</option>
          <option value="profit">Top Profit Margin</option>
          <option value="dead">Dead Stock</option>
        </select>
      </div>
      <div className="flex-1 min-h-0">
        {data.length > 0 ? (
          <ResponsiveContainer width="100%" height="100%">
            <PieChart>
              <Pie
                data={data}
                cx="50%"
                cy="50%"
                innerRadius={60}
                outerRadius={80}
                paddingAngle={5}
                dataKey="value"
              >
                {data.map((entry: any, index: number) => (
                  <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                ))}
              </Pie>
              <Tooltip 
                formatter={(value: any) => (chartType === 'customer' || chartType === 'profit') ? `₹${Number(value).toFixed(2)}` : `${value} units`}
                contentStyle={{ borderRadius: '8px', border: 'none', boxShadow: '0 4px 6px -1px rgb(0 0 0 / 0.1)' }}
              />
              <Legend verticalAlign="bottom" height={36}/>
            </PieChart>
          </ResponsiveContainer>
        ) : (
          <div className="flex h-full items-center justify-center text-gray-400">No data available</div>
        )}
      </div>
    </div>
  );
}
