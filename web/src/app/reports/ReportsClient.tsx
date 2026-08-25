'use client';

import React, { useState, useEffect, useMemo, useRef } from 'react';
import { ChevronLeft, ChevronRight, Loader2, Download } from 'lucide-react';
import { dataConnect } from '@/lib/firebase';
import {
  syncSales,
  syncSaleItems,
  syncItems,
  syncPurchases,
  syncPurchaseItems,
  syncSuppliers,
  getStore,
  getBusinessProfile,
} from '@/dataconnect';
import {
  exportGstr1Report,
  exportGstr2Report,
  exportGstr3BReport,
  exportDetailedGstReport,
} from '@/lib/gstReportExporter';
import { DcSale, DcSaleItem, DcItem } from '@/types/dataconnect';
import Gstr1ReportView from '@/components/reports/Gstr1ReportView';
import Gstr2ReportView from '@/components/reports/Gstr2ReportView';
import Gstr3BReportView from '@/components/reports/Gstr3BReportView';
import DetailedGstReportView from '@/components/reports/DetailedGstReportView';

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

  const [loading, setLoading] = useState<boolean>(false);
  const [salesFetched, setSalesFetched] = useState<boolean>(false);
  const [purchasesFetched, setPurchasesFetched] = useState<boolean>(false);

  // In-flight fetch refs to prevent duplicate requests across renders
  const salesFetchingRef = useRef<boolean>(false);
  const purchasesFetchingRef = useRef<boolean>(false);
  const itemsFetchingRef = useRef<boolean>(false);

  const [allSales, setAllSales] = useState<DcSale[]>([]);
  const [allSaleItems, setAllSaleItems] = useState<DcSaleItem[]>([]);
  const [allPurchases, setAllPurchases] = useState<any[]>([]);
  const [allPurchaseItems, setAllPurchaseItems] = useState<any[]>([]);
  const [allSuppliersMap, setAllSuppliersMap] = useState<Map<string, any>>(new Map());
  const [allItemsMap, setAllItemsMap] = useState<Map<string, DcItem>>(new Map());
  const [businessGstin, setBusinessGstin] = useState<string>('');
  const [businessName, setBusinessName] = useState<string>('StoreBook');

  // 1. Fetch store name and business profile on mount
  useEffect(() => {
    let isMounted = true;
    if (!storeId) return;

    const fetchProfile = async () => {
      try {
        const [storeRes, profileRes] = await Promise.all([
          getStore(dataConnect, { id: storeId }).catch(() => null),
          getBusinessProfile(dataConnect, { storeId }).catch(() => null),
        ]);
        if (!isMounted) return;

        const sName = storeRes?.data?.store?.name;
        if (sName && sName.trim()) {
          setBusinessName(sName.trim());
        }

        const profileGstin = profileRes?.data?.storeProfiles?.[0]?.gstin;
        if (profileGstin && profileGstin.trim()) {
          setBusinessGstin(profileGstin.trim());
        }
      } catch (err) {
        console.error('Failed to load store and business profile:', err);
      }
    };

    fetchProfile();

    return () => {
      isMounted = false;
    };
  }, [storeId]);

  // 2. Fetch master items on mount (isolated to avoid re-triggering tab effects)
  useEffect(() => {
    let isMounted = true;
    if (!storeId || itemsFetchingRef.current) return;
    itemsFetchingRef.current = true;

    const loadItems = async () => {
      try {
        const itemsRes = await syncItems(
          dataConnect,
          { storeId, lastSync: -1 },
          { fetchPolicy: 'SERVER_ONLY' as const }
        );
        if (!isMounted) return;
        const rawMasterItems = itemsRes.data?.items || [];
        const itemMap = new Map<string, DcItem>();
        for (const item of rawMasterItems) {
          itemMap.set(String(item.id).trim(), item);
        }
        setAllItemsMap(itemMap);
      } catch (err) {
        console.error('Failed to fetch master items:', err);
        itemsFetchingRef.current = false;
      }
    };

    loadItems();

    return () => {
      isMounted = false;
    };
  }, [storeId]);

  // Compute month start and end timestamps in ms
  const { startTs, endTs } = useMemo(() => {
    const start = new Date(selectedYear, selectedMonth, 1, 0, 0, 0, 0).getTime();
    const end = new Date(selectedYear, selectedMonth + 1, 0, 23, 59, 59, 999).getTime();
    return { startTs: start, endTs: end };
  }, [selectedMonth, selectedYear]);

  // 3. Tab-selection-wise on-demand data fetching with strict in-flight locks
  useEffect(() => {
    let isMounted = true;
    if (!storeId) return;

    const loadData = async () => {
      // GSTR-1 selected: fetch sales & sale items on demand
      if (activeTab === 'GSTR1' && !salesFetched && !salesFetchingRef.current) {
        salesFetchingRef.current = true;
        setLoading(true);
        try {
          const [salesRes, saleItemsRes] = await Promise.all([
            syncSales(dataConnect, { storeId, lastSync: -1 }, { fetchPolicy: 'SERVER_ONLY' as const }),
            syncSaleItems(dataConnect, { storeId, lastSync: -1 }, { fetchPolicy: 'SERVER_ONLY' as const }),
          ]);
          if (!isMounted) return;
          const rawSales = salesRes.data?.sales || [];
          const rawSaleItems = (saleItemsRes.data?.saleItemDetails || []).filter((i: any) => !i.isDeleted);
          setAllSales(rawSales);
          setAllSaleItems(rawSaleItems);
          const bizGstin = rawSales.find((s) => s.businessGstin)?.businessGstin || '';
          if (bizGstin) setBusinessGstin((prev) => prev || bizGstin);
          setSalesFetched(true);
        } catch (err) {
          console.error('Failed to fetch sales data:', err);
          salesFetchingRef.current = false;
        } finally {
          if (isMounted) setLoading(false);
        }
      }

      // GSTR-2 selected: fetch purchases, purchase items & suppliers on demand
      if (activeTab === 'GSTR2' && !purchasesFetched && !purchasesFetchingRef.current) {
        purchasesFetchingRef.current = true;
        setLoading(true);
        try {
          const [purchasesRes, purchaseItemsRes, suppliersRes] = await Promise.all([
            syncPurchases(dataConnect, { storeId, lastSync: -1 }, { fetchPolicy: 'SERVER_ONLY' as const }),
            syncPurchaseItems(dataConnect, { storeId, lastSync: -1 }, { fetchPolicy: 'SERVER_ONLY' as const }),
            syncSuppliers(dataConnect, { storeId, lastSync: -1 }, { fetchPolicy: 'SERVER_ONLY' as const }),
          ]);
          if (!isMounted) return;
          const rawPurchases = purchasesRes.data?.purchases || [];
          const rawPurchaseItems = (purchaseItemsRes.data?.purchaseItemDetails || []).filter((i: any) => !i.isDeleted);
          const rawSuppliers = suppliersRes.data?.suppliers || [];

          const supplierMap = new Map<string, any>();
          for (const supplier of rawSuppliers) {
            supplierMap.set(String(supplier.id).trim(), supplier);
          }

          setAllPurchases(rawPurchases);
          setAllPurchaseItems(rawPurchaseItems);
          setAllSuppliersMap(supplierMap);
          setPurchasesFetched(true);
        } catch (err) {
          console.error('Failed to fetch purchases data:', err);
          purchasesFetchingRef.current = false;
        } finally {
          if (isMounted) setLoading(false);
        }
      }

      // GSTR-3B or DETAILED selected: ensure both sales and purchases are loaded on demand
      if (activeTab === 'GSTR3B' || activeTab === 'DETAILED') {
        const promises: Promise<void>[] = [];

        if (!salesFetched && !salesFetchingRef.current) {
          salesFetchingRef.current = true;
          promises.push(
            Promise.all([
              syncSales(dataConnect, { storeId, lastSync: -1 }, { fetchPolicy: 'SERVER_ONLY' as const }),
              syncSaleItems(dataConnect, { storeId, lastSync: -1 }, { fetchPolicy: 'SERVER_ONLY' as const }),
            ]).then(([salesRes, saleItemsRes]) => {
              if (!isMounted) return;
              const rawSales = salesRes.data?.sales || [];
              const rawSaleItems = (saleItemsRes.data?.saleItemDetails || []).filter((i: any) => !i.isDeleted);
              setAllSales(rawSales);
              setAllSaleItems(rawSaleItems);
              const bizGstin = rawSales.find((s) => s.businessGstin)?.businessGstin || '';
              if (bizGstin) setBusinessGstin((prev) => prev || bizGstin);
              setSalesFetched(true);
            }).catch((err) => {
              console.error('Failed to fetch sales data:', err);
              salesFetchingRef.current = false;
            })
          );
        }

        if (!purchasesFetched && !purchasesFetchingRef.current) {
          purchasesFetchingRef.current = true;
          promises.push(
            Promise.all([
              syncPurchases(dataConnect, { storeId, lastSync: -1 }, { fetchPolicy: 'SERVER_ONLY' as const }),
              syncPurchaseItems(dataConnect, { storeId, lastSync: -1 }, { fetchPolicy: 'SERVER_ONLY' as const }),
              syncSuppliers(dataConnect, { storeId, lastSync: -1 }, { fetchPolicy: 'SERVER_ONLY' as const }),
            ]).then(([purchasesRes, purchaseItemsRes, suppliersRes]) => {
              if (!isMounted) return;
              const rawPurchases = purchasesRes.data?.purchases || [];
              const rawPurchaseItems = (purchaseItemsRes.data?.purchaseItemDetails || []).filter((i: any) => !i.isDeleted);
              const rawSuppliers = suppliersRes.data?.suppliers || [];

              const supplierMap = new Map<string, any>();
              for (const supplier of rawSuppliers) {
                supplierMap.set(String(supplier.id).trim(), supplier);
              }

              setAllPurchases(rawPurchases);
              setAllPurchaseItems(rawPurchaseItems);
              setAllSuppliersMap(supplierMap);
              setPurchasesFetched(true);
            }).catch((err) => {
              console.error('Failed to fetch purchases data:', err);
              purchasesFetchingRef.current = false;
            })
          );
        }

        if (promises.length > 0) {
          setLoading(true);
          try {
            await Promise.all(promises);
          } catch (err) {
            console.error('Failed to fetch consolidated reports data:', err);
          } finally {
            if (isMounted) setLoading(false);
          }
        }
      }
    };

    loadData();

    return () => {
      isMounted = false;
    };
  }, [storeId, activeTab, salesFetched, purchasesFetched]);

  const isCurrentTabLoading =
    loading ||
    (activeTab === 'GSTR1' && !salesFetched) ||
    (activeTab === 'GSTR2' && !purchasesFetched) ||
    ((activeTab === 'GSTR3B' || activeTab === 'DETAILED') && (!salesFetched || !purchasesFetched));

  // In-memory filter for sales in the selected reporting month matching Android
  const monthlySales = useMemo(() => {
    return allSales.filter((sale) => {
      if (sale.isDeleted) return false;
      const isEstimate = typeof sale.type === 'string' && sale.type.toUpperCase() === 'ESTIMATE';
      if (isEstimate) return false;
      const rawTs = Number(sale.timestamp) || 0;
      const saleTs = rawTs < 100000000000 ? rawTs * 1000 : rawTs;
      return saleTs >= startTs && saleTs <= endTs;
    });
  }, [allSales, startTs, endTs]);

  // In-memory filter for purchases in the selected reporting month matching Android
  const monthlyPurchases = useMemo(() => {
    return allPurchases.filter((purchase) => {
      if (purchase.isDeleted) return false;
      const rawTs = Number(purchase.timestamp) || 0;
      const purchaseTs = rawTs < 100000000000 ? rawTs * 1000 : rawTs;
      return purchaseTs >= startTs && purchaseTs <= endTs;
    });
  }, [allPurchases, startTs, endTs]);

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
    } else if (activeTab === 'GSTR2') {
      exportGstr2Report(
        monthlyPurchases,
        allPurchaseItems,
        allSuppliersMap,
        allItemsMap,
        businessGstin,
        businessName,
        MONTH_NAMES[selectedMonth],
        selectedYear
      );
    } else if (activeTab === 'GSTR3B') {
      exportGstr3BReport(
        monthlySales,
        allSaleItems,
        monthlyPurchases,
        allPurchaseItems,
        allSuppliersMap,
        allItemsMap,
        businessGstin,
        businessName,
        MONTH_NAMES[selectedMonth],
        selectedYear
      );
    } else if (activeTab === 'DETAILED') {
      exportDetailedGstReport(
        monthlySales,
        allSaleItems,
        monthlyPurchases,
        allPurchaseItems,
        allSuppliersMap,
        allItemsMap,
        businessGstin,
        businessName,
        MONTH_NAMES[selectedMonth],
        selectedYear
      );
    }
  };

  const isExportDisabled =
    (activeTab === 'GSTR1' && monthlySales.length === 0) ||
    (activeTab === 'GSTR2' && monthlyPurchases.length === 0) ||
    ((activeTab === 'GSTR3B' || activeTab === 'DETAILED') &&
      monthlySales.length === 0 &&
      monthlyPurchases.length === 0);

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
                : 'bg-white dark:bg-gray-800 text-gray-600 dark:text-gray-300 border border-gray-200 dark:border-gray-700 hover:bg-gray-50 dark:hover:bg-gray-700'
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
          disabled={isExportDisabled}
          className="h-[60px] w-full px-6 rounded-2xl bg-indigo-950 hover:bg-indigo-900 dark:bg-indigo-900 dark:hover:bg-indigo-800 text-white font-bold text-sm flex items-center justify-center space-x-2 shadow-sm hover:shadow-md transition-all disabled:opacity-50 disabled:cursor-not-allowed"
        >
          <Download size={18} />
          <span>Export {currentTabMeta.label} Excel Report</span>
        </button>
      </div>

      {/* 4. Content Area */}
      {isCurrentTabLoading ? (
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

          {activeTab === 'GSTR2' && (
            <Gstr2ReportView
              purchases={monthlyPurchases}
              purchaseItems={allPurchaseItems}
              suppliersMap={allSuppliersMap}
              allItemsMap={allItemsMap}
              businessGstin={businessGstin}
              businessName={businessName}
              monthName={MONTH_NAMES[selectedMonth]}
              year={selectedYear}
            />
          )}

          {activeTab === 'GSTR3B' && (
            <Gstr3BReportView
              sales={monthlySales}
              saleItems={allSaleItems}
              purchases={monthlyPurchases}
              purchaseItems={allPurchaseItems}
              suppliersMap={allSuppliersMap}
              allItemsMap={allItemsMap}
              businessGstin={businessGstin}
              businessName={businessName}
              monthName={MONTH_NAMES[selectedMonth]}
              year={selectedYear}
            />
          )}

          {activeTab === 'DETAILED' && (
            <DetailedGstReportView
              sales={monthlySales}
              saleItems={allSaleItems}
              purchases={monthlyPurchases}
              purchaseItems={allPurchaseItems}
              suppliersMap={allSuppliersMap}
              allItemsMap={allItemsMap}
              businessGstin={businessGstin}
              businessName={businessName}
              monthName={MONTH_NAMES[selectedMonth]}
              year={selectedYear}
            />
          )}
        </>
      )}
    </div>
  );
}
