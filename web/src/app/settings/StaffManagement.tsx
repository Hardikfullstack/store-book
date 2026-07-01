'use client';

import { useState } from 'react';
import { Users, UserPlus, Trash2, Loader2, ShieldCheck } from 'lucide-react';
import { createStaffAccount } from '@/app/actions';

export default function StaffManagement({ storeId }: { storeId: string }) {
  const [username, setUsername] = useState('');
  const [pin, setPin] = useState('');
  const [canViewProfit, setCanViewProfit] = useState(false);
  const [canDelete, setCanDelete] = useState(false);
  const [isCreating, setIsCreating] = useState(false);
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');

  const handleCreate = async (e: React.FormEvent) => {
    e.preventDefault();
    if (pin.length < 4) {
      setError('PIN must be at least 4 characters');
      return;
    }
    
    setIsCreating(true);
    setMessage('');
    setError('');

    try {
      // In production, we would pass permissions to the backend action.
      // Currently passing them to createStaffAccount (requires updating actions.ts).
      const res = await createStaffAccount(username, pin, { canViewProfit, canDelete });
      if (res.success) {
        setMessage(`Staff account ${username} created successfully!`);
        setUsername('');
        setPin('');
        setCanViewProfit(false);
        setCanDelete(false);
      } else {
        setError(res.error || 'Failed to create account');
      }
    } catch (err: any) {
      setError(err.message || 'An error occurred');
    } finally {
      setIsCreating(false);
    }
  };

  return (
    <div className="glass-card p-6 mt-6">
      <div className="flex items-center space-x-3 mb-6 border-b border-gray-100 dark:border-gray-800 pb-4">
        <div className="p-3 rounded-xl bg-purple-50 dark:bg-purple-900/30 text-purple-600 dark:text-purple-400">
          <Users size={24} />
        </div>
        <div>
          <h2 className="text-lg font-bold text-gray-900 dark:text-white">Staff Management</h2>
          <p className="text-sm text-gray-500 dark:text-gray-400">Add and manage staff members for your store</p>
        </div>
      </div>

      <div className="bg-gray-50 dark:bg-gray-900/50 p-5 rounded-xl border border-gray-100 dark:border-gray-800">
        <h3 className="text-sm font-semibold text-gray-900 dark:text-white mb-4 flex items-center">
          <UserPlus size={16} className="mr-2" />
          Create New Staff Account
        </h3>
        
        <form onSubmit={handleCreate} className="space-y-4">
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div>
              <label className="block text-xs font-medium text-gray-700 dark:text-gray-300 mb-1">Username (No Spaces)</label>
              <input 
                type="text" 
                required
                value={username}
                onChange={e => setUsername(e.target.value.replace(/\s+/g, '').toLowerCase())}
                placeholder="e.g. rahulstaff"
                className="w-full px-3 py-2 border border-gray-200 dark:border-gray-700 rounded-lg bg-white dark:bg-gray-800 focus:ring-2 focus:ring-teal-500 outline-none text-sm dark:text-white"
              />
            </div>
            <div>
              <label className="block text-xs font-medium text-gray-700 dark:text-gray-300 mb-1">Login PIN</label>
              <input 
                type="password" 
                required
                value={pin}
                onChange={e => setPin(e.target.value)}
                placeholder="4-6 digit PIN"
                className="w-full px-3 py-2 border border-gray-200 dark:border-gray-700 rounded-lg bg-white dark:bg-gray-800 focus:ring-2 focus:ring-teal-500 outline-none text-sm dark:text-white"
              />
            </div>
          </div>

          <div className="flex flex-col space-y-3 mt-4">
            <label className="flex items-center space-x-2 text-sm text-gray-700 dark:text-gray-300">
              <input 
                type="checkbox" 
                checked={canViewProfit}
                onChange={e => setCanViewProfit(e.target.checked)}
                className="w-4 h-4 text-teal-600 rounded border-gray-300 focus:ring-teal-500"
              />
              <span>Can View Profit Margins</span>
            </label>
            <label className="flex items-center space-x-2 text-sm text-gray-700 dark:text-gray-300">
              <input 
                type="checkbox" 
                checked={canDelete}
                onChange={e => setCanDelete(e.target.checked)}
                className="w-4 h-4 text-teal-600 rounded border-gray-300 focus:ring-teal-500"
              />
              <span>Can Delete Sales/Items</span>
            </label>
          </div>
          
          {error && <p className="text-red-500 text-xs font-medium">{error}</p>}
          {message && <p className="text-teal-600 dark:text-teal-400 text-xs font-medium">{message}</p>}
          
          <div className="flex justify-end mt-4">
            <button 
              type="submit" 
              disabled={isCreating || !username || !pin}
              className="bg-purple-600 hover:bg-purple-700 text-white px-4 py-2 rounded-lg text-sm font-medium transition-colors disabled:opacity-50 flex items-center"
            >
              {isCreating ? <Loader2 size={16} className="animate-spin mr-2" /> : <ShieldCheck size={16} className="mr-2" />}
              Create Staff
            </button>
          </div>
        </form>
      </div>
      <div className="mt-4 text-xs text-gray-500 dark:text-gray-400">
        * Staff accounts can add sales and manage inventory, but cannot access settings or financial reports.
      </div>
    </div>
  );
}
