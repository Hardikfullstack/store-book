'use client';

import React, { useState } from 'react';
import { X, CreditCard, Loader2, Check } from 'lucide-react';
import { dataConnect } from '@/lib/firebase';
import { syncPurchase } from '@/dataconnect';
import { sanitizeInput } from '@/lib/sanitize';
import { FormattedAmount } from '@/components/FormattedAmount';

export interface RecordedPaymentData {
  id: string;
  supplierId: string;
  supplierName: string;
  totalAmount: number;
  taxAmount: number;
  type: string;
  timestamp: number;
  notes?: string | null;
  isDeleted: boolean;
  updatedAt: number;
}

interface RecordSupplierPaymentModalProps {
  isOpen: boolean;
  onClose: () => void;
  storeId: string;
  supplier: {
    id: string;
    name: string;
    currentBalance?: number;
  };
  onPaymentRecorded: (payment: RecordedPaymentData) => void;
}

export default function RecordSupplierPaymentModal({
  isOpen,
  onClose,
  storeId,
  supplier,
  onPaymentRecorded,
}: RecordSupplierPaymentModalProps) {
  // Component remounts on each open (conditionally rendered), so initial values work as reset
  const [amount, setAmount] = useState('');
  const [notes, setNotes] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  if (!isOpen) return null;

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    const parsedAmount = parseFloat(amount);

    if (isNaN(parsedAmount) || parsedAmount <= 0) {
      setError('Please enter a valid payment amount greater than 0.');
      return;
    }

    if (!storeId || !supplier?.id) return;

    setIsSubmitting(true);
    setError(null);

    try {
      const paymentId = crypto.randomUUID();
      const now = Date.now();
      const trimmedNotes = notes.trim() ? sanitizeInput(notes.trim()) : null;

      await syncPurchase(dataConnect, {
        id: paymentId,
        storeId,
        supplierId: supplier.id,
        supplierName: supplier.name,
        totalAmount: parsedAmount,
        taxAmount: 0.0,
        type: 'PAYMENT',
        timestamp: now,
        notes: trimmedNotes,
        isDeleted: false,
        updatedAt: now,
      });

      const paymentRecord: RecordedPaymentData = {
        id: paymentId,
        supplierId: supplier.id,
        supplierName: supplier.name,
        totalAmount: parsedAmount,
        taxAmount: 0.0,
        type: 'PAYMENT',
        timestamp: now,
        notes: trimmedNotes,
        isDeleted: false,
        updatedAt: now,
      };

      onPaymentRecorded(paymentRecord);
      onClose();
    } catch (err) {
      console.error('Failed to record supplier payment:', err);
      setError('Failed to record payment. Please try again.');
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="fixed inset-0 z-[70] overflow-y-auto bg-black/60 backdrop-blur-sm flex items-center justify-center p-4">
      <div className="bg-white dark:bg-gray-900 border border-gray-100 dark:border-gray-800 rounded-2xl max-w-md w-full overflow-hidden shadow-2xl animate-in fade-in zoom-in-95 duration-150">
        {/* Header */}
        <div className="px-6 py-4 border-b border-gray-100 dark:border-gray-800 flex justify-between items-center bg-gray-50 dark:bg-gray-800/80">
          <div className="flex items-center space-x-2.5">
            <div className="w-9 h-9 rounded-xl bg-emerald-50 dark:bg-emerald-950/60 text-emerald-600 dark:text-emerald-400 flex items-center justify-center shrink-0">
              <CreditCard size={20} />
            </div>
            <div>
              <h2 className="text-lg font-bold text-gray-900 dark:text-white">
                Record Payment
              </h2>
              <p className="text-xs text-gray-500 dark:text-gray-300">
                To: <span className="font-semibold text-gray-800 dark:text-gray-100">{supplier.name}</span>
              </p>
            </div>
          </div>

          <button
            type="button"
            onClick={onClose}
            className="p-1.5 rounded-lg text-gray-400 hover:text-gray-600 dark:hover:text-gray-200 hover:bg-gray-100 dark:hover:bg-gray-800 transition-colors"
          >
            <X size={18} />
          </button>
        </div>

        {/* Form */}
        <form onSubmit={handleSubmit} className="p-6 space-y-4">
          {/* Current balance indicator if available */}
          {typeof supplier.currentBalance === 'number' && (
            <div className="p-3 rounded-xl bg-gray-50 dark:bg-gray-800 border border-gray-200 dark:border-gray-700 flex items-center justify-between">
              <span className="text-xs font-semibold text-gray-600 dark:text-gray-300 uppercase tracking-wider">
                Current Balance
              </span>
              <span
                className={`text-sm font-bold ${supplier.currentBalance > 0
                  ? 'text-red-600 dark:text-red-400'
                  : supplier.currentBalance < 0
                    ? 'text-emerald-600 dark:text-emerald-400'
                    : 'text-gray-700 dark:text-gray-200'
                  }`}
              >
                {supplier.currentBalance > 0 ? 'Pending: +' : supplier.currentBalance < 0 ? 'Advance: ' : ''}
                <FormattedAmount amount={Math.abs(supplier.currentBalance)} />
              </span>
            </div>
          )}

          {/* 1. Amount Paid (Required) */}
          <div>
            <label className="block text-xs font-semibold text-gray-700 dark:text-gray-200 uppercase tracking-wider mb-1.5">
              Amount Paid (₹) <span className="text-red-500">*</span>
            </label>
            <div className="relative">
              <span className="absolute left-3.5 top-1/2 -translate-y-1/2 text-gray-400 dark:text-gray-400 font-bold text-base">
                ₹
              </span>
              <input
                type="number"
                step="0.01"
                min="0.01"
                value={amount}
                onChange={(e) => {
                  setAmount(e.target.value);
                  if (error) setError(null);
                }}
                placeholder="0.00"
                className={`w-full pl-8 pr-3.5 py-2.5 rounded-xl border text-base font-bold bg-white dark:bg-gray-800 text-gray-900 dark:text-white placeholder-gray-400 dark:placeholder-gray-500 focus:outline-none focus:ring-2 transition-all ${error
                  ? 'border-red-300 dark:border-red-800 focus:ring-red-500/20'
                  : 'border-gray-200 dark:border-gray-700 focus:ring-emerald-500/20 focus:border-emerald-500'
                  }`}
                autoFocus
              />
            </div>
            {error && (
              <p className="text-xs text-red-500 mt-1 font-medium">{error}</p>
            )}
          </div>

          {/* 2. Payment Notes (Optional) */}
          <div>
            <label className="block text-xs font-semibold text-gray-700 dark:text-gray-200 uppercase tracking-wider mb-1.5">
              Payment Notes <span className="text-[11px] font-normal text-gray-400 dark:text-gray-400">(Optional)</span>
            </label>
            <input
              type="text"
              value={notes}
              onChange={(e) => setNotes(e.target.value)}
              placeholder="e.g. Bank Ref, Cash, etc..."
              className="w-full px-3.5 py-2.5 rounded-xl border border-gray-200 dark:border-gray-700 text-sm bg-white dark:bg-gray-800 text-gray-900 dark:text-white placeholder-gray-400 dark:placeholder-gray-500 focus:outline-none focus:ring-2 focus:ring-emerald-500/20 focus:border-emerald-500 transition-all"
            />
          </div>

          {/* Actions */}
          <div className="pt-3 flex justify-end items-center space-x-3 border-t border-gray-100 dark:border-gray-800">
            <button
              type="button"
              onClick={onClose}
              disabled={isSubmitting}
              className="px-4 py-2 text-sm font-semibold text-gray-700 dark:text-gray-200 hover:bg-gray-100 dark:hover:bg-gray-800 rounded-xl transition-colors disabled:opacity-50"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={isSubmitting}
              className="inline-flex items-center space-x-1.5 px-5 py-2 text-sm font-semibold text-white bg-emerald-600 hover:bg-emerald-700 rounded-xl shadow-sm hover:shadow transition-all disabled:opacity-50"
            >
              {isSubmitting ? (
                <Loader2 size={16} className="animate-spin" />
              ) : (
                <Check size={16} />
              )}
              <span>Record Payment</span>
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
