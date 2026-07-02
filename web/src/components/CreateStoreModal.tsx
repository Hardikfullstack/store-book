'use client';
import { useState } from 'react';
import { createStore } from '@/app/actions';
import { sanitizeInput } from '@/lib/sanitize';

export default function CreateStoreModal({ onClose }: { onClose: () => void }) {
  const [storeName, setStoreName] = useState('');
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!storeName.trim()) return;
    setLoading(true);
    try {
      const sanitizedName = sanitizeInput(storeName);
      await createStore(sanitizedName);
      window.location.href = '/';
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
            <label className="block text-sm font-medium dark:text-gray-300">Store Name</label>
            <input aria-label="text" 
              required 
              type="text" 
              value={storeName} 
              onChange={e => setStoreName(sanitizeInput(e.target.value))} 
              className="mt-1 w-full p-2 border dark:border-gray-700 rounded dark:bg-gray-800 dark:text-white" 
              placeholder="e.g. My Second Shop"
            />
          </div>
          <div className="flex justify-end space-x-3 mt-6">
            <button type="button" onClick={onClose} disabled={loading} className="px-4 py-2 text-gray-500 hover:text-gray-700 dark:text-gray-400 dark:hover:text-gray-200">Cancel</button>
            <button type="submit" disabled={loading} className="btn-primary">{loading ? 'Creating...' : 'Create Store'}</button>
          </div>
        </form>
      </div>
    </div>
  );
}
