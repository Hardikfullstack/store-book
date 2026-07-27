'use client';

import { useState, useEffect } from 'react';
import { Store, AlertCircle, CheckCircle2, Loader2 } from 'lucide-react';
import { validateGSTIN, getGSTStateName } from '@/lib/gstinUtils';

interface StoreProfileProps {
  storeId: string;
  storeName?: string;
}

function emptyState() {
  return {
    name: '',
    gstin: '',
    address: '',
    phone: '',
  };
}

export default function BusinessProfileEdit({ storeId, storeName }: StoreProfileProps) {
  const [formData, setFormData] = useState(emptyState());
  const [isSaving, setIsSaving] = useState(false);
  const [justSaved, setJustSaved] = useState(false);
  const [gstinError, setGstinError] = useState<string | null>(null);
  const [gstinSuccess, setGstinSuccess] = useState(false);
  const [suggestedState, setSuggestedState] = useState<string | null>(null);

  const initialData = { name: storeName || '', gstin: '', address: '', phone: '' };

  function validateField(value: string) {
    const result = validateGSTIN(value.trim() || undefined);
    if (value.trim().length === 0) {
      setGstinError(null);
      setGstinSuccess(false);
      setSuggestedState(null);
      return;
    }
    if (result.isValid) {
      setGstinError(null);
      setGstinSuccess(true);
      setSuggestedState(result.stateName);
    } else {
      setGstinError(result.errors[0] || 'Invalid GSTIN format');
      setGstinSuccess(false);
      if (result.stateCode) {
        setSuggestedState(getGSTStateName(result.stateCode));
      } else {
        setSuggestedState(null);
      }
    }
  }

  const handleChange = (field: string, value: string) => {
    setFormData({ ...formData, [field]: value });
    if (field === 'gstin') validateField(value);
  };

  const handleSave = async () => {
    if (gstinError) return;

    setIsSaving(true);
    setJustSaved(false);
    try {
      await new Promise(r => setTimeout(r, 600));
      // TODO: wire up to syncBusinessProfileRef once the DataConnect mutation is added
      console.log('[BusinessProfileEdit] saved profile', storeId, {
        name: formData.name.trim(),
        gstin: formData.gstin.trim().toUpperCase() || null,
        address: formData.address.trim() || null,
        phone: formData.phone.trim() || null,
      });

      setJustSaved(true);
      setTimeout(() => setJustSaved(false), 3000);
    } catch (err) {
      console.error('Failed to save business profile:', err);
    } finally {
      setIsSaving(false);
    }
  };

  const isSaveDisabled = !!gstinError || !formData.name.trim();

  return (
    <div className="glass-card p-6">
      <div className="flex items-center space-x-3 mb-6 border-b border-gray-100 dark:border-gray-800 pb-4">
        <div className="p-3 rounded-xl bg-teal-50 dark:bg-teal-900/30 text-teal-600 dark:text-teal-400">
          <Store size={24} />
        </div>
        <div>
          <h2 className="text-lg font-bold text-gray-900 dark:text-white">Business Details</h2>
          <p className="text-sm text-gray-500 dark:text-gray-400">GSTIN & invoice configuration</p>
        </div>
      </div>

      <div className="space-y-4">
        <div>
          <label className="block text-xs font-semibold text-gray-500 uppercase tracking-wider" htmlFor="store-name-input">Store Name</label>
          <input
            id="store-name-input"
            type="text"
            value={formData.name}
            onChange={(e) => handleChange('name', e.target.value)}
            className="mt-1 w-full p-2 border dark:border-gray-700 bg-white dark:bg-gray-800 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-teal-500 dark:text-white"
          />
        </div>

        <div>
          <label className="block text-xs font-semibold text-gray-500 uppercase tracking-wider" htmlFor="gstin-input">GSTIN (Optional)</label>
          <input
            id="gstin-input"
            type="text"
            maxLength={15}
            value={formData.gstin.toUpperCase()}
            onChange={(e) => handleChange('gstin', e.target.value.toUpperCase())}
            onBlur={() => validateField(formData.gstin)}
            className={`mt-1 w-full p-2 border rounded-lg text-sm font-mono uppercase focus:outline-none focus:ring-2 dark:bg-gray-800 dark:text-white transition-colors ${
              gstinError
                ? 'border-red-400 focus:ring-red-500 dark:border-red-500'
                : gstinSuccess
                  ? 'border-emerald-400 focus:ring-emerald-500 dark:border-emerald-500'
                  : 'border-gray-200 dark:border-gray-700 focus:ring-teal-500'
            }`}
            placeholder="e.g. 29ABCDE1234F1Z5"
          />

          {gstinError && (
            <div className="flex items-center gap-1 mt-1 text-xs text-red-600 dark:text-red-400">
              <AlertCircle size={12} />
              <span>{gstinError}</span>
            </div>
          )}

          {gstinSuccess && (
            <div className="flex items-center gap-1 mt-1 text-xs text-emerald-600 dark:text-emerald-400">
              <CheckCircle2 size={12} />
              <span>Valid GSTIN</span>
              {suggestedState && <span> ({suggestedState})</span>}
            </div>
          )}

          {!gstinSuccess && !gstinError && formData.gstin.trim().length > 0 && (
            <p className="text-[11px] text-amber-600 dark:text-amber-400 mt-1">Please enter a valid 15-character GSTIN before saving</p>
          )}

          {formData.gstin.trim().length === 0 && (
            <p className="text-[11px] text-gray-400 dark:text-gray-500 mt-1">Leaving blank means no tax will be applied to invoices</p>
          )}
        </div>

        <div>
          <label className="block text-xs font-semibold text-gray-500 uppercase tracking-wider" htmlFor="store-address-input">Business Address</label>
          <textarea
            id="store-address-input"
            rows={2}
            value={formData.address}
            onChange={(e) => handleChange('address', e.target.value)}
            className="mt-1 w-full p-2 border dark:border-gray-700 bg-white dark:bg-gray-800 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-teal-500 dark:text-white"
            placeholder="For invoice address line"
          />
        </div>

        <div>
          <label className="block text-xs font-semibold text-gray-500 uppercase tracking-wider" htmlFor="store-phone-input">Business Phone</label>
          <input
            id="store-phone-input"
            type="tel"
            value={formData.phone}
            onChange={(e) => handleChange('phone', e.target.value)}
            className="mt-1 w-full p-2 border dark:border-gray-700 bg-white dark:bg-gray-800 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-teal-500 dark:text-white"
          />
        </div>

        <button
          type="button"
          onClick={handleSave}
          disabled={isSaveDisabled || isSaving || justSaved}
          className={`w-full flex items-center justify-center gap-2 py-2 px-4 rounded-lg text-sm font-medium transition-colors ${
            isSaveDisabled
              ? 'bg-gray-100 text-gray-400 cursor-not-allowed dark:bg-gray-800 dark:text-gray-600'
              : justSaved
                ? 'bg-emerald-100 text-emerald-700 dark:bg-emerald-900/30 dark:text-emerald-400'
                : 'bg-teal-600 text-white hover:bg-teal-700 dark:bg-teal-500 dark:hover:bg-teal-600 focus:outline-none focus:ring-2 focus:ring-offset-1 focus:ring-teal-500'
          }`}
        >
          {isSaving && <Loader2 size={16} className="animate-spin" />}
          {justSaved ? 'Saved!' : 'Save Changes'}
        </button>
      </div>
    </div>
  );
}
