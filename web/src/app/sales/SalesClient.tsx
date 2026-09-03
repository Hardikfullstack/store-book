"use client";

import { useState, useEffect, useRef, useMemo } from "react";
import {
	Plus,
	Search,
	Trash2,
	Loader2,
	Download,
	X,
	Clock,
	Users,
	Package,
} from "lucide-react";
import Pagination from "@/app/components/Pagination";
import jsPDF from "jspdf";
import autoTable from "jspdf-autotable";
import ExportButtons from "@/app/ExportButtons";
import { dataConnect } from "@/lib/firebase";
import { executeQuery } from "firebase/data-connect";
import { sanitizeInput } from "@/lib/sanitize";
import {
	getActiveSalesRef,
	softDeleteSale,
	getSalesCountRef,
	getActiveSaleItemsRef,
	OrderDirection,
	getSaleItemsBySaleId
} from "@/dataconnect";
import { FormattedAmount } from "@/components/FormattedAmount";
import SalesPOS from "./SalesPOS";
import DynamicTable, {
	TableColumn,
	TableRowAction,
} from "@/components/DynamicTable";
import type { GetActiveSaleItemsData } from "@/dataconnect";

interface SaleRowBase {
	id: string;
	timestamp: number;
	totalAmount: number;
	discountAmount: number;
	customerName?: string | null;
	type: string;
	notes?: string | null;
	updatedAt: number;
	customer_name?: string | null;
	total_amount: number;
	is_deleted: number;
	cloud_id?: string;
}

type SaleRow = { [K in keyof SaleRowBase]?: SaleRowBase[K] } & { id: string; timestamp: number; totalAmount: number; discountAmount: number; type: string; updatedAt: number; is_deleted: number } & Record<string, unknown>;

type SaleItemRow = GetActiveSaleItemsData["saleItemDetails"][number];

export type GroupByOption = "timeline" | "customer" | "product";

export interface CustomerSalesSummary extends Record<string, unknown> {
	id: string;
	customer_name: string;
	totalOrders: number;
	totalSpent: number;
	lastPurchase: number;
	averageOrderValue: number;
}

export interface ProductSalesSummary extends Record<string, unknown> {
	id: string;
	itemName: string;
	totalQuantity: number;
	totalRevenue: number;
	totalProfit: number;
	orderCount: number;
}

interface SaleVars {
	storeId: string;
	type: string;
	orderByTimestamp?: OrderDirection;
	orderByCustomerName?: OrderDirection;
	orderByTotalAmount?: OrderDirection;
	minAmount?: number;
	maxAmount?: number;
	startDate?: number;
	endDate?: number;
	searchTerm?: string;
	limit?: number;
	offset?: number;
}

