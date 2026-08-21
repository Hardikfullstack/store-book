"use client";

import { useEffect, useState } from "react";
import {
    Users,
    UserPlus,
    Trash2,
    Loader2,
    ShieldCheck,
    KeyRound,
    CheckCircle,
    AlertCircle,
    XCircle,
} from "lucide-react";
import { sanitizeInput } from "@/lib/sanitize";
import { ROLE_LABELS } from "@/lib/roleMatrix";
import {
    createStaffAccount,
    updateStaffRole,
    deleteStaffAccount,
    resetStaffPassword,
    getStaffByStore,
} from "@/app/actions";

type StaffMember = {
    id: string;
    username: string;
    role: string;
    createdAt: number;
};

export default function StaffManagement({ storeId }: { storeId: string }) {
    // Form state
    const [username, setUsername] = useState("");
    const [pin, setPin] = useState("");
    const [newRole, setNewRole] = useState<"cashier" | "manager">("cashier");
    const [canViewProfit, setCanViewProfit] = useState(false);
    const [canDelete, setCanDelete] = useState(false);
    const [isCreating, setIsCreating] = useState(false);

    // List state
    const [staff, setStaff] = useState<StaffMember[]>([]);
    const [loadingList, setLoadingList] = useState(true);
    const [listError, setListError] = useState("");

    // Action state
    const [message, setMessage] = useState("");
    const [error, setError] = useState("");
    const [actionUid, setActionUid] = useState<string | null>(null);
    const [activeAction, setActiveAction] = useState<
        "delete" | "resetPassword" | null
    >(null);

    // Inline new password for reset + modal state
    const [newPassword, setNewPassword] = useState("");
    const [resetTarget, setResetTarget] = useState<string | null>(null);
    const [resetConfirm, setResetConfirm] = useState("");

    useEffect(() => {
        let cancelled = false;
        (async () => {
            setLoadingList(true);
            setListError("");
            try {
                const res = await getStaffByStore(storeId);
                if (!cancelled) {
                if (res.success && res.data) {
                    setStaff(res.data as StaffMember[]);
                    } else {
                        setListError(res.error || "Failed to load staff");
                    }
                }
            } catch {
                if (!cancelled) setListError("Failed to load staff");
            } finally {
                if (!cancelled) setLoadingList(false);
            }
        })();
        return () => {
            cancelled = true;
        };
    }, [storeId]);

    const reloadStaff = async () => {
        setLoadingList(true);
        setListError("");
        try {
            const res = await getStaffByStore(storeId);
            if (res.success && res.data) {
                setStaff(res.data as StaffMember[]);
            } else {
                setListError(res.error || "Failed to load staff");
            }
        } catch {
            setListError("Failed to load staff");
        } finally {
            setLoadingList(false);
        }
    };

    const handleCreate = async (e: React.FormEvent) => {
        e.preventDefault();
        if (pin.length < 4) {
            setError("PIN must be at least 4 characters");
            return;
        }

        setIsCreating(true);
        setMessage("");
        setError("");

        try {
            const res = await createStaffAccount(username, pin, {
                role: newRole,
                canViewProfit,
                canDelete,
            });
            if (res.success) {
                setMessage(`Staff account "${username}" created successfully!`);
                setUsername("");
                setPin("");
                setNewRole("cashier");
                setCanViewProfit(false);
                setCanDelete(false);
                reloadStaff();
            } else {
                setError(res.error || "Failed to create account");
            }
        } catch (err: unknown) {
            setError(err instanceof Error ? err.message : "An error occurred");
        } finally {
            setIsCreating(false);
        }
    };

    const handleRoleChange = async (
        uid: string,
        role: "cashier" | "manager",
    ) => {
        setMessage("");
        setError("");
        try {
            const res = await updateStaffRole(uid, role);
            if (res.success) {
                setMessage("Role updated successfully.");
                reloadStaff();
            } else {
                setError(res.error || "Failed to update role");
            }
        } catch (err: unknown) {
            setError(err instanceof Error ? err.message : "An error occurred");
        }
    };

    const handleDelete = async (uid: string) => {
        if (
            !confirm(
                "Permanently delete this staff account? This cannot be undone.",
            )
        ) {
            return;
        }
        setActionUid(uid);
        setActiveAction("delete");
        setMessage("");
        setError("");
        try {
            const res = await deleteStaffAccount(uid);
            if (res.success) {
                setMessage("Staff account deleted.");
                reloadStaff();
            } else {
                setError(res.error || "Failed to remove staff");
            }
        } catch (err: unknown) {
            setError(err instanceof Error ? err.message : "An error occurred");
        } finally {
            setActionUid(null);
            setActiveAction(null);
        }
    };

    const handleResetPassword = async (uid: string) => {
        if (!newPassword || newPassword.length < 4) {
            setError("PIN must be at least 4 characters");
            return;
        }
        setActionUid(uid);
        setActiveAction("resetPassword");
        setMessage("");
        setError("");
        try {
            const res = await resetStaffPassword(uid, newPassword);
            if (res.success) {
                setMessage("Password reset successfully.");
                setNewPassword("");
            } else {
                setError(res.error || "Failed to reset password");
            }
        } catch (err: unknown) {
            setError(err instanceof Error ? err.message : "An error occurred");
        } finally {
            setActionUid(null);
            setActiveAction(null);
        }
    };

    return (
        <div className="glass-card p-6 mt-6">
            <div className="flex items-center space-x-3 mb-6 border-b border-gray-100 dark:border-gray-800 pb-4">
                <div className="p-3 rounded-xl bg-purple-50 dark:bg-purple-900/30 text-purple-600 dark:text-purple-400">
                    <Users size={24} />
                </div>
                <div>
                    <h2 className="text-lg font-bold text-gray-900 dark:text-white">
                        Staff Management
                    </h2>
                    <p className="text-sm text-gray-500 dark:text-gray-400">
                        Add and manage staff members for your store
                    </p>
                </div>
            </div>

            {/* Create Form */}
            <div className="bg-gray-50 dark:bg-gray-900/50 p-5 rounded-xl border border-gray-100 dark:border-gray-800 mb-6">
                <h3 className="text-sm font-semibold text-gray-900 dark:text-white mb-4 flex items-center">
                    <UserPlus size={16} className="mr-2" />
                    Create New Staff Account
                </h3>

                <form onSubmit={handleCreate} className="space-y-4">
                    <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
                        <div>
                            <label className="block text-xs font-medium text-gray-700 dark:text-gray-300 mb-1">
                                Username (No Spaces)
                            </label>
                            <input
                                aria-label="text"
                                type="text"
                                required
                                value={username}
                                onChange={(e) =>
                                    setUsername(
                                        sanitizeInput(e.target.value)
                                            .replace(/\s+/g, "")
                                            .toLowerCase(),
                                    )
                                }
                                placeholder="e.g. rahulstaff"
                                className="w-full px-3 py-2 border border-gray-200 dark:border-gray-700 rounded-lg bg-white dark:bg-gray-800 focus:ring-2 focus:ring-teal-500 outline-none text-sm dark:text-white"
                            />
                        </div>
                        <div>
                            <label className="block text-xs font-medium text-gray-700 dark:text-gray-300 mb-1">
                                Login PIN
                            </label>
                            <input
                                aria-label="password"
                                type="password"
                                required
                                value={pin}
                                onChange={(e) =>
                                    setPin(sanitizeInput(e.target.value))
                                }
                                placeholder="4-6 digit PIN"
                                className="w-full px-3 py-2 border border-gray-200 dark:border-gray-700 rounded-lg bg-white dark:bg-gray-800 focus:ring-2 focus:ring-teal-500 outline-none text-sm dark:text-white"
                            />
                        </div>
                        <div>
                            <label className="block text-xs font-medium text-gray-700 dark:text-gray-300 mb-1">
                                Role
                            </label>
                            <select
                                value={newRole}
                                onChange={(e) =>
                                    setNewRole(
                                        e.target.value as "cashier" | "manager",
                                    )
                                }
                                className="w-full px-3 py-2 border border-gray-200 dark:border-gray-700 rounded-lg bg-white dark:bg-gray-800 focus:ring-2 focus:ring-teal-500 outline-none text-sm dark:text-white"
                            >
                                <option value="cashier">Cashier</option>
                                <option value="manager">Manager</option>
                            </select>
                        </div>
                    </div>

                    <div className="flex flex-col space-y-3 mt-4">
                        <label className="flex items-center space-x-2 text-sm text-gray-700 dark:text-gray-300">
                            <input
                                aria-label="checkbox"
                                type="checkbox"
                                checked={canViewProfit}
                                onChange={(e) =>
                                    setCanViewProfit(e.target.checked)
                                }
                                className="w-4 h-4 text-teal-600 rounded border-gray-300 focus:ring-teal-500"
                            />
                            <span>Can View Profit Margins</span>
                        </label>
                        <label className="flex items-center space-x-2 text-sm text-gray-700 dark:text-gray-300">
                            <input
                                aria-label="checkbox"
                                type="checkbox"
                                checked={canDelete}
                                onChange={(e) => setCanDelete(e.target.checked)}
                                className="w-4 h-4 text-teal-600 rounded border-gray-300 focus:ring-teal-500"
                            />
                            <span>Can Delete Sales/Items</span>
                        </label>
                    </div>

                    {error && (
                        <p className="text-red-500 text-xs font-medium flex items-center">
                            <XCircle size={14} className="mr-1" />
                            {error}
                        </p>
                    )}
                    {message && (
                        <p className="text-teal-600 dark:text-teal-400 text-xs font-medium flex items-center">
                            <CheckCircle size={14} className="mr-1" />
                            {message}
                        </p>
                    )}

                    <div className="flex justify-end mt-4">
                        <button
                            type="submit"
                            disabled={isCreating || !username || !pin}
                            className="bg-purple-600 hover:bg-purple-700 text-white px-4 py-2 rounded-lg text-sm font-medium transition-colors disabled:opacity-50 flex items-center"
                        >
                            {isCreating ? (
                                <Loader2
                                    size={16}
                                    className="animate-spin mr-2"
                                />
                            ) : (
                                <ShieldCheck size={16} className="mr-2" />
                            )}
                            Create Staff
                        </button>
                    </div>
                </form>
            </div>

            {/* Staff List */}
            {loadingList ? (
                <div className="flex justify-center py-8">
                    <Loader2 size={24} className="animate-spin text-gray-400" />
                </div>
            ) : listError ? (
                <div className="flex items-center space-x-2 text-red-500 text-sm py-4">
                    <AlertCircle size={16} />
                    <span>{listError}</span>
                </div>
            ) : staff.length === 0 ? (
                <div className="text-center py-8 text-gray-400 dark:text-gray-500">
                    <Users size={32} className="mx-auto mb-2 opacity-40" />
                    <p className="text-sm">No staff accounts found</p>
                </div>
            ) : (
                <div className="overflow-x-auto rounded-xl border border-gray-100 dark:border-gray-800">
                    <table className="w-full text-sm">
                        <thead className="bg-gray-50 dark:bg-gray-900/60 text-gray-500 dark:text-gray-400 uppercase text-xs font-semibold">
                            <tr>
                                <th className="px-4 py-3 text-left">
                                    Username
                                </th>
                                <th className="px-4 py-3 text-left">Role</th>
                                <th className="px-4 py-3 text-left">Joined</th>
                                <th className="px-4 py-3 text-center">
                                    Actions
                                </th>
                            </tr>
                        </thead>
                        <tbody className="divide-y divide-gray-100 dark:divide-gray-800 bg-white dark:bg-gray-900/20">
                            {staff.map((member) => (
                                <tr
                                    key={member.id}
                                    className="hover:bg-gray-50 dark:hover:bg-gray-800/40 transition-colors"
                                >
                                    <td className="px-4 py-3 text-gray-900 dark:text-white font-medium">
                                        {member.username || "—"}
                                    </td>
                                    <td className="px-4 py-3">
                                        <div className="flex items-center space-x-2">
                                            <span
                                                className={`inline-block px-2.5 py-0.5 rounded-full text-xs font-medium ${
                                                    member.role === "manager"
                                                        ? "bg-yellow-100 text-yellow-700 dark:bg-yellow-900/30 dark:text-yellow-400"
                                                        : "bg-blue-100 text-blue-700 dark:bg-blue-900/30 dark:text-blue-400"
                                                }`}
                                            >
                                                {ROLE_LABELS[member.role] ??
                                                    member.role}
                                            </span>
                                            <select
                                                value={member.role}
                                                onChange={(e) =>
                                                    handleRoleChange(
                                                        member.id,
                                                        e.target.value as
                                                            | "cashier"
                                                            | "manager",
                                                    )
                                                }
                                                className="px-2 py-0.5 border border-gray-200 dark:border-gray-700 rounded bg-white dark:bg-gray-800 text-xs outline-none dark:text-white focus:ring-1 focus:ring-teal-500"
                                            >
                                                <option value="cashier">
                                                    Cashier
                                                </option>
                                                <option value="manager">
                                                    Manager
                                                </option>
                                            </select>
                                        </div>
                                    </td>
                                    <td className="px-4 py-3 text-gray-500 dark:text-gray-400 text-xs">
                                        {member.createdAt
                                            ? new Date(
                                                  member.createdAt * 1000,
                                              ).toLocaleDateString()
                                            : "—"}
                                    </td>
                                    <td className="px-4 py-3">
                                        <div className="flex items-center justify-center space-x-2">
                                            {/* Reset Password */}
                                            <button
                                                onClick={() => {
                                                    setResetTarget(member.id);
                                                    setResetConfirm("");
                                                }}
                                                title="Reset Password"
                                                disabled={activeAction !== null}
                                                className="p-1.5 rounded-lg text-gray-500 hover:text-yellow-600 hover:bg-yellow-50 dark:hover:bg-yellow-900/20 transition-colors disabled:opacity-40"
                                            >
                                                {activeAction ===
                                                    "resetPassword" &&
                                                actionUid === member.id ? (
                                                    <Loader2
                                                        size={14}
                                                        className="animate-spin"
                                                    />
                                                ) : (
                                                    <KeyRound size={14} />
                                                )}
                                            </button>
                                            {/* Delete */}
                                            <button
                                                onClick={() =>
                                                    handleDelete(member.id)
                                                }
                                                title="Delete Staff"
                                                disabled={activeAction !== null}
                                                className="p-1.5 rounded-lg text-gray-500 hover:text-red-600 hover:bg-red-50 dark:hover:bg-red-900/20 transition-colors disabled:opacity-40"
                                            >
                                                {activeAction === "delete" &&
                                                actionUid === member.id ? (
                                                    <Loader2
                                                        size={14}
                                                        className="animate-spin"
                                                    />
                                                ) : (
                                                    <Trash2 size={14} />
                                                )}
                                            </button>
                                        </div>
                                    </td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                </div>
            )}

            {resetTarget && (
                <div className="fixed inset-0 z-[60] flex items-center justify-center bg-black/50 backdrop-blur-sm">
                    <div className="bg-white dark:bg-gray-900 rounded-xl shadow-xl w-full max-w-sm p-6 border border-gray-200 dark:border-gray-700">
                        <h3 className="text-base font-bold text-gray-900 dark:text-white mb-4">
                            Reset Staff PIN
                        </h3>
                        <input
                            aria-label="password"
                            type="password"
                            value={resetConfirm}
                            onChange={(e) => setResetConfirm(e.target.value)}
                            placeholder="New PIN (min 4 characters)"
                            className="w-full px-3 py-2 border border-gray-200 dark:border-gray-700 rounded-lg bg-white dark:bg-gray-800 text-sm focus:ring-2 focus:ring-teal-500 outline-none dark:text-white mb-4"
                        />
                        <div className="flex justify-end space-x-2">
                            <button
                                type="button"
                                onClick={() => {
                                    setResetTarget(null);
                                    setResetConfirm("");
                                }}
                                className="px-3 py-1.5 rounded-lg text-sm font-medium text-gray-600 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-800 transition-colors"
                            >
                                Cancel
                            </button>
                            <button
                                type="button"
                                onClick={() => {
                                    if (resetConfirm.length >= 4) {
                                        setNewPassword(resetConfirm);
                                        handleResetPassword(resetTarget);
                                        setResetTarget(null);
                                        setResetConfirm("");
                                    }
                                }}
                                disabled={activeAction !== null}
                                className="px-3 py-1.5 rounded-lg text-sm font-medium bg-yellow-600 hover:bg-yellow-700 text-white transition-colors disabled:opacity-40"
                            >
                                {activeAction === "resetPassword" && actionUid === resetTarget
                                    ? "Resetting..."
                                    : "Confirm"}
                            </button>
                        </div>
                    </div>
                </div>
            )}

            <div className="mt-4 text-xs text-gray-500 dark:text-gray-400">
                * Staff permissions are role-based. Manager can view reports but
                cannot manage other staff. Cashier has limited access.
            </div>
        </div>
    );
}
