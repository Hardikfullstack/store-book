'use client';

import { useState } from 'react';
import { Database, AlertTriangle, CheckCircle, Clock } from 'lucide-react';
import { archiveOldData } from '@/app/actions';

export default function DataCenterClient() {
  const [loading, setLoading] = useState(false);
  const [result, setResult] = useState<{ success: boolean; message: string } | null>(null);
  const [days, setDays] = useState(180); // Default 6 months

  const handleArchive = async () => {
    if (!confirm(`Are you absolutely sure you want to archive data older than ${days} days? This cannot be undone.`)) {
      return;
    }

    setLoading(true);
    setResult(null);

    try {
      const response = await archiveOldData(days);
      if (response.success) {
        setResult({ success: true, message: `Successfully archived ${response.count} records.` });
      } else {
        setResult({ success: false, message: response.error || 'Failed to archive data.' });
      }
    } catch (error: any) {
      setResult({ success: false, message: error.message || 'An unexpected error occurred.' });
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="space-y-6 max-w-4xl">
      <div className="flex justify-between items-center mb-6">
        <div>
          <h1 className="text-2xl font-bold text-gray-900 dark:text-white flex items-center gap-2">
            <Database className="text-teal-500" />
            Data Center
          </h1>
          <p className="text-gray-500 dark:text-gray-400 text-sm mt-1">Manage platform-wide database performance and archival policies.</p>
        </div>
      </div>

      {result && (
        <div className={`p-4 rounded-xl flex items-center gap-3 border ${result.success ? 'bg-emerald-50 border-emerald-200 text-emerald-800 dark:bg-emerald-900/30 dark:border-emerald-800 dark:text-emerald-400' : 'bg-red-50 border-red-200 text-red-800 dark:bg-red-900/30 dark:border-red-800 dark:text-red-400'}`}>
          {result.success ? <CheckCircle size={20} /> : <AlertTriangle size={20} />}
          <p className="font-medium">{result.message}</p>
        </div>
      )}

      <div className="glass-card overflow-hidden border border-gray-200 dark:border-gray-800">
        <div className="p-6 border-b border-gray-100 dark:border-gray-800 bg-gray-50/50 dark:bg-gray-900/50">
          <h2 className="text-lg font-semibold text-gray-900 dark:text-white flex items-center gap-2">
            <Clock className="text-orange-500" size={20} />
            Data Archival System
          </h2>
          <p className="text-sm text-gray-600 dark:text-gray-400 mt-2">
            Archiving old data improves sync speeds for mobile clients and reduces Firebase payload sizes. Archived data is excluded from regular queries.
          </p>
        </div>

        <div className="p-6 space-y-6">
          <div className="flex items-start gap-4 p-4 bg-orange-50 dark:bg-orange-900/20 rounded-xl border border-orange-100 dark:border-orange-800/50">
            <AlertTriangle className="text-orange-600 dark:text-orange-500 shrink-0 mt-0.5" size={20} />
            <div className="text-sm text-orange-800 dark:text-orange-400">
              <strong className="block mb-1">Warning: Destructive Action</strong>
              Triggering this process will scan all Stores' Sales, Udhaar, and Expenses collections. Any document older than the selected timeframe will be flagged as archived.
            </div>
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
              Archive records older than:
            </label>
            <div className="flex items-center gap-4">
              <select 
                value={days} 
                onChange={(e) => setDays(Number(e.target.value))}
                className="w-48 p-3 bg-white dark:bg-gray-800 border border-gray-200 dark:border-gray-700 rounded-xl text-gray-900 dark:text-white focus:ring-2 focus:ring-teal-500 transition-shadow"
              >
                <option value={30}>30 Days</option>
                <option value={90}>90 Days (3 Months)</option>
                <option value={180}>180 Days (6 Months)</option>
                <option value={365}>365 Days (1 Year)</option>
              </select>
            </div>
          </div>

          <div className="pt-4 border-t border-gray-100 dark:border-gray-800">
            <button
              onClick={handleArchive}
              disabled={loading}
              className="btn-primary bg-red-600 hover:bg-red-700 shadow-red-600/30 flex items-center justify-center gap-2 min-w-[200px]"
            >
              {loading ? (
                <span className="w-5 h-5 border-2 border-white/30 border-t-white rounded-full animate-spin"></span>
              ) : (
                <>
                  <Database size={18} />
                  <span>Execute Archival</span>
                </>
              )}
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}
