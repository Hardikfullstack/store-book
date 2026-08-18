"use client";

import { useState, useEffect, useRef } from "react";
import {
	Plus,
	Search,
	Calendar,
	Trash2,
	Edit2,
	Loader2,
	ArrowDownCircle,
	Download,
	ChevronDown,
	ChevronUp,
	X,
} from "lucide-react";
import { fetchMoreData } from "@/app/actions";
import Pagination from "@/app/components/Pagination";
import jsPDF from "jspdf";
import autoTable from "jspdf-autotable";
import ExportButtons from "@/app/ExportButtons";
import { dataConnect } from "@/lib/firebase";
import { executeQuery } from "firebase/data-connect";
import { sanitizeInput } from "@/lib/sanitize";
import {
	getActiveSalesRef,
	syncSale,
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

export default function SalesClient({
	initialSales,
	maxDiscountPercent = 100,
	canDeleteRecords = true,
	storeId,
	isPremium,
}: {
	initialSales: any[];
	maxDiscountPercent?: number;
	canDeleteRecords?: boolean;
	storeId?: string;
	isPremium?: boolean;
}) {
	const [sales, setSales] = useState(initialSales);
	const [refreshTrigger, setRefreshTrigger] = useState(0);
	const [sortField, setSortField] = useState<string | null>(null);
	const [sortDirection, setSortDirection] = useState<"asc" | "desc">("asc");
	const [dataVersion, setDataVersion] = useState(0);
	const fetchedPagesAtVersionRef = useRef<Map<string, number>>(new Map());
	const [showModal, setShowModal] = useState(false);
	const [debouncedSearch, setDebouncedSearch] = useState("");
	const [searchResults, setSearchResults] = useState<any[]>([]);
	const [searchQuery, setSearchQuery] = useState("");
	const [minAmountFilter, setMinAmountFilter] = useState("");
	const [maxAmountFilter, setMaxAmountFilter] = useState("");
	const [startDateFilter, setStartDateFilter] = useState("");
	const [endDateFilter, setEndDateFilter] = useState("");
	const [selectedSale, setSelectedSale] = useState<any | null>(null);
	const [selectedSaleItems, setSelectedSaleItems] = useState<any[]>([]);
	const [isLoadingDetails, setIsLoadingDetails] = useState(false);
	const debounceRef = useRef<ReturnType<typeof setTimeout> | null>(null);
	const searchResultsKeyRef = useRef("");

	const handleRowClick = async (sale: any) => {
		setSelectedSale(sale);
		setIsLoadingDetails(true);
		try {
			const resp: any = await getSaleItemsBySaleId(dataConnect, { saleId: sale.id });
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
		if (!sortField) return {};
		const dir =
			direction === "asc" ? OrderDirection.ASC : OrderDirection.DESC;
		return {
			orderByTimestamp: sortField === "timestamp" ? dir : undefined,
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

	// Fetch total count — skip when searching or filtering
	useEffect(() => {
		if (
			!isPremium ||
			!storeId ||
			debouncedSearch ||
			minAmountFilter ||
			maxAmountFilter ||
			startDateFilter ||
			endDateFilter
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
				const needsFullFetch = isSearching || isFiltering;
				const currentSearchKey = `${debouncedSearch}-${sortField}-${sortDirection}-${minAmountFilter}-${maxAmountFilter}-${startDateFilter}-${endDateFilter}`;

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

				const vars: any = {
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
					if (isSearching) vars.searchTerm = debouncedSearch;
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

				const updated = response.data.sales.map((sale: any) => ({
					...sale,
					is_deleted: 0,
					updated_at: sale.updatedAt || Date.now(),
					customer_name: sale.customerName,
					total_amount: sale.totalAmount,
				}));

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
	]);
	const [expandedSaleId, setExpandedSaleId] = useState<string | null>(null);
	const [saleItemsMap, setSaleItemsMap] = useState<Record<string, any[]>>({});

	const fetchSaleItems = async (saleId: string) => {
		if (saleItemsMap[saleId] !== undefined) return;
		try {
			if (!storeId) return;
			const resp = await executeQuery(
				getActiveSaleItemsRef(dataConnect, { storeId: storeId }),
			);
			const allItems: any[] = resp.data?.saleItemDetails || [];
			const saleItems = allItems.filter((i) => i.saleId === saleId);
			setSaleItemsMap((prev) => ({ ...prev, [saleId]: saleItems }));
		} catch (e) {
			console.error("Failed to fetch sale items:", e);
		}
	};

	const toggleRowExpand = async (sale: any) => {
		if (expandedSaleId === sale.id) {
			setExpandedSaleId(null);
		} else {
			await fetchSaleItems(sale.id);
			setExpandedSaleId(sale.id);
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
		const isFiltering =
			!!minAmountFilter ||
			!!maxAmountFilter ||
			!!startDateFilter ||
			!!endDateFilter;
		if ((debouncedSearch || isFiltering) && searchResults.length > 0) {
			const start = (page - 1) * pageSize;
			setSales(searchResults.slice(start, start + pageSize));
		}
	};
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
				} catch (_) { }
				try {
					await import("@/app/actions").then(
						(m) => m.revalidateDashboard() as any,
					);
				} catch (_) { }
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

	const generateInvoice = async (sale: any) => {
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
			`Date: ${formatDate(sale.timestamp || sale.updated_at)}`,
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
						data={sales}
						type="sales"
						columns={[
							"timestamp",
							"customer_name",
							"total_amount",
							"notes",
						]}
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
					<div className="flex flex-col md:flex-row gap-4 justify-between items-start md:items-center w-full">
						<div className="relative w-full md:w-64 flex-shrink-0">
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
								placeholder="Search sales by customer..."
							/>
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
						const columns: TableColumn[] = [
							{
								key: "id",
								label: "Invoice ID",
								render: (value, row) => (
									<span className="font-medium text-teal-600 dark:text-teal-400">
										#
										{`INV-${(row.cloud_id || value).substring(0, 8)}`}
									</span>
								),
							},
							{
								key: "timestamp",
								label: "Date & Time",
								sortable: true,
								render: (value, row) =>
									formatDate(value || row.updated_at),
							},
							{
								key: "customer_name",
								label: "Customer",
								sortable: true,
								render: (value) => (
									<span className="font-medium text-gray-900 dark:text-gray-100">
										{value || "Walk-in Customer"}
									</span>
								),
							},
							{
								key: "notes",
								label: "Notes",
								textAlign: "right",
								className:
									"text-gray-500 dark:text-gray-400 truncate max-w-[150px]",
								render: (value) => value || "-",
							},
							{
								key: "total_amount",
								label: "Total Amount",
								sortable: true,
								textAlign: "right",
								className:
									"font-bold text-gray-900 dark:text-gray-100",
								render: (value) => (
									<FormattedAmount amount={value || 0} />
								),
							},
						];

						const rowActions: TableRowAction[] = [
							{
								icon: <Download size={16} />,
								onClick: (sale: any) => generateInvoice(sale),
								className:
									"text-teal-600 hover:text-teal-800 transition-colors",
								title: "Download Invoice",
							},
							...(canDeleteRecords
								? [
									{
										icon: <Trash2 size={16} />,
										onClick: (sale: any) => handleDelete(sale.id),
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
					totalItems={totalItems}
					isLoading={isLoading}
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
