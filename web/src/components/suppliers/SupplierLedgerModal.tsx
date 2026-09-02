'use client';

import React, { useState, useMemo } from 'react';
import {
  X,
  Phone,
  MapPin,
  FileText,
  CreditCard,
  Plus,
  ArrowUpRight,
  ArrowDownLeft,
  Building2,
} from 'lucide-react';
import { FormattedAmount } from '@/components/FormattedAmount';
import RecordSupplierPaymentModal, {
  RecordedPaymentData,
} from './RecordSupplierPaymentModal';

export interface SupplierLedgerItem {
  id: string;
  supplierId: string;
  supplierName: string;
  totalAmount: number;
  taxAmount?: number;
  type: string;
  timestamp: number;
  notes?: string | null;
  isDeleted?: boolean;
}

interface SupplierLedgerModalProps {
  isOpen: boolean;
  onClose: () => void;
  storeId: string;
  supplier: {
    id: string;
    name: string;
    phone?: string | null;
    gstin?: string | null;
    address?: string | null;
  };
  initialPurchases: SupplierLedgerItem[];
  onPaymentLogged: (payment: RecordedPaymentData) => void;
}

export default function SupplierLedgerModal({
  isOpen,
  onClose,
  storeId,
  supplier,
  initialPurchases,
  onPaymentLogged,
}: SupplierLedgerModalProps) {
  const [showRecordPayment, setShowRecordPayment] = useState(false);
  const [localPayments, setLocalPayments] = useState<SupplierLedgerItem[]>([]);

  // Merge prop-driven purchases with locally-added payments (no effect needed)
  const transactions = useMemo(() => {
    const existingIds = new Set(initialPurchases.map((p) => p.id));
    const uniqueLocal = localPayments.filter((p) => !existingIds.has(p.id));
    return [...uniqueLocal, ...initialPurchases];
  }, [initialPurchases, localPayments]);

  if (!isOpen) return null;

  // Calculate live balances from transactions
  let totalPurchases = 0;
  let totalPaid = 0;

  for (const t of transactions) {
    const amt = Number(t.totalAmount) || 0;
    const pType = String(t.type || '').toUpperCase();
    if (pType === 'PAYMENT') {
      totalPaid += amt;
    } else {
      totalPurchases += amt;
    }
  }

  const netBalance = totalPurchases - totalPaid;

  const handlePaymentSuccess = (payment: RecordedPaymentData) => {
    // Prepend to local payments and propagate to parent
    setLocalPayments((prev) => [payment, ...prev]);
    onPaymentLogged(payment);
  };

  const formatDate = (ts: number) => {
    if (!ts) return '-';
    const raw = ts < 100000000000 ? ts * 1000 : ts;
    const d = new Date(raw);
    if (isNaN(d.getTime())) return '-';
    return d.toLocaleDateString('en-IN', {
      day: '2-digit',
      month: 'short',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  // Sort transactions newest first
  const sortedTransactions = [...transactions].sort((a, b) => {
    const tsA = Number(a.timestamp) || 0;
    const tsB = Number(b.timestamp) || 0;
    return tsB - tsA;
  });

  return (
    <>
      <div className="fixed inset-0 z-50 overflow-y-auto bg-black/60 backdrop-blur-sm flex items-center justify-center p-4">
        <div className="bg-white dark:bg-gray-900 border border-gray-100 dark:border-gray-800 rounded-2xl max-w-2xl w-full overflow-hidden shadow-2xl animate-in fade-in zoom-in-95 duration-150 flex flex-col max-h-[90vh]">
          {/* 1. Header with Supplier Details */}
          <div className="px-6 py-5 border-b border-gray-100 dark:border-gray-800 bg-gray-50 dark:bg-gray-800/80 flex justify-between items-start">
            <div className="flex items-start space-x-3 min-w-0">
              <div className="w-11 h-11 rounded-xl bg-teal-50 dark:bg-teal-950/60 text-teal-600 dark:text-teal-400 flex items-center justify-center shrink-0 mt-0.5">
                <Building2 size={24} />
              </div>
              <div className="min-w-0 space-y-1">
                <div className="flex items-center space-x-2 flex-wrap">
                  <h2 className="text-xl font-bold text-gray-900 dark:text-white truncate">
                    {supplier.name}
                  </h2>
                  {supplier.gstin ? (
                    <span className="inline-block px-2 py-0.5 rounded text-[11px] font-mono font-medium text-gray-700 dark:text-gray-300 bg-gray-100 dark:bg-gray-800 border border-gray-200 dark:border-gray-700">
                      GSTIN: {supplier.gstin}
                    </span>
                  ) : null}
                </div>

                <div className="flex items-center space-x-4 text-xs text-gray-500 dark:text-gray-400 flex-wrap gap-y-1">
                  {supplier.phone ? (
                    <a
                      href={`tel:${supplier.phone}`}
                      className="inline-flex items-center space-x-1 text-teal-600 dark:text-teal-400 hover:underline font-medium"
                    >
                      <Phone size={13} />
                      <span>{supplier.phone}</span>
                    </a>
                  ) : null}
                  {supplier.address ? (
                    <div className="inline-flex items-center space-x-1 max-w-sm truncate text-gray-600 dark:text-gray-300" title={supplier.address}>
                      <MapPin size={13} className="shrink-0 text-gray-400 dark:text-gray-500" />
                      <span className="truncate">{supplier.address}</span>
                    </div>
                  ) : null}
                </div>
              </div>
            </div>

            <button
              type="button"
              onClick={onClose}
              className="p-1.5 rounded-lg text-gray-400 hover:text-gray-600 dark:hover:text-gray-200 hover:bg-gray-100 dark:hover:bg-gray-800 transition-colors"
            >
              <X size={20} />
            </button>
          </div>

          {/* 2. Middle Balance Banner & Record Payment Button */}
          <div className="p-6 border-b border-gray-100 dark:border-gray-800 bg-white dark:bg-gray-900">
            <div className="p-4 rounded-xl border border-gray-200 dark:border-gray-700 bg-gradient-to-br from-gray-50 to-gray-100/60 dark:from-gray-800 dark:to-gray-800/70 flex flex-col sm:flex-row sm:items-center justify-between gap-4">
              <div className="space-y-1">
                <div className="text-xs font-bold text-gray-600 dark:text-gray-300 uppercase tracking-wider">
                  Advance Balance
                </div>
                <div className="flex items-baseline space-x-2">
                  <div
                    className={`text-2xl font-black ${netBalance > 0
                      ? 'text-red-600 dark:text-red-400'
                      : netBalance < 0
                        ? 'text-emerald-600 dark:text-emerald-400'
                        : 'text-gray-700 dark:text-gray-200'
                      }`}
                  >
                    {netBalance > 0 ? '+' : ''}
                    <FormattedAmount amount={Math.abs(netBalance)} />
                  </div>
                </div>
                <div className="text-xs text-gray-500 dark:text-gray-400">
                  Total Bills: ₹{totalPurchases.toLocaleString('en-IN')} | Total Paid: ₹{totalPaid.toLocaleString('en-IN')}
                </div>
              </div>

              <button
                type="button"
                onClick={() => setShowRecordPayment(true)}
                className="inline-flex items-center justify-center space-x-1.5 px-4 py-2.5 text-sm font-bold text-white bg-emerald-600 hover:bg-emerald-700 rounded-xl shadow-sm hover:shadow transition-all self-start sm:self-auto shrink-0"
              >
                <Plus size={16} />
                <span>Record Payment</span>
              </button>
            </div>
          </div>

          {/* 3. Lower Scrollable Section: Transaction History */}
          <div className="p-6 flex-1 min-h-0 flex flex-col overflow-hidden bg-gray-50/40 dark:bg-gray-900">
            <div className="flex items-center justify-between mb-3 shrink-0">
              <h3 className="text-xs font-bold text-gray-600 dark:text-gray-300 uppercase tracking-wider">
                Transaction History ({sortedTransactions.length})
              </h3>
            </div>

            {sortedTransactions.length === 0 ? (
              <div className="flex-1 flex flex-col items-center justify-center p-8 text-center border-2 border-dashed border-gray-200 dark:border-gray-700 rounded-xl bg-white dark:bg-gray-800/60">
                <FileText size={32} className="text-gray-400 dark:text-gray-500 mb-2" />
                <p className="text-sm font-semibold text-gray-800 dark:text-gray-200">
                  No transaction history yet
                </p>
                <p className="text-xs text-gray-500 dark:text-gray-400 mt-0.5">
                  Purchase bills and logged payments for this supplier will appear here.
                </p>
              </div>
            ) : (
              <div className="flex-1 min-h-0 overflow-y-auto space-y-2.5 pr-1">
                {sortedTransactions.map((tx) => {
                  const isPayment = String(tx.type || '').toUpperCase() === 'PAYMENT';
                  const amountNum = Number(tx.totalAmount) || 0;

                  return (
                    <div
                      key={tx.id}
                      className="p-3.5 rounded-xl bg-white dark:bg-gray-800 border border-gray-100 dark:border-gray-700 shadow-sm flex items-center justify-between gap-3 hover:border-gray-200 dark:hover:border-gray-600 transition-colors"
                    >
                      <div className="flex items-center space-x-3 min-w-0">
                        <div
                          className={`w-9 h-9 rounded-xl flex items-center justify-center shrink-0 ${isPayment
                            ? 'bg-emerald-50 dark:bg-emerald-950/60 text-emerald-600 dark:text-emerald-400'
                            : 'bg-red-50 dark:bg-red-950/60 text-red-600 dark:text-red-400'
                            }`}
                        >
                          {isPayment ? (
                            <ArrowDownLeft size={18} />
                          ) : (
                            <ArrowUpRight size={18} />
                          )}
                        </div>

                        <div className="min-w-0 space-y-0.5">
                          <div className="flex items-center space-x-2">
                            <span className="text-sm font-bold text-gray-900 dark:text-white">
                              {isPayment ? 'Payment Paid' : 'Purchase Bill'}
                            </span>
                          </div>

                          <div className="text-xs text-gray-500 dark:text-gray-400 flex items-center space-x-2 flex-wrap">
                            <span>{formatDate(tx.timestamp)}</span>
                            {tx.notes ? (
                              <>
                                <span>•</span>
                                <span className="italic truncate text-gray-700 dark:text-gray-300">
                                  {tx.notes}
                                </span>
                              </>
                            ) : null}
                          </div>
                        </div>
                      </div>

                      <div className="text-right shrink-0">
                        <div
                          className={`text-sm font-black ${isPayment
                            ? 'text-emerald-600 dark:text-emerald-400'
                            : 'text-red-600 dark:text-red-400'
                            }`}
                        >
                          {isPayment ? '-' : '+'}
                          <FormattedAmount amount={amountNum} />
                        </div>
                      </div>
                    </div>
                  );
                })}
              </div>
            )}
          </div>
        </div>
      </div>

      {/* Nested Record Payment Sub-Modal */}
      {showRecordPayment && (
        <RecordSupplierPaymentModal
          isOpen={showRecordPayment}
          onClose={() => setShowRecordPayment(false)}
          storeId={storeId}
          supplier={{
            id: supplier.id,
            name: supplier.name,
            currentBalance: netBalance,
          }}
          onPaymentRecorded={handlePaymentSuccess}
        />
      )}
    </>
  );
}
