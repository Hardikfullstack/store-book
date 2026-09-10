'use client';
import { useState } from 'react';
import { createStore } from '@/app/actions';
import { sanitizeInput } from '@/lib/sanitize';
import { BUSINESS_TYPES } from '@/lib/businessTypes';

export default function CreateStoreModal({ onClose }: { onClose: () => void }) {
  const [storeName, setStoreName] = useState('');
  const [businessType, setBusinessType] = useState('');
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!storeName.trim() || !businessType) return;
    setLoading(true);
    try {
      const sanitizedName = sanitizeInput(storeName);
      const res = await createStore(sanitizedName, businessType);
      if (res.success) {
        const { persistor } = await import('@/store');
        await persistor.purge();
        if (typeof window !== 'undefined') {
          window.sessionStorage.clear();
        }
        window.location.href = '/';
      } else {
        alert(res.error || 'Failed to create store.');
        setLoading(false);
      }
    } catch (err) {
      console.error(err);
      alert('Failed to create store.');
      setLoading(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 backdrop-blur-sm">
      <div className="bg-white dark:bg-gray-900 rounded-xl shadow-xl w-full max-w-md p-6">
        <h2 className="text-xl font-bold mb-4 dark:text-white">Create New Store</h2>
        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label className="block text-sm font-medium dark:text-gray-300">
              Store Name <span className="text-red-500">*</span>
            </label>
            <input
              aria-label="Store Name"
              required
              type="text"
              value={storeName}
              onChange={e => setStoreName(sanitizeInput(e.target.value))}
              className="mt-1 w-full p-2 border dark:border-gray-700 rounded dark:bg-gray-800 dark:text-white focus:ring-2 focus:ring-teal-500 focus:outline-none"
              placeholder="e.g. My Pharmacy Store"
            />
          </div>

          <div>
            <label className="block text-sm font-medium dark:text-gray-300">
              Business Type <span className="text-red-500">*</span>
            </label>
            <select
              aria-label="Business Type"
              required
              value={businessType}
              onChange={e => setBusinessType(e.target.value)}
              className="mt-1 w-full p-2 border dark:border-gray-700 rounded dark:bg-gray-800 dark:text-white focus:ring-2 focus:ring-teal-500 focus:outline-none"
            >
              <option value="" disabled>
                Select Business Type (Required)...
              </option>
              {BUSINESS_TYPES.map(bt => (
                <option key={bt.id} value={bt.id}>
                  {bt.emoji} {bt.label}
                </option>
              ))}
            </select>
            <p className="mt-1 text-xs text-gray-500 dark:text-gray-400">
              Categories will be shared across all stores of this business type.
            </p>
          </div>

          <div className="flex justify-end space-x-3 mt-6">
            <button
              type="button"
              onClick={onClose}
              disabled={loading}
              className="px-4 py-2 text-gray-500 hover:text-gray-700 dark:text-gray-400 dark:hover:text-gray-200"
            >
              Cancel
            </button>
            <button type="submit" disabled={loading} className="btn-primary">
              {loading ? 'Creating...' : 'Create Store'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}

