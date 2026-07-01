'use client';

import { useState } from 'react';
import { PieChart, Pie, Cell, ResponsiveContainer, Tooltip, Legend } from 'recharts';

const COLORS = ['#0088FE', '#00C49F', '#FFBB28', '#FF8042', '#8884d8', '#82ca9d'];

export default function DashboardCharts({ salesData, itemsData }: { salesData: any[], itemsData: any[] }) {
  const [chartType, setChartType] = useState<'customer' | 'product'>('customer');

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

  const data = chartType === 'customer' ? customerData : productData;
  const title = chartType === 'customer' ? 'Top Revenue by Customer' : 'Inventory by Category';

  return (
    <div className="w-full h-full flex flex-col">
      <div className="flex justify-between items-center mb-4">
        <h3 className="font-bold text-gray-900 dark:text-white">{title}</h3>
        <select 
          value={chartType} 
          onChange={(e) => setChartType(e.target.value as 'customer' | 'product')}
          className="text-sm border-gray-200 dark:border-gray-700 bg-white dark:bg-gray-800 rounded-lg dark:text-gray-200 p-1"
        >
          <option value="customer">By Customer</option>
          <option value="product">By Product Category</option>
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
                formatter={(value: any) => chartType === 'customer' ? `₹${Number(value).toFixed(2)}` : `${value} units`}
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
