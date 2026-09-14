"use client";

import { Building2 } from "lucide-react";
import { FormattedAmount } from "@/components/FormattedAmount";
import type { ConsolidatedPLResult } from "@/types/dataconnect";

interface ConsolidatedPLViewProps {
    data: ConsolidatedPLResult;
    daysAgo: number;
}

export default function ConsolidatedPLView({
    data,
    daysAgo,
}: ConsolidatedPLViewProps) {
    return (
        <div className="space-y-8">
            <div className="border-b border-gray-200 dark:border-gray-800 pb-6 space-y-8">
                <div className="flex justify-between items-center mb-8">
                    <div>
                        <h1 className="text-2xl font-bold text-gray-900 dark:text-white">
                            Consolidated P&L View
                        </h1>
                        <p className="text-gray-500 dark:text-gray-400 text-sm mt-1">
                            Aggregated across {data.storesCount} store{data.storesCount !== 1 ? "s" : ""} • Last {daysAgo} days
                        </p>
                    </div>
                </div>

                <div className="grid grid-cols-1 md:grid-cols-3 lg:grid-cols-3 gap-8">
                    <div className="glass-card p-6 flex flex-col justify-between hover:shadow-md transition-shadow duration-300">
                        <h2 className="text-sm font-medium text-gray-500 dark:text-gray-400 mb-1">
                            Total Revenue
                        </h2>
                        <p className="text-3xl font-bold text-gray-900 dark:text-white tracking-tight">
                            <FormattedAmount amount={data.consolidated.totalRevenue} />
                        </p>
                    </div>

                    <div className="glass-card p-6 flex flex-col justify-between hover:shadow-md transition-shadow duration-300">
                        <h2 className="text-sm font-medium text-gray-500 dark:text-gray-400 mb-1">
                            Total Expenses
                        </h2>
                        <p className="text-3xl font-bold text-gray-900 dark:text-white tracking-tight">
                            <FormattedAmount amount={data.consolidated.totalExpenses} />
                        </p>
                    </div>

                    <div className="glass-card p-6 flex flex-col justify-between hover:shadow-md transition-shadow duration-300">
                        <h2 className="text-sm font-medium text-gray-500 dark:text-gray-400 mb-1">
                            Net P&L
                        </h2>
                        <p className="text-3xl font-bold tracking-tight">
                            <FormattedAmount amount={data.consolidated.netPL} />
                        </p>
                    </div>
                </div>
            </div>

            <div className="glass-card p-6 rounded-xl overflow-hidden space-y-4">
                <div className="flex justify-between items-center mb-6">
                    <h2 className="text-lg font-semibold text-gray-900 dark:text-white">
                        Per-Store Breakdown
                    </h2>
                </div>

                <table className="w-full bg-white dark:bg-gray-800 border border-gray-200 dark:border-gray-700 rounded-lg overflow-hidden">
                    <thead className="bg-gray-50 dark:bg-gray-900">
                        <tr>
                            <th className="text-left text-xs font-semibold text-gray-600 dark:text-gray-300 uppercase tracking-wider p-4">
                                Store
                            </th>
                            <th className="text-right text-xs font-semibold text-gray-600 dark:text-gray-300 uppercase tracking-wider p-4">
                                Revenue
                            </th>
                            <th className="text-right text-xs font-semibold text-gray-600 dark:text-gray-300 uppercase tracking-wider p-4">
                                Expenses
                            </th>
                            <th className="text-right text-xs font-semibold text-gray-600 dark:text-gray-300 uppercase tracking-wider p-4">
                                Profit
                            </th>
                        </tr>
                    </thead>
                    <tbody>
                        {data.perStore.map((store) => (
                            <tr
                                key={store.storeId}
                                className="border-b border-gray-100 dark:border-gray-700 last:border-none hover:bg-gray-50 dark:hover:bg-gray-800"
                            >
                                <td className="p-4">
                                    <div className="flex items-center space-x-3">
                                        <Building2
                                            size={16}
                                            className="text-gray-400 dark:text-gray-500"
                                        />
                                        <span className="font-medium text-gray-900 dark:text-white">
                                            {store.storeName}
                                        </span>
                                    </div>
                                </td>
                                <td className="text-right p-4 text-gray-900 dark:text-white font-medium">
                                    <FormattedAmount amount={store.revenue} />
                                </td>
                                <td className="text-right p-4 text-orange-600 dark:text-orange-400 font-medium">
                                    <FormattedAmount amount={store.expenses} />
                                </td>
                                <td className="text-right p-4 font-medium">
                                    <span
                                        className={`${
                                            store.profit >= 0
                                                ? "text-emerald-600 dark:text-emerald-400"
                                                : "text-red-600 dark:text-red-400"
                                        }`}
                                    >
                                        <FormattedAmount amount={store.profit} />
                                    </span>
                                </td>
                            </tr>
                        ))}
                    </tbody>
                </table>
            </div>
        </div>
    );
}