export default function SalesClient({
	initialSales,
	maxDiscountPercent = 100,
	canDeleteRecords = true,
	storeId,
	isPremium,
}: {
	initialSales: SaleRow[];
	maxDiscountPercent?: number;
	canDeleteRecords?: boolean;
	storeId?: string;
	isPremium?: boolean;
}) {
	const [sales, setSales] = useState(initialSales);
	const [groupBy, setGroupBy] = useState<GroupByOption>("timeline");
	const [refreshTrigger, setRefreshTrigger] = useState(0);
	const [sortField, setSortField] = useState<string | null>("timestamp");
	const [sortDirection, setSortDirection] = useState<"asc" | "desc">("desc");
	const [dataVersion, setDataVersion] = useState(0);
	const fetchedPagesAtVersionRef = useRef<Map<string, number>>(new Map());
	const [showModal, setShowModal] = useState(false);
	const [debouncedSearch, setDebouncedSearch] = useState("");
	const [searchResults, setSearchResults] = useState<SaleRow[]>([]);
	const [searchQuery, setSearchQuery] = useState("");
	const [minAmountFilter, setMinAmountFilter] = useState("");
	const [maxAmountFilter, setMaxAmountFilter] = useState("");
	const [startDateFilter, setStartDateFilter] = useState("");
	const [endDateFilter, setEndDateFilter] = useState("");
	const [selectedSale, setSelectedSale] = useState<SaleRow | null>(null);
	const [selectedSaleItems, setSelectedSaleItems] = useState<SaleItemRow[]>([]);
	const [isLoadingDetails, setIsLoadingDetails] = useState(false);
	const debounceRef = useRef<ReturnType<typeof setTimeout> | null>(null);
	const searchResultsKeyRef = useRef("");
	const [allStoreSaleItems, setAllStoreSaleItems] = useState<SaleItemRow[]>([]);
	const [isLoadingSaleItems, setIsLoadingSaleItems] = useState(false);

	const handleRowClick = async (sale: SaleRow) => {
		setSelectedSale(sale);
		setIsLoadingDetails(true);
		try {
			const resp = await getSaleItemsBySaleId(dataConnect, { saleId: sale.id });
			setSelectedSaleItems(resp.data.saleItemDetails || []);
		} catch (e) {
			console.error('Error fetching sale details:', e);
			setSelectedSaleItems([]);
		} finally {
			setIsLoadingDetails(false);
		}
	};

	const buildSortVars = (
		sortField: string | null,
		direction: "asc" | "desc",
	) => {
		if (!sortField || sortField === "timestamp") {
			const dir =
				sortField === "timestamp" && direction === "asc"
					? OrderDirection.ASC
					: OrderDirection.DESC;
			return { orderByTimestamp: dir };
		}
		const dir =
			direction === "asc" ? OrderDirection.ASC : OrderDirection.DESC;
		return {
			orderByCustomerName:
				sortField === "customer_name" ? dir : undefined,
			orderByTotalAmount: sortField === "total_amount" ? dir : undefined,
		};
	};

	const invalidateAllPages = () => {
		setDataVersion((v) => v + 1);
		fetchedPagesAtVersionRef.current = new Map();
	};

	const handleSort = (field: string) => {
		invalidateAllPages();
		setCurrentPage(1);
		if (field === sortField) {
			setSortDirection((prev) => (prev === "asc" ? "desc" : "asc"));
		} else {
			setSortField(field);
			setSortDirection("asc");
		}
	};

	// Pagination state
	const [currentPage, setCurrentPage] = useState(1);
	const pageSize = 10;
	const [totalItems, setTotalItems] = useState(0);
	const [isLoading, setIsLoading] = useState(false);

	// Fetch total count — skip when searching, filtering, or grouping
	useEffect(() => {
		if (
			!isPremium ||
			!storeId ||
			debouncedSearch ||
			minAmountFilter ||
			maxAmountFilter ||
			startDateFilter ||
			endDateFilter ||
			groupBy !== "timeline"
		)
			return;
		const countKey = `count-SALE`;
		const needsServerFetch =
			(fetchedPagesAtVersionRef.current.get(countKey) ?? -1) <
			dataVersion;
		const options = needsServerFetch
			? { fetchPolicy: "SERVER_ONLY" as const }
			: undefined;

		const fetchTotal = async () => {
			try {
				const resp = await executeQuery(
					getSalesCountRef(dataConnect, { storeId, type: "SALE" }),
					options,
				);
				setTotalItems(resp.data.sales.length);
				fetchedPagesAtVersionRef.current.set(countKey, dataVersion);
			} catch (e) {
				console.error("Count fetch error:", e);
			}
		};
		fetchTotal();
		// eslint-disable-next-line react-hooks/exhaustive-deps
	}, [
		isPremium,
		storeId,
		refreshTrigger,
		debouncedSearch,
		minAmountFilter,
		maxAmountFilter,
		startDateFilter,
		endDateFilter,
	]);

	// Fetch paginated sales
	useEffect(() => {
		if (!isPremium || !storeId) return;
		let isMounted = true;
		const fetchPaginated = async () => {
			setIsLoading(true);
			try {
				const isSearching = debouncedSearch.length >= 3;
				const isFiltering =
					!!minAmountFilter ||
					!!maxAmountFilter ||
					!!startDateFilter ||
					!!endDateFilter;
				const isGrouped = groupBy !== "timeline";
				const needsFullFetch = isSearching || isFiltering || isGrouped;
				const currentSearchKey = `${groupBy}-${debouncedSearch}-${sortField}-${sortDirection}-${minAmountFilter}-${maxAmountFilter}-${startDateFilter}-${endDateFilter}`;

				if (
					needsFullFetch &&
					searchResults.length > 0 &&
					searchResultsKeyRef.current === currentSearchKey
				) {
					setIsLoading(false);
					return;
				}

				const offset = (currentPage - 1) * pageSize;
				const pageKey = `page-${currentPage}-${sortField}-${sortDirection}-${debouncedSearch}-${minAmountFilter}-${maxAmountFilter}-${startDateFilter}-${endDateFilter}`;
				const needsServerFetch =
					(fetchedPagesAtVersionRef.current.get(pageKey) ?? -1) <
					dataVersion;
				const options = needsServerFetch
					? { fetchPolicy: "SERVER_ONLY" as const }
					: undefined;

				const vars: SaleVars = {
					storeId,
					type: "SALE",
					...buildSortVars(sortField, sortDirection),
				};
				if (minAmountFilter) vars.minAmount = Number(minAmountFilter);
				if (maxAmountFilter) vars.maxAmount = Number(maxAmountFilter);
				if (startDateFilter)
					vars.startDate = new Date(startDateFilter).getTime();
				if (endDateFilter)
					vars.endDate = new Date(endDateFilter).setHours(
						23,
						59,
						59,
						999,
					);

				if (needsFullFetch) {
					if (isSearching && groupBy !== "product") vars.searchTerm = debouncedSearch;
				} else {
					vars.limit = pageSize;
					vars.offset = offset;
				}

				const response = await executeQuery(
					getActiveSalesRef(dataConnect, vars),
					options,
				);

				if (!isMounted) return;

				fetchedPagesAtVersionRef.current.set(pageKey, dataVersion);

				const updated: SaleRow[] = response.data.sales.map((sale) => ({
					...sale,
					is_deleted: 0,
					updated_at: sale.updatedAt || Date.now(),
					customer_name: sale.customerName,
					total_amount: sale.totalAmount,
				}));

				if (!sortField || sortField === "timestamp") {
					updated.sort((a, b) => {
						const tA = Number(a.timestamp) || Number(a.updated_at) || 0;
						const tB = Number(b.timestamp) || Number(b.updated_at) || 0;
						return sortDirection === "asc" && sortField === "timestamp"
							? tA - tB
							: tB - tA;
					});
				}

				if (needsFullFetch) {
					searchResultsKeyRef.current = currentSearchKey;
					setSearchResults(updated);
					setTotalItems(updated.length);
					setSales(updated.slice(0, pageSize));
				} else {
					searchResultsKeyRef.current = "";
					setSearchResults([]);
					setSales(updated);
				}
			} catch (error) {
				console.error("Data Connect sales sync error:", error);
			} finally {
				if (isMounted) setIsLoading(false);
			}
		};
		fetchPaginated();
		const intervalId = setInterval(fetchPaginated, 30000);
		return () => {
			isMounted = false;
			clearInterval(intervalId);
		};
		// eslint-disable-next-line react-hooks/exhaustive-deps
	}, [
		isPremium,
		storeId,
		currentPage,
		refreshTrigger,
		dataVersion,
		debouncedSearch,
		minAmountFilter,
		maxAmountFilter,
		startDateFilter,
		endDateFilter,
		groupBy,
	]);
	const [saleItemsMap, setSaleItemsMap] = useState<Record<string, SaleItemRow[]>>({});

	const fetchSaleItems = async (saleId: string) => {
		if (saleItemsMap[saleId] !== undefined) return;
		try {
			if (!storeId) return;
			const resp = await executeQuery(
				getActiveSaleItemsRef(dataConnect, { storeId: storeId }),
			);
			const allItems = (resp.data?.saleItemDetails as unknown as SaleItemRow[]) || [];
			const saleItems = allItems.filter((i) => i.saleId === saleId);
			setSaleItemsMap((prev) => ({ ...prev, [saleId]: saleItems }));
		} catch (e) {
			console.error("Failed to fetch sale items:", e);
		}
	};

	const handleSearchChange = (value: string) => {
		setSearchQuery(value);
		if (debounceRef.current) clearTimeout(debounceRef.current);
		const trimmed = value.trim();
		if (trimmed.length === 0) {
			setDebouncedSearch("");
			setCurrentPage(1);
			return;
		}
		if (trimmed.length < 3) return;
		debounceRef.current = setTimeout(() => {
			setDebouncedSearch(trimmed);
			setCurrentPage(1);
		}, 400);
	};

	const handlePageChange = (page: number) => {
		setCurrentPage(page);
		if (groupBy === "timeline") {
			const isFiltering =
				!!minAmountFilter ||
				!!maxAmountFilter ||
				!!startDateFilter ||
				!!endDateFilter;
			if ((debouncedSearch || isFiltering) && searchResults.length > 0) {
				const start = (page - 1) * pageSize;
				setSales(searchResults.slice(start, start + pageSize));
			}
		}
	};

	// Fetch all active sale items when in product grouping mode
	useEffect(() => {
		if (groupBy !== "product" || !storeId) return;
		let isMounted = true;
		const loadItems = async () => {
			setIsLoadingSaleItems(true);
			try {
				const resp = await executeQuery(
					getActiveSaleItemsRef(dataConnect, { storeId }),
				);
				if (isMounted) {
					const items =
						(resp.data?.saleItemDetails as unknown as SaleItemRow[]) || [];
					setAllStoreSaleItems(items);
				}
			} catch (err) {
				console.error("Failed to load sale items for product grouping:", err);
			} finally {
				if (isMounted) {
					setIsLoadingSaleItems(false);
				}
			}
		};
		loadItems();
		return () => {
			isMounted = false;
		};
	}, [groupBy, storeId, refreshTrigger, dataVersion]);

	// Customer Grouping Aggregation
	const customerGroups = useMemo<CustomerSalesSummary[]>(() => {
		if (groupBy !== "customer") return [];
		const source =
			searchResults.length > 0 ||
				debouncedSearch.length >= 3 ||
				minAmountFilter ||
				maxAmountFilter ||
				startDateFilter ||
				endDateFilter
				? searchResults
				: sales;

		const map = new Map<string, CustomerSalesSummary>();
		const searchLower = debouncedSearch.toLowerCase().trim();

		for (const sale of source) {
			const name = (
				sale.customer_name ||
				sale.customerName ||
				"Walk-in Customer"
			).trim();
			if (searchLower && !name.toLowerCase().includes(searchLower)) {
				continue;
			}
			const existing = map.get(name) || {
				id: name,
				customer_name: name,
				totalOrders: 0,
				totalSpent: 0,
				lastPurchase: 0,
				averageOrderValue: 0,
			};
			existing.totalOrders += 1;
			existing.totalSpent += Number(sale.total_amount || sale.totalAmount || 0);
			const ts = Number(sale.timestamp) || Number(sale.updatedAt) || 0;
			existing.lastPurchase = Math.max(existing.lastPurchase, ts);
			map.set(name, existing);
		}

		const list = Array.from(map.values());
		for (const item of list) {
			item.averageOrderValue =
				item.totalOrders > 0 ? item.totalSpent / item.totalOrders : 0;
		}
		return list;
	}, [
		groupBy,
		searchResults,
		sales,
		debouncedSearch,
		minAmountFilter,
		maxAmountFilter,
		startDateFilter,
		endDateFilter,
	]);

	const sortedCustomers = useMemo<CustomerSalesSummary[]>(() => {
		if (groupBy !== "customer") return [];
		const list = [...customerGroups];
		if (!sortField) {
			return list.sort((a, b) => b.totalSpent - a.totalSpent);
		}
		return list.sort((a, b) => {
			const valA = a[sortField];
			const valB = b[sortField];
			if (typeof valA === "string" && typeof valB === "string") {
				return sortDirection === "asc"
					? valA.localeCompare(valB)
					: valB.localeCompare(valA);
			}
			const numA = Number(valA) || 0;
			const numB = Number(valB) || 0;
			return sortDirection === "asc" ? numA - numB : numB - numA;
		});
	}, [groupBy, customerGroups, sortField, sortDirection]);

	const pagedCustomers = useMemo(() => {
		if (groupBy !== "customer") return [];
		const start = (currentPage - 1) * pageSize;
		return sortedCustomers.slice(start, start + pageSize);
	}, [groupBy, sortedCustomers, currentPage, pageSize]);

	// Product Grouping Aggregation
	const productSummaries = useMemo<ProductSalesSummary[]>(() => {
		if (groupBy !== "product") return [];

		const sourceSales =
			searchResults.length > 0 ||
				minAmountFilter ||
				maxAmountFilter ||
				startDateFilter ||
				endDateFilter
				? searchResults
				: sales;

		const hasActiveFilter = !!(
			startDateFilter ||
			endDateFilter ||
			minAmountFilter ||
			maxAmountFilter
		);
		const allowedSaleIds = hasActiveFilter
			? new Set(sourceSales.map((s) => s.id))
			: null;

		const map = new Map<string, ProductSalesSummary>();
		const searchLower = debouncedSearch.toLowerCase().trim();

		for (const item of allStoreSaleItems) {
			if (allowedSaleIds && !allowedSaleIds.has(item.saleId)) {
				continue;
			}
			const itemName = item.itemName || "Unknown Item";
			if (searchLower && !itemName.toLowerCase().includes(searchLower)) {
				continue;
			}

			const key = item.itemId || itemName;
			const existing = map.get(key) || {
				id: key,
				itemName: itemName,
				totalQuantity: 0,
				totalRevenue: 0,
				totalProfit: 0,
				orderCount: 0,
			};

			const qty = Number(item.quantity) || 0;
			const sell = Number(item.sellPrice) || 0;
			const buy = Number(item.buyPrice) || 0;

			existing.totalQuantity += qty;
			existing.totalRevenue += qty * sell;
			existing.totalProfit += qty * (sell - buy);
			existing.orderCount += 1;
			map.set(key, existing);
		}

		return Array.from(map.values());
	}, [
		groupBy,
		allStoreSaleItems,
		searchResults,
		sales,
		minAmountFilter,
		maxAmountFilter,
		startDateFilter,
		endDateFilter,
		debouncedSearch,
	]);

	const sortedProducts = useMemo<ProductSalesSummary[]>(() => {
		if (groupBy !== "product") return [];
		const list = [...productSummaries];
		if (!sortField) {
			return list.sort((a, b) => b.totalRevenue - a.totalRevenue);
		}
		return list.sort((a, b) => {
			const valA = a[sortField];
			const valB = b[sortField];
			if (typeof valA === "string" && typeof valB === "string") {
				return sortDirection === "asc"
					? valA.localeCompare(valB)
					: valB.localeCompare(valA);
			}
			const numA = Number(valA) || 0;
			const numB = Number(valB) || 0;
			return sortDirection === "asc" ? numA - numB : numB - numA;
		});
	}, [groupBy, productSummaries, sortField, sortDirection]);

	const pagedProducts = useMemo(() => {
		if (groupBy !== "product") return [];
		const start = (currentPage - 1) * pageSize;
		return sortedProducts.slice(start, start + pageSize);
	}, [groupBy, sortedProducts, currentPage, pageSize]);

	const displayTotalItems =
		groupBy === "timeline"
			? totalItems
			: groupBy === "customer"
				? sortedCustomers.length
				: sortedProducts.length;
	// Loading state handled by isLoading from pagination

	const handleDelete = async (id: string) => {
		if (confirm("Are you sure you want to delete this sale?")) {
			try {
				await softDeleteSale(dataConnect, {
					id,
					updatedAt: Math.floor(Date.now() / 1000),
				});
				invalidateAllPages();
				setCurrentPage(1);
				setRefreshTrigger((prev) => prev + 1);
				try {
					await import("@/lib/sync-ping").then((m) =>
						m.pingDashboardStore(storeId as string),
					);
				} catch (_e) { }
				try {
					const actions = await import("@/app/actions");
					await (actions as { revalidateDashboard?: () => Promise<void> }).revalidateDashboard?.();
				} catch (_e) { }
			} catch (err) {
				console.error("Failed to delete sale:", err);
			}
		}
	};

	const formatDate = (timestamp: number) => {
		if (!timestamp) return "-";
		return new Date(timestamp).toLocaleDateString("en-IN", {
			day: "numeric",
			month: "short",
			year: "numeric",
			hour: "2-digit",
			minute: "2-digit",
		});
	};

	const generateInvoice = async (sale: SaleRow) => {
		await fetchSaleItems(sale.id);
		const items = saleItemsMap[sale.id] || [];

		const doc = new jsPDF();

		// Header
		doc.setFontSize(20);
		doc.text("StoreBook Invoice", 14, 22);

		doc.setFontSize(10);
		doc.setTextColor(100);
		doc.text(
			`Invoice ID: #INV-${(sale.cloud_id || sale.id).substring(0, 8)}`,
			14,
			30,
		);
		doc.text(
			`Date: ${formatDate((sale.timestamp as number) || (sale.updated_at as number))}`,
			14,
			35,
		);
		doc.text(
			`Customer: ${sale.customer_name || "Walk-in Customer"}`,
			14,
			40,
		);

		// Format amount for invoice with 2 decimals
		const displayAmount = Number(sale.total_amount || 0).toFixed(2);

		if (items.length > 0) {
			autoTable(doc, {
				startY: 50,
				head: [["Item", "Qty", "Price", "Total"]],
				body: items.map((item) => [
					item.itemName?.substring(0, 30),
					item.quantity || 0,
					`Rs. ${(Number(item.sellPrice) || 0).toFixed(2)}`,
					`Rs. ${(Number(item.quantity) * (Number(item.sellPrice) || 0)).toFixed(2)}`,
				]),
				theme: "striped",
				headStyles: { fillColor: [13, 148, 136] },
			});
		} else {
			autoTable(doc, {
				startY: 50,
				head: [["Description", "Amount"]],
				body: [[sale.notes || "Purchases", `Rs. ${displayAmount}`]],
				theme: "striped",
				headStyles: { fillColor: [13, 148, 136] },
			});
		}

		const finalY = (doc as any).lastAutoTable.finalY || 50;
		doc.setFontSize(12);
		doc.setTextColor(0);
		doc.text(`Total Amount: Rs. ${displayAmount}`, 14, finalY + 10);

		doc.setFontSize(10);
		doc.setTextColor(150);
		doc.text("Thank you for your business!", 14, finalY + 30);

		doc.save(`Invoice_${(sale.cloud_id || sale.id).substring(0, 8)}.pdf`);
	};

	return (
		<div className="space-y-6">
			<div className="flex justify-between items-center mb-6">
				<div>
					<h1 className="text-2xl font-bold text-gray-900 dark:text-white">
						Sales History
					</h1>
					<p className="text-gray-500 dark:text-gray-400 text-sm mt-1">
						View and manage all your past transactions.
					</p>
				</div>
				<div className="flex items-center space-x-3">
					<ExportButtons
						data={
							groupBy === "timeline"
								? (sales as Record<string, unknown>[])
								: groupBy === "customer"
									? (sortedCustomers as unknown as Record<string, unknown>[])
									: (sortedProducts as unknown as Record<string, unknown>[])
						}
						type={
							groupBy === "timeline"
								? "sales"
								: groupBy === "customer"
									? "sales_by_customer"
									: "sales_by_product"
						}
						columns={
							groupBy === "timeline"
								? [
									"timestamp",
									"customer_name",
									"total_amount",
									"notes",
								]
								: groupBy === "customer"
									? [
										"customer_name",
										"totalOrders",
										"averageOrderValue",
										"totalSpent",
										"lastPurchase",
									]
									: [
										"itemName",
										"totalQuantity",
										"orderCount",
										"totalRevenue",
										"totalProfit",
									]
						}
					/>
					<button
						onClick={() => setShowModal(true)}
						className="btn-primary flex items-center space-x-2"
					>
						<Plus size={18} />
						<span>New Sale</span>
					</button>
				</div>
			</div>

			<div className="glass-card overflow-hidden">
				<div className="p-4 border-b border-gray-100 dark:border-gray-800 bg-gray-50/50 dark:bg-gray-900/50">
					<div className="flex flex-col lg:flex-row gap-4 justify-between items-start lg:items-center w-full">
						<div className="flex flex-wrap items-center gap-3 w-full lg:w-auto">
							<div className="relative w-full sm:w-64 flex-shrink-0">
								<div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
									<Search size={16} className="text-gray-400" />
								</div>
								<input
									aria-label="text"
									type="text"
									value={searchQuery}
									onChange={(e) =>
										handleSearchChange(
											sanitizeInput(e.target.value),
										)
									}
									className="block w-full pl-10 pr-3 py-2 border border-gray-200 dark:border-gray-700 bg-white dark:bg-gray-800 rounded-lg text-sm placeholder-gray-400 dark:placeholder-gray-500 focus:outline-none focus:ring-2 focus:ring-teal-500 focus:border-transparent transition-all dark:text-gray-100"
									placeholder={
										groupBy === "timeline"
											? "Search sales by customer..."
											: groupBy === "customer"
												? "Search customer..."
												: "Search product name..."
									}
								/>
							</div>

							<div className="flex items-center gap-2 bg-white dark:bg-gray-800 border border-gray-200 dark:border-gray-700 rounded-lg px-3 py-2 text-sm shadow-sm">
								<span className="text-xs font-semibold text-gray-500 dark:text-gray-400 whitespace-nowrap flex items-center gap-1">
									{groupBy === "timeline" && <Clock size={14} className="text-teal-600 dark:text-teal-400" />}
									{groupBy === "customer" && <Users size={14} className="text-teal-600 dark:text-teal-400" />}
									{groupBy === "product" && <Package size={14} className="text-teal-600 dark:text-teal-400" />}
									Group By:
								</span>
								<select
									value={groupBy}
									onChange={(e) => {
										const val = e.target.value as GroupByOption;
										setGroupBy(val);
										setCurrentPage(1);
										if (val === "timeline") {
											setSortField("timestamp");
											setSortDirection("desc");
										} else {
											setSortField(null);
										}
									}}
									className="bg-transparent border-none text-xs font-bold text-teal-600 dark:text-teal-400 outline-none cursor-pointer focus:ring-0 pr-1"
								>
									<option value="timeline" className="text-gray-900 dark:text-white dark:bg-gray-800">
										Timeline (Recent First)
									</option>
									<option value="customer" className="text-gray-900 dark:text-white dark:bg-gray-800">
										Customers
									</option>
									<option value="product" className="text-gray-900 dark:text-white dark:bg-gray-800">
										Products
									</option>
								</select>
							</div>
						</div>

						<div className="flex flex-wrap items-center gap-3">
							<div className="flex items-center gap-2 bg-white dark:bg-gray-800 border border-gray-200 dark:border-gray-700 rounded-lg px-2 py-1">
								<span className="text-xs text-gray-500 dark:text-gray-400">
									Date:
								</span>
								<input
									type="date"
									value={startDateFilter}
									onChange={(e) => {
										setStartDateFilter(e.target.value);
										setCurrentPage(1);
									}}
									className="w-[110px] px-1 py-1 bg-transparent border-none text-xs text-gray-900 dark:text-white outline-none focus:ring-0"
								/>
								<span className="text-gray-300 dark:text-gray-600">
									-
								</span>
								<input
									type="date"
									value={endDateFilter}
									onChange={(e) => {
										setEndDateFilter(e.target.value);
										setCurrentPage(1);
									}}
									className="w-[110px] px-1 py-1 bg-transparent border-none text-xs text-gray-900 dark:text-white outline-none focus:ring-0"
								/>
							</div>

							<div className="flex items-center gap-2 bg-white dark:bg-gray-800 border border-gray-200 dark:border-gray-700 rounded-lg px-2 py-1">
								<span className="text-xs text-gray-500 dark:text-gray-400">
									Total:
								</span>
								<input
									type="number"
									placeholder="Min"
									value={minAmountFilter}
									onChange={(e) => {
										setMinAmountFilter(e.target.value);
										setCurrentPage(1);
									}}
									className="w-16 px-1 py-1 bg-transparent border-none text-xs text-gray-900 dark:text-white outline-none focus:ring-0"
								/>
								<span className="text-gray-300 dark:text-gray-600">
									-
								</span>
								<input
									type="number"
									placeholder="Max"
									value={maxAmountFilter}
									onChange={(e) => {
										setMaxAmountFilter(e.target.value);
										setCurrentPage(1);
									}}
									className="w-16 px-1 py-1 bg-transparent border-none text-xs text-gray-900 dark:text-white outline-none focus:ring-0"
								/>
							</div>
						</div>
					</div>
				</div>

				<div className="overflow-x-auto">
					{(() => {
						if (groupBy === "customer") {
							const customerColumns: TableColumn<CustomerSalesSummary>[] = [
								{
									key: "customer_name",
									label: "Customer",
									sortable: true,
									render: (value) => (
										<span className="font-semibold text-gray-900 dark:text-gray-100">
											{String(value) || "Walk-in Customer"}
										</span>
									),
								},
								{
									key: "totalOrders",
									label: "Invoices",
									sortable: true,
									textAlign: "center",
									render: (value) => (
										<span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-semibold bg-blue-50 dark:bg-blue-900/30 text-blue-700 dark:text-blue-300">
											{Number(value) || 0}
										</span>
									),
								},
								{
									key: "averageOrderValue",
									label: "Avg. Order Value",
									sortable: true,
									textAlign: "right",
									render: (value) => (
										<FormattedAmount amount={Number(value) || 0} />
									),
								},
								{
									key: "lastPurchase",
									label: "Last Purchase",
									sortable: true,
									textAlign: "center",
									render: (value) => formatDate(Number(value) || 0),
								},
								{
									key: "totalSpent",
									label: "Total Spent",
									sortable: true,
									textAlign: "right",
									className:
										"font-bold text-gray-900 dark:text-gray-100",
									render: (value) => (
										<FormattedAmount amount={Number(value) || 0} />
									),
								},
							];

							return (
								<DynamicTable<CustomerSalesSummary>
									columns={customerColumns}
									rows={pagedCustomers}
									isLoading={isLoading}
									emptyMessage="No customer sales found matching your criteria."
									rowKey="id"
									sortField={sortField || ""}
									sortDirection={sortDirection}
									onSort={handleSort}
								/>
							);
						}

						if (groupBy === "product") {
							const productColumns: TableColumn<ProductSalesSummary>[] = [
								{
									key: "itemName",
									label: "Product Name",
									sortable: true,
									render: (value) => (
										<span className="font-semibold text-gray-900 dark:text-gray-100">
											{String(value) || "Unknown Item"}
										</span>
									),
								},
								{
									key: "totalQuantity",
									label: "Units Sold",
									sortable: true,
									textAlign: "center",
									render: (value) => (
										<span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-semibold bg-teal-50 dark:bg-teal-900/30 text-teal-700 dark:text-teal-300">
											{Number(value) || 0}
										</span>
									),
								},
								{
									key: "orderCount",
									label: "Invoices Included",
									sortable: true,
									textAlign: "center",
									render: (value) => (
										<span className="text-gray-600 dark:text-gray-400">
											{Number(value) || 0}
										</span>
									),
								},
								{
									key: "totalProfit",
									label: "Est. Gross Profit",
									sortable: true,
									textAlign: "right",
									render: (value) => {
										const profit = Number(value) || 0;
										return (
											<span
												className={`font-semibold ${profit >= 0
													? "text-green-600 dark:text-green-400"
													: "text-red-600 dark:text-red-400"
													}`}
											>
												<FormattedAmount amount={profit} />
											</span>
										);
									},
								},
								{
									key: "totalRevenue",
									label: "Total Revenue",
									sortable: true,
									textAlign: "right",
									className:
										"font-bold text-gray-900 dark:text-gray-100",
									render: (value) => (
										<FormattedAmount amount={Number(value) || 0} />
									),
								},
							];

							return (
								<DynamicTable<ProductSalesSummary>
									columns={productColumns}
									rows={pagedProducts}
									isLoading={isLoading || isLoadingSaleItems}
									emptyMessage="No product sales found matching your criteria."
									rowKey="id"
									sortField={sortField || ""}
									sortDirection={sortDirection}
									onSort={handleSort}
								/>
							);
						}

						const columns: TableColumn[] = [
							{
								key: "id",
								label: "Invoice ID",
								render: (value, row) => (
									<span className="font-medium text-teal-600 dark:text-teal-400">
										#
										{`INV-${String(row.cloud_id || value).substring(0, 8)}`}
									</span>
								),
							},
							{
								key: "timestamp",
								label: "Date & Time",
								sortable: true,
								render: (value, row) =>
									formatDate(Number(value) || Number(row.updated_at)),
							},
							{
								key: "customer_name",
								label: "Customer",
								sortable: true,
								render: (value) => (
									<span className="font-medium text-gray-900 dark:text-gray-100">
										{String(value) || "Walk-in Customer"}
									</span>
								),
							},
							{
								key: "notes",
								label: "Notes",
								textAlign: "right",
								className:
									"text-gray-500 dark:text-gray-400 truncate max-w-[150px]",
								render: (value) => String(value) || "-",
							},
							{
								key: "total_amount",
								label: "Total Amount",
								sortable: true,
								textAlign: "right",
								className:
									"font-bold text-gray-900 dark:text-gray-100",
								render: (value) => (
									<FormattedAmount amount={Number(value) || 0} />
								),
							},
						];

						const rowActions: TableRowAction[] = [
							{
								icon: <Download size={16} />,
								onClick: (s) => generateInvoice(s as SaleRow),
								className:
									"text-teal-600 hover:text-teal-800 transition-colors",
								title: "Download Invoice",
							},
							...(canDeleteRecords
								? [
									{
										icon: <Trash2 size={16} />,
										onClick: (sale: Record<string, unknown>) => handleDelete(String(sale.id)),
										className:
											"text-red-500 hover:text-red-700 transition-colors",
										title: "Delete Sale",
									},
								]
								: []),
						];

						return (
							<DynamicTable
								columns={columns}
								rows={sales}
								isLoading={isLoading}
								emptyMessage="No sales found matching your search."
								rowKey="id"
								rowActions={rowActions}
								onRowClick={handleRowClick}
								sortField={sortField || ""}
								sortDirection={sortDirection}
								onSort={handleSort}
							/>
						);
					})()}
				</div>
				<Pagination
					currentPage={currentPage}
					pageSize={pageSize}
					totalItems={displayTotalItems}
					isLoading={isLoading || (groupBy === "product" && isLoadingSaleItems)}
					onPageChange={handlePageChange}
				/>
			</div>

			{showModal && storeId && (
				<SalesPOS
					storeId={storeId}
					maxDiscountPercent={maxDiscountPercent}
					onClose={() => setShowModal(false)}
					onSuccess={() => {
						setShowModal(false);
						invalidateAllPages();
						setCurrentPage(1);
						setRefreshTrigger((prev) => prev + 1);
					}}
				/>
			)}

			{selectedSale && (
				<div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/60 backdrop-blur-sm">
					<div className="bg-white dark:bg-gray-800 rounded-xl max-w-2xl w-full shadow-2xl border border-gray-200 dark:border-gray-700 overflow-hidden flex flex-col max-h-[90vh]">
						{/* Modal Header */}
						<div className="px-6 py-4 border-b border-gray-100 dark:border-gray-700 bg-gray-50/50 dark:bg-gray-900/50 flex items-center justify-between">
							<div>
								<h3 className="text-lg font-bold text-gray-900 dark:text-white">
									Sale Details
								</h3>
								<p className="text-xs text-gray-500 dark:text-gray-400 mt-1">
									Customer: <span className="font-semibold text-gray-800 dark:text-gray-200">{selectedSale.customerName || 'Walk-in Customer'}</span> | Date: {new Date(selectedSale.timestamp).toLocaleString()}
								</p>
							</div>
							<button
								type="button"
								onClick={() => {
									setSelectedSale(null);
									setSelectedSaleItems([]);
								}}
								className="text-gray-400 hover:text-gray-600 dark:hover:text-gray-200 transition-colors p-1"
							>
								<X size={20} />
							</button>
						</div>

						{/* Modal Content */}
						<div className="flex-1 overflow-y-auto p-6">
							{isLoadingDetails ? (
								<div className="flex items-center justify-center py-12">
									<Loader2 className="w-8 h-8 text-teal-500 animate-spin" />
								</div>
							) : selectedSaleItems.length === 0 ? (
								<p className="text-sm text-gray-500 dark:text-gray-400 text-center py-6">
									No items found in this sale.
								</p>
							) : (
								<div className="overflow-x-auto">
									<table className="w-full text-left text-sm border-collapse">
										<thead>
											<tr className="border-b border-gray-100 dark:border-gray-700 text-xs font-semibold uppercase text-gray-500 dark:text-gray-400">
												<th className="pb-3 text-left">Item Name</th>
												<th className="pb-3 text-center">Qty</th>
												<th className="pb-3 text-right">Sell Price</th>
												<th className="pb-3 text-right">Purchase Price</th>
												<th className="pb-3 text-right">Profit</th>
												<th className="pb-3 text-right">Total</th>
											</tr>
										</thead>
										<tbody className="divide-y divide-gray-100 dark:divide-gray-700">
											{selectedSaleItems.map((item) => {
												const itemSubtotal = item.sellPrice * item.quantity;
												const profit = (item.sellPrice - item.buyPrice) * item.quantity;

												return (
													<tr key={item.id} className="text-gray-700 dark:text-gray-300">
														<td className="py-3 font-medium text-gray-900 dark:text-white">
															{item.itemName}
														</td>
														<td className="py-3 text-center">{item.quantity}</td>
														<td className="py-3 text-right">
															<FormattedAmount amount={item.sellPrice} />
														</td>
														<td className="py-3 text-right">
															<span className="font-medium text-teal-600 dark:text-teal-400 bg-teal-50 dark:bg-teal-950/20 px-2 py-0.5 rounded-md">
																<FormattedAmount amount={item.buyPrice} />
															</span>
														</td>
														<td className={`py-3 text-right font-semibold ${profit >= 0 ? 'text-green-600 dark:text-green-400' : 'text-red-600 dark:text-red-400'}`}>
															<FormattedAmount amount={profit} />
														</td>
														<td className="py-3 text-right font-bold text-gray-900 dark:text-white">
															<FormattedAmount amount={itemSubtotal} />
														</td>
													</tr>
												);
											})}
										</tbody>
									</table>
								</div>
							)}
						</div>

						{/* Modal Footer */}
						<div className="px-6 py-4 border-t border-gray-100 dark:border-gray-700 bg-gray-50/50 dark:bg-gray-900/50 flex flex-col items-end gap-1">
							<div className="text-sm text-gray-600 dark:text-gray-400 flex justify-between w-full max-w-xs">
								<span>Discount:</span>
								<span className="font-medium text-red-500">
									-<FormattedAmount amount={selectedSale.discountAmount || 0} />
								</span>
							</div>
							<div className="text-lg font-bold text-gray-900 dark:text-white flex justify-between w-full max-w-xs border-t border-gray-200 dark:border-gray-600 pt-2 mt-1">
								<span>Total Paid:</span>
								<span>
									<FormattedAmount amount={selectedSale.totalAmount || 0} />
								</span>
							</div>
							<button
								type="button"
								onClick={() => {
									setSelectedSale(null);
									setSelectedSaleItems([]);
								}}
								className="mt-4 px-4 py-2 text-sm font-medium text-gray-600 dark:text-gray-300 bg-gray-100 hover:bg-gray-200 dark:bg-gray-700 dark:hover:bg-gray-600 rounded-lg transition-colors w-full sm:w-auto"
							>
								Close
							</button>
						</div>
					</div>
				</div>
			)}
		</div>
	);
}
