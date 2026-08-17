import { sanitizeInput } from "@/lib/sanitize";
import React, { useEffect, useState } from "react";
import { dataConnect } from "@/lib/firebase";
import { getActiveSuppliers, syncSupplier } from "@/dataconnect";

type SupplierOption = {
    id: string;
    name: string;
};

type RestockQuantityProps = {
    readonly open: boolean;
    readonly item: Record<string, any> | null;
    readonly canAccessCost?: boolean;
    readonly storeId?: string;
    readonly onClose: () => void;
    readonly onConfirm?: (payload: {
        quantity: number;
        buyPrice: number;
        supplierId: string;
        supplierName: string;
        batchNumber: string;
        expiryDate: string;
    }) => Promise<void> | void;
};

const PRESET_AMOUNTS = [5, 10, 25, 50];

const CASH_SUPPLIER_ID = "068cdba2-f58e-4ab8-a596-13806e4cb18e";

function RestockQuantity({
    open,
    item,
    canAccessCost = true,
    storeId,
    onClose,
    onConfirm,
}: Readonly<RestockQuantityProps>) {
    const [quantity, setQuantity] = useState("");
    const [buyPrice, setBuyPrice] = useState("");
    const [supplierSearch, setSupplierSearch] = useState("");
    const [selectedSupplier, setSelectedSupplier] =
        useState<SupplierOption | null>(null);
    const [batchNumber, setBatchNumber] = useState("");
    const [expiryDate, setExpiryDate] = useState("");
    const [showSupplierDropdown, setShowSupplierDropdown] = useState(false);
    const [suppliers, setSuppliers] = useState<SupplierOption[]>([]);
    const [isCreatingSupplier, setIsCreatingSupplier] = useState(false);
    const [error, setError] = useState("");
    const [isSubmitting, setIsSubmitting] = useState(false);

    useEffect(() => {
        if (!open || !storeId) {
            // eslint-disable-next-line react-hooks/set-state-in-effect
            setSuppliers([]);
            return;
        }

        let isMounted = true;

        const fetchSuppliers = async () => {
            try {
                const res = await getActiveSuppliers(dataConnect, { storeId });
                if (!isMounted) return;

                const nextSuppliers = res.data.suppliers
                    .map((doc: any) => {
                        const rawName = doc.name;
                        return {
                            id: doc.id,
                            name:
                                typeof rawName === "string"
                                    ? rawName.trim()
                                    : "",
                        };
                    })
                    .filter(
                        (supplier): supplier is SupplierOption =>
                            supplier.name.length > 0,
                    );

                setSuppliers(
                    nextSuppliers.sort((a, b) => a.name.localeCompare(b.name)),
                );
            } catch (err) {
                console.error("Failed to fetch suppliers:", err);
            }
        };

        fetchSuppliers();
        return () => {
            isMounted = false;
        };
    }, [open, storeId]);

    useEffect(() => {
        if (!open) return;

        // eslint-disable-next-line react-hooks/set-state-in-effect
        setQuantity("");
        setBuyPrice(item?.buy_price?.toString() || "");
        setSupplierSearch("");
        setSelectedSupplier(null);
        setShowSupplierDropdown(false);
        setBatchNumber("");
        setExpiryDate("");
        setError("");
        setIsSubmitting(false);
    }, [open, item]);

    const unitLabel = item?.unit || "pcs";
    const presets =
        unitLabel === "pcs" ||
        unitLabel === "dozen" ||
        unitLabel === "box" ||
        unitLabel === "packet"
            ? PRESET_AMOUNTS
            : [5, 10, 25, 50];

    const filteredSuppliers = (() => {
        const query = supplierSearch.trim().toLowerCase();
        if (!query) return suppliers;
        return suppliers.filter((supplier) =>
            supplier.name.toLowerCase().includes(query),
        );
    })();

    if (!open || !item) return null;

    const handlePreset = (amount: number) => {
        const currentValue = Number(quantity || 0);
        const nextValue = currentValue + amount;
        setQuantity(
            Number.isInteger(nextValue)
                ? String(nextValue)
                : nextValue.toString(),
        );
    };

    const handleCreateSupplier = async () => {
        const trimmedName = supplierSearch.trim();
        if (!trimmedName || !storeId || isCreatingSupplier) return;

        setIsCreatingSupplier(true);
        try {
            const newId = crypto.randomUUID();
            await syncSupplier(dataConnect, {
                id: newId,
                storeId,
                name: trimmedName,
                phone: "",
                gstin: "",
                address: "",
                isDeleted: false,
                updatedAt: Math.floor(Date.now() / 1000),
            });

            setSelectedSupplier({ id: newId, name: trimmedName });
            setSupplierSearch(trimmedName);
            setShowSupplierDropdown(false);
        } catch (err) {
            console.error("Create supplier failed", err);
            setError("Unable to create supplier.");
        } finally {
            setIsCreatingSupplier(false);
        }
    };

    const handleSubmit = async (event: React.FormEvent) => {
        event.preventDefault();
        setError("");

        const parsedQuantity = Number(quantity);
        if (!Number.isFinite(parsedQuantity) || parsedQuantity <= 0) {
            setError("Please enter a valid positive quantity.");
            return;
        }

        if (canAccessCost) {
            const parsedBuyPrice = Number(buyPrice);
            if (!Number.isFinite(parsedBuyPrice) || parsedBuyPrice < 0) {
                setError("Please enter a valid buy price.");
                return;
            }
        }
        setIsSubmitting(true);
        try {
            await onConfirm?.({
                quantity: parsedQuantity,
                buyPrice: canAccessCost
                    ? Number(buyPrice || 0)
                    : Number(item?.buy_price || 0),
                supplierId: selectedSupplier?.id || CASH_SUPPLIER_ID,
                supplierName: selectedSupplier?.name || "Cash / Anonymous",
                batchNumber: batchNumber.trim(),
                expiryDate: expiryDate,
            });
            onClose();
        } catch (err) {
            console.error("Restock failed", err);
            setError("Unable to restock item right now.");
        } finally {
            setIsSubmitting(false);
        }
    };

    return (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 backdrop-blur-sm px-10">
            <div className="bg-white dark:bg-gray-900 rounded-xl shadow-xl w-full max-w-lg p-6">
                <div className="flex items-start justify-between gap-3 mb-4">
                    <div>
                        <h2 className="text-xl font-bold dark:text-white">
                            Restock Stock
                        </h2>
                        <p className="text-sm text-gray-500 dark:text-gray-400 mt-1">
                            Add stock for {item?.name || "this item"}
                        </p>
                    </div>
                    <button
                        type="button"
                        onClick={onClose}
                        className="text-gray-500 hover:text-gray-700 dark:text-gray-400 dark:hover:text-gray-200"
                    >
                        ✕
                    </button>
                </div>

                <form onSubmit={handleSubmit} className="space-y-4">
                    <div className="rounded-lg border border-gray-200 dark:border-gray-700 bg-gray-50/70 dark:bg-gray-800/60 p-3">
                        <p className="text-sm text-gray-500 dark:text-gray-400">
                            Current stock
                        </p>
                        <p className="text-lg font-semibold text-gray-900 dark:text-white">
                            {Number(item?.quantity || 0)} {unitLabel}
                        </p>
                    </div>

                    <div>
                        <label
                            htmlFor="restock-quantity"
                            className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1"
                        >
                            Add Quantity
                        </label>
                        <input
                            id="restock-quantity"
                            type="number"
                            min="0"
                            step="any"
                            value={quantity}
                            onChange={(event) =>
                                setQuantity(sanitizeInput(event.target.value))
                            }
                            placeholder="Enter quantity"
                            className="w-full rounded-lg border border-gray-300 dark:border-gray-700 bg-white dark:bg-gray-800 px-3 py-2 text-gray-900 dark:text-white outline-none focus:ring-2 focus:ring-teal-500"
                        />
                    </div>

                    <div className="flex flex-wrap gap-2">
                        {presets.map((amount) => (
                            <button
                                key={amount}
                                type="button"
                                onClick={() => handlePreset(amount)}
                                className="rounded-full border border-teal-500 px-3 py-1 text-sm font-medium text-teal-600 hover:bg-teal-50 dark:border-teal-400 dark:text-teal-300 dark:hover:bg-teal-900/30"
                            >
                                +{amount}
                            </button>
                        ))}
                    </div>

                    {canAccessCost && (
                        <div>
                            <label
                                htmlFor="restock-buy-price"
                                className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1"
                            >
                                Buy Price (per {unitLabel})
                            </label>
                            <input
                                id="restock-buy-price"
                                type="number"
                                min="0"
                                step="0.01"
                                value={buyPrice}
                                onChange={(event) =>
                                    setBuyPrice(
                                        sanitizeInput(event.target.value),
                                    )
                                }
                                placeholder="0.00"
                                className="w-full rounded-lg border border-gray-300 dark:border-gray-700 bg-white dark:bg-gray-800 px-3 py-2 text-gray-900 dark:text-white outline-none focus:ring-2 focus:ring-teal-500"
                            />
                        </div>
                    )}

                    <div className="relative">
                        <label
                            htmlFor="restock-supplier"
                            className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1"
                        >
                            Supplier
                        </label>
                        <button
                            id="restock-supplier"
                            type="button"
                            onClick={() =>
                                setShowSupplierDropdown((value) => !value)
                            }
                            className="flex w-full items-center justify-between rounded-lg border border-gray-300 bg-white px-3 py-2 text-left text-sm text-gray-900 dark:border-gray-700 dark:bg-gray-800 dark:text-white"
                        >
                            <span>
                                {selectedSupplier
                                    ? selectedSupplier.name
                                    : "Select supplier"}
                            </span>
                            <span className="text-gray-500 dark:text-gray-400">
                                ▼
                            </span>
                        </button>

                        {showSupplierDropdown && (
                            <div className="absolute z-10 mt-1 w-full rounded-lg border border-gray-200 bg-white shadow-lg dark:border-gray-700 dark:bg-gray-800">
                                <div className="border-b border-gray-100 px-3 py-2 dark:border-gray-700">
                                    <input
                                        aria-label="text"
                                        type="text"
                                        value={supplierSearch}
                                        onChange={(event) =>
                                            setSupplierSearch(
                                                sanitizeInput(
                                                    event.target.value,
                                                ),
                                            )
                                        }
                                        placeholder="Search supplier..."
                                        className="w-full rounded-md border border-gray-200 bg-gray-50 px-2 py-2 text-sm text-gray-900 outline-none focus:border-teal-500 dark:border-gray-700 dark:bg-gray-900 dark:text-white"
                                    />
                                </div>

                                <button
                                    type="button"
                                    onClick={() => {
                                        setSelectedSupplier(null);
                                        setSupplierSearch("");
                                        setShowSupplierDropdown(false);
                                    }}
                                    className="flex w-full items-center px-3 py-2 text-left text-sm text-gray-700 hover:bg-gray-100 dark:text-gray-200 dark:hover:bg-gray-700"
                                >
                                    Cash Purchase / No Supplier
                                </button>

                                {filteredSuppliers.map((supplier) => (
                                    <button
                                        key={supplier.id || supplier.name}
                                        type="button"
                                        onClick={() => {
                                            setSelectedSupplier(supplier);
                                            setSupplierSearch(supplier.name);
                                            setShowSupplierDropdown(false);
                                        }}
                                        className="flex w-full items-center px-3 py-2 text-left text-sm text-gray-700 hover:bg-gray-100 dark:text-gray-200 dark:hover:bg-gray-700"
                                    >
                                        {supplier.name}
                                    </button>
                                ))}

                                {supplierSearch.trim() &&
                                    !filteredSuppliers.some(
                                        (supplier) =>
                                            supplier.name.toLowerCase() ===
                                            supplierSearch.trim().toLowerCase(),
                                    ) && (
                                        <button
                                            type="button"
                                            onClick={handleCreateSupplier}
                                            disabled={isCreatingSupplier}
                                            className="flex w-full items-center px-3 py-2 text-left text-sm font-medium text-teal-600 hover:bg-teal-50 disabled:cursor-not-allowed disabled:opacity-70 dark:text-teal-300 dark:hover:bg-teal-900/30"
                                        >
                                            {isCreatingSupplier
                                                ? "Creating..."
                                                : `Create supplier: "${supplierSearch.trim()}"`}
                                        </button>
                                    )}
                            </div>
                        )}
                    </div>

                    <div>
                        <label
                            htmlFor="restock-batch"
                            className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1"
                        >
                            Batch/Lot Number (Optional)
                        </label>
                        <input
                            id="restock-batch"
                            type="text"
                            value={batchNumber}
                            onChange={(event) =>
                                setBatchNumber(
                                    sanitizeInput(event.target.value),
                                )
                            }
                            placeholder="e.g. BATCH-001"
                            className="w-full rounded-lg border border-gray-300 dark:border-gray-700 bg-white dark:bg-gray-800 px-3 py-2 text-gray-900 dark:text-white outline-none focus:ring-2 focus:ring-teal-500"
                        />
                    </div>

                    <div>
                        <label
                            htmlFor="restock-expiry"
                            className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1"
                        >
                            Expiry Date (Optional)
                        </label>
                        <input
                            id="restock-expiry"
                            type="date"
                            value={expiryDate}
                            onChange={(event) =>
                                setExpiryDate(sanitizeInput(event.target.value))
                            }
                            className="w-full rounded-lg border border-gray-300 dark:border-gray-700 bg-white dark:bg-gray-800 px-3 py-2 text-gray-900 dark:text-white outline-none focus:ring-2 focus:ring-teal-500"
                        />
                    </div>

                    {error ? (
                        <p className="text-sm text-red-600 dark:text-red-400">
                            {error}
                        </p>
                    ) : null}

                    <div className="flex justify-end gap-3 pt-2">
                        <button
                            type="button"
                            onClick={onClose}
                            className="rounded-lg px-4 py-2 text-sm font-medium text-gray-600 hover:text-gray-900 dark:text-gray-300 dark:hover:text-white"
                        >
                            Cancel
                        </button>
                        <button
                            type="submit"
                            disabled={isSubmitting}
                            className="rounded-lg bg-teal-600 px-4 py-2 text-sm font-medium text-white hover:bg-teal-700 disabled:cursor-not-allowed disabled:opacity-70"
                        >
                            {isSubmitting ? "Saving..." : "Add Stock"}
                        </button>
                    </div>
                </form>
            </div>
        </div>
    );
}

export default RestockQuantity;
