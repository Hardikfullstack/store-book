'use client';

import React, { useState, useEffect, useMemo } from 'react';
import { ChevronLeft, ChevronRight, Loader2, Download, FileSpreadsheet } from 'lucide-react';
import { dataConnect } from '@/lib/firebase';
import { getActiveSaleItems, syncSales, syncItems } from '@/dataconnect';
import { exportGstr1Report } from '@/lib/gstReportExporter';
import Gstr1ReportView from '@/components/reports/Gstr1ReportView';

type GSTReportType = 'GSTR1' | 'GSTR2' | 'GSTR3B' | 'DETAILED';

interface ReportMeta {
  id: GSTReportType;
  label: string;
  subtitle: string;
}

const REPORT_TABS: ReportMeta[] = [
  { id: 'GSTR1', label: 'GSTR-1', subtitle: 'Sales / Outward Supplies' },
  { id: 'GSTR2', label: 'GSTR-2', subtitle: 'Purchases / Inward Supplies' },
  { id: 'GSTR3B', label: 'GSTR-3B', subtitle: 'Consolidated Summary' },
  { id: 'DETAILED', label: 'Detailed GST', subtitle: 'Item-wise Breakup' },
];

const MONTH_NAMES = [
  'Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun',
  'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'
];

export default function ReportsClient({ storeId }: { storeId: string }) {
  const [activeTab, setActiveTab] = useState<GSTReportType>('GSTR1');

  // Month navigation state (defaults to current month)
  const now = new Date();
  const [selectedMonth, setSelectedMonth] = useState<number>(now.getMonth());
  const [selectedYear, setSelectedYear] = useState<number>(now.getFullYear());

  const [loading, setLoading] = useState<boolean>(true);
  const [allSales, setAllSales] = useState<any[]>([]);
  const [allSaleItems, setAllSaleItems] = useState<any[]>([]);
  const [allItemsMap, setAllItemsMap] = useState<Map<string, any>>(new Map());
  const [businessGstin, setBusinessGstin] = useState<string>('');
  const [businessName, setBusinessName] = useState<string>('StoreBook');

  // Compute month start and end timestamps in ms
  const { startTs, endTs } = useMemo(() => {
    const start = new Date(selectedYear, selectedMonth, 1, 0, 0, 0, 0).getTime();
    const end = new Date(selectedYear, selectedMonth + 1, 0, 23, 59, 59, 999).getTime();
    return { startTs: start, endTs: end };
  }, [selectedMonth, selectedYear]);

  // Fetch sales, saleItems, and master items from DataConnect ONCE on mount
  useEffect(() => {
    let isMounted = true;

    const fetchData = async () => {
      if (!storeId) return;
      setLoading(true);
      try {
        const [salesRes, saleItemsRes, itemsRes] = await Promise.all([
          syncSales(dataConnect, { storeId, lastSync: 0 }, { fetchPolicy: 'SERVER_ONLY' as const }),
          getActiveSaleItems(dataConnect, { storeId }, { fetchPolicy: 'SERVER_ONLY' as const }),
          syncItems(dataConnect, { storeId, lastSync: 0 }, { fetchPolicy: 'SERVER_ONLY' as const }),
        ]);

        if (!isMounted) return;

        const rawSales = salesRes.data?.sales || [];
        const rawItems = saleItemsRes.data?.saleItemDetails || [];
        const rawMasterItems = itemsRes.data?.items || [];

        const itemMap = new Map<string, any>();
        for (const item of rawMasterItems) {
          itemMap.set(item.id, item);
        }

        setAllSales(rawSales);
        setAllSaleItems(rawItems);
        setAllItemsMap(itemMap);

        // Derive business GSTIN from latest sale if available
        const bizGstin = rawSales.find((s: any) => s.businessGstin)?.businessGstin || '';
        setBusinessGstin(bizGstin);
      } catch (err) {
        console.error('Failed to fetch GST reports data:', err);
      } finally {
        if (isMounted) setLoading(false);
      }
    };

    fetchData();

    return () => {
      isMounted = false;
    };
  }, [storeId]);

  // Pure in-memory filter for the selected reporting month
  const monthlySales = useMemo(() => {
    return allSales.filter((sale) => {
      if (sale.isDeleted) return false;
      if (sale.type === 'ESTIMATE') return false;
      const rawTs = Number(sale.timestamp) || 0;
      const saleTs = rawTs > 10000000000 ? rawTs : rawTs * 1000;
      return saleTs >= startTs && saleTs <= endTs;
    });
  }, [allSales, startTs, endTs]);

  const handlePrevMonth = () => {
    if (selectedMonth === 0) {
      setSelectedMonth(11);
      setSelectedYear((prev) => prev - 1);
    } else {
      setSelectedMonth((prev) => prev - 1);
    }
  };

  const handleNextMonth = () => {
    if (selectedMonth === 11) {
      setSelectedMonth(0);
      setSelectedYear((prev) => prev + 1);
    } else {
      setSelectedMonth((prev) => prev + 1);
    }
  };

  const handleExport = () => {
    if (activeTab === 'GSTR1') {
      exportGstr1Report(
        monthlySales,
        allSaleItems,
        allItemsMap,
        businessGstin,
        businessName,
        MONTH_NAMES[selectedMonth],
        selectedYear
      );
    }
  };

  const currentTabMeta = REPORT_TABS.find((t) => t.id === activeTab) || REPORT_TABS[0];

  return (
    <div className="max-w-4xl mx-auto flex flex-col h-[calc(100vh-5rem)] space-y-4 overflow-hidden">
      {/* 1. Top Header */}
      <div>
        <h1 className="text-2xl font-black text-gray-900 dark:text-white">
          GST Compliance Reports
        </h1>
        <p className="text-sm font-medium text-gray-500 dark:text-gray-400 mt-0.5">
          {currentTabMeta.subtitle}
        </p>
      </div>

      {/* 2. Top Segmented Navigation Tabs */}
      <div className="flex items-center space-x-2 overflow-x-auto pb-1 no-scrollbar">
        {REPORT_TABS.map((tab) => {
          const isActive = activeTab === tab.id;
          return (
            <button
              key={tab.id}
              type="button"
              onClick={() => setActiveTab(tab.id)}
              className={`px-5 py-2 rounded-full text-xs font-bold transition-all whitespace-nowrap ${isActive
                  ? 'bg-indigo-950 dark:bg-indigo-900 text-white shadow-md shadow-indigo-950/20'
                  : 'bg-white dark:bg-gray-800 text-gray-600 dark:text-gray-300 border border-gray-200 dark:border-gray-700 hover:bg-gray-50 dark:hover:bg-gray-750'
                }`}
            >
              {tab.label}
            </button>
          );
        })}
      </div>

      {/* 3. Controls Bar: 50% Left Month Picker | 50% Right Export Button */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-4 items-center">
        {/* Left 50%: Month-wise Reporting Period Picker */}
        <div className="h-[60px] px-3.5 bg-white dark:bg-gray-800 border border-gray-100 dark:border-gray-700/80 rounded-2xl shadow-sm flex items-center justify-between">
          <button
            type="button"
            onClick={handlePrevMonth}
            className="p-2 rounded-xl text-gray-600 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-700 transition-colors"
            title="Previous Month"
          >
            <ChevronLeft size={20} />
          </button>

          <div className="text-center flex flex-col justify-center">
            <div className="text-base font-black text-gray-900 dark:text-white leading-tight">
              {MONTH_NAMES[selectedMonth]} {selectedYear}
            </div>
            <div className="text-[11px] font-medium text-gray-400 dark:text-gray-500 leading-tight mt-0.5">
              Reporting Period
            </div>
          </div>

          <button
            type="button"
            onClick={handleNextMonth}
            className="p-2 rounded-xl text-gray-600 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-700 transition-colors"
            title="Next Month"
          >
            <ChevronRight size={20} />
          </button>
        </div>

        {/* Right 50%: Export Button */}
        <button
          type="button"
          onClick={handleExport}
          disabled={monthlySales.length === 0}
          className="h-[60px] w-full px-6 rounded-2xl bg-indigo-950 hover:bg-indigo-900 dark:bg-indigo-900 dark:hover:bg-indigo-800 text-white font-bold text-sm flex items-center justify-center space-x-2 shadow-sm hover:shadow-md transition-all disabled:opacity-50 disabled:cursor-not-allowed"
        >
          <Download size={18} />
          <span>Export {currentTabMeta.label} Excel Report</span>
        </button>
      </div>

      {/* 4. Content Area */}
      {loading ? (
        <div className="py-20 flex flex-col items-center justify-center space-y-3">
          <Loader2 className="w-8 h-8 text-indigo-950 dark:text-indigo-400 animate-spin" />
          <p className="text-xs font-semibold text-gray-500 dark:text-gray-400">
            Calculating GST compliance summaries...
          </p>
        </div>
      ) : (
        <>
          {activeTab === 'GSTR1' && (
            <Gstr1ReportView
              sales={monthlySales}
              saleItems={allSaleItems}
              allItemsMap={allItemsMap}
              businessGstin={businessGstin}
              businessName={businessName}
              monthName={MONTH_NAMES[selectedMonth]}
              year={selectedYear}
            />
          )}

          {activeTab !== 'GSTR1' && (
            <div className="glass-card p-12 text-center border border-gray-100 dark:border-gray-800 bg-white dark:bg-gray-800 rounded-2xl space-y-2">
              <div className="w-12 h-12 rounded-full bg-indigo-50 dark:bg-indigo-950/40 text-indigo-600 dark:text-indigo-400 flex items-center justify-center mx-auto mb-3">
                <FileSpreadsheet size={24} />
              </div>
              <h3 className="text-base font-bold text-gray-900 dark:text-white">
                {currentTabMeta.label} Report ({currentTabMeta.subtitle})
              </h3>
              <p className="text-xs text-gray-500 dark:text-gray-400 max-w-sm mx-auto">
                This report view will be populated in subsequent release stories of Epic e46.
              </p>
            </div>
          )}
        </>
      )}
    </div>
  );
}
