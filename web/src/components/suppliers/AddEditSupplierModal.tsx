'use client';

import React, { useState } from 'react';
import { X, Building2, Loader2, Check } from 'lucide-react';
import { dataConnect } from '@/lib/firebase';
import { syncSupplier } from '@/dataconnect';
import { sanitizeInput } from '@/lib/sanitize';

export interface SupplierFormInitialData {
  id: string;
  name: string;
  phone?: string | null;
  gstin?: string | null;
  address?: string | null;
  updatedAt?: number;
}

interface AddEditSupplierModalProps {
  isOpen: boolean;
  onClose: () => void;
  storeId: string;
  supplier?: SupplierFormInitialData | null;
  onSuccess: (savedSupplier: SupplierFormInitialData) => void;
}

// 15-character Indian GST format regex
const GSTIN_REGEX = /^[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z]{1}[1-9A-Z]{1}Z[0-9A-Z]$/;

export default function AddEditSupplierModal({
  isOpen,
  onClose,
  storeId,
  supplier,
  onSuccess,
}: AddEditSupplierModalProps) {
  // Component remounts on each open (conditionally rendered), so initial values work as reset
  const [name, setName] = useState(supplier?.name || '');
  const [phone, setPhone] = useState(supplier?.phone || '');
  const [gstin, setGstin] = useState(supplier?.gstin || '');
  const [address, setAddress] = useState(supplier?.address || '');

  const [errors, setErrors] = useState<{ name?: string; gstin?: string }>({});
  const [isSubmitting, setIsSubmitting] = useState(false);

  const isEditing = Boolean(supplier?.id);

  if (!isOpen) return null;

  const validate = () => {
    const newErrors: { name?: string; gstin?: string } = {};
    const trimmedName = name.trim();
    if (!trimmedName) {
      newErrors.name = 'Supplier name is required.';
    }

    const trimmedGstin = gstin.trim().toUpperCase();
    if (trimmedGstin && !GSTIN_REGEX.test(trimmedGstin)) {
      newErrors.gstin = 'Invalid GSTIN format (e.g. 22AAAAA0000A1Z5).';
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!validate()) return;
    if (!storeId) return;

    setIsSubmitting(true);
    try {
      const supplierId = supplier?.id || crypto.randomUUID();
      const now = Date.now();
      const trimmedName = sanitizeInput(name.trim());
      const trimmedPhone = phone.trim() ? sanitizeInput(phone.trim()) : null;
      const trimmedGstin = gstin.trim() ? sanitizeInput(gstin.trim().toUpperCase()) : null;
      const trimmedAddress = address.trim() ? sanitizeInput(address.trim()) : null;

      await syncSupplier(dataConnect, {
        id: supplierId,
        storeId,
        name: trimmedName,
        phone: trimmedPhone,
        gstin: trimmedGstin,
        address: trimmedAddress,
        isDeleted: false,
        updatedAt: now,
      });

      onSuccess({
        id: supplierId,
        name: trimmedName,
        phone: trimmedPhone,
        gstin: trimmedGstin,
        address: trimmedAddress,
        updatedAt: now,
      });
      onClose();
    } catch (err) {
      console.error('Failed to save supplier:', err);
      setErrors((prev) => ({
        ...prev,
        name: 'Failed to save supplier. Please try again.',
      }));
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 overflow-y-auto bg-black/60 backdrop-blur-sm flex items-center justify-center p-4">
      <div className="bg-white dark:bg-gray-900 border border-gray-100 dark:border-gray-800 rounded-2xl max-w-lg w-full overflow-hidden shadow-2xl animate-in fade-in zoom-in-95 duration-150">
        {/* Header */}
        <div className="px-6 py-4 border-b border-gray-100 dark:border-gray-800 flex justify-between items-center bg-gray-50 dark:bg-gray-800/80">
          <div className="flex items-center space-x-2.5">
            <div className="w-9 h-9 rounded-xl bg-teal-50 dark:bg-teal-950/60 text-teal-600 dark:text-teal-400 flex items-center justify-center shrink-0">
              <Building2 size={20} />
            </div>
            <div>
              <h2 className="text-lg font-bold text-gray-900 dark:text-white">
                {isEditing ? 'Edit Supplier' : 'Add New Supplier'}
              </h2>
              <p className="text-xs text-gray-500 dark:text-gray-300">
                {isEditing
                  ? 'Update supplier contact details and tax info.'
                  : 'Register a new vendor for purchase bills and payments.'}
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
          {/* 1. Supplier Name (Required) */}
          <div>
            <label className="block text-xs font-semibold text-gray-700 dark:text-gray-200 uppercase tracking-wider mb-1.5">
              Supplier Name <span className="text-red-500">*</span>
            </label>
            <input
              type="text"
              value={name}
              onChange={(e) => {
                setName(e.target.value);
                if (errors.name) setErrors((prev) => ({ ...prev, name: undefined }));
              }}
              placeholder="e.g. Mahadev Traders"
              className={`w-full px-3.5 py-2.5 rounded-xl border text-sm bg-white dark:bg-gray-800 text-gray-900 dark:text-white placeholder-gray-400 dark:placeholder-gray-500 focus:outline-none focus:ring-2 transition-all ${errors.name
                  ? 'border-red-300 dark:border-red-800 focus:ring-red-500/20'
                  : 'border-gray-200 dark:border-gray-700 focus:ring-teal-500/20 focus:border-teal-500'
                }`}
              autoFocus
            />
            {errors.name && (
              <p className="text-xs text-red-500 mt-1 font-medium">{errors.name}</p>
            )}
          </div>

          {/* 2. Phone Number (Optional) */}
          <div>
            <label className="block text-xs font-semibold text-gray-700 dark:text-gray-200 uppercase tracking-wider mb-1.5">
              Phone Number <span className="text-[11px] font-normal text-gray-400 dark:text-gray-400">(Optional)</span>
            </label>
            <input
              type="tel"
              value={phone}
              onChange={(e) => setPhone(e.target.value)}
              placeholder="e.g. 9876543210"
              className="w-full px-3.5 py-2.5 rounded-xl border border-gray-200 dark:border-gray-700 text-sm bg-white dark:bg-gray-800 text-gray-900 dark:text-white placeholder-gray-400 dark:placeholder-gray-500 focus:outline-none focus:ring-2 focus:ring-teal-500/20 focus:border-teal-500 transition-all"
            />
          </div>

          {/* 3. GSTIN (Optional) */}
          <div>
            <label className="block text-xs font-semibold text-gray-700 dark:text-gray-200 uppercase tracking-wider mb-1.5">
              GSTIN <span className="text-[11px] font-normal text-gray-400 dark:text-gray-400">(Optional, 15 Characters)</span>
            </label>
            <input
              type="text"
              value={gstin}
              onChange={(e) => {
                setGstin(e.target.value.toUpperCase());
                if (errors.gstin) setErrors((prev) => ({ ...prev, gstin: undefined }));
              }}
              placeholder="e.g. 24AAAAA0000A1Z5"
              maxLength={15}
              className={`w-full px-3.5 py-2.5 rounded-xl border font-mono text-sm uppercase bg-white dark:bg-gray-800 text-gray-900 dark:text-white placeholder-gray-400 dark:placeholder-gray-500 focus:outline-none focus:ring-2 transition-all ${errors.gstin
                  ? 'border-red-300 dark:border-red-800 focus:ring-red-500/20'
                  : 'border-gray-200 dark:border-gray-700 focus:ring-teal-500/20 focus:border-teal-500'
                }`}
            />
            {errors.gstin && (
              <p className="text-xs text-red-500 mt-1 font-medium">{errors.gstin}</p>
            )}
          </div>

          {/* 4. Address (Optional) */}
          <div>
            <label className="block text-xs font-semibold text-gray-700 dark:text-gray-200 uppercase tracking-wider mb-1.5">
              Address <span className="text-[11px] font-normal text-gray-400 dark:text-gray-400">(Optional)</span>
            </label>
            <textarea
              value={address}
              onChange={(e) => setAddress(e.target.value)}
              placeholder="e.g. Shop 12, APMC Market, Surat, Gujarat"
              rows={3}
              className="w-full px-3.5 py-2.5 rounded-xl border border-gray-200 dark:border-gray-700 text-sm bg-white dark:bg-gray-800 text-gray-900 dark:text-white placeholder-gray-400 dark:placeholder-gray-500 focus:outline-none focus:ring-2 focus:ring-teal-500/20 focus:border-teal-500 transition-all resize-none"
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
              className="inline-flex items-center space-x-1.5 px-5 py-2 text-sm font-semibold text-white bg-teal-600 hover:bg-teal-700 rounded-xl shadow-sm hover:shadow transition-all disabled:opacity-50"
            >
              {isSubmitting ? (
                <Loader2 size={16} className="animate-spin" />
              ) : (
                <Check size={16} />
              )}
              <span>{isEditing ? 'Update Supplier' : 'Save Supplier'}</span>
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
