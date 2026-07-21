'use client';

import { useState } from 'react';
import { sanitizeInput } from '@/lib/sanitize';
import { Database, AlertTriangle, CheckCircle, Clock, Shield, Trash2 } from 'lucide-react';
import { archiveOldData } from '@/app/actions';

export default function DataClient({ initialAuditLogs }: { initialAuditLogs: any[] }) {
  const [loading, setLoading] = useState(false);
  const [result, setResult] = useState<{ success: boolean; message: string } | null>(null);
  const [days, setDays] = useState(180);

  const handleArchive = async () => {
    if (!confirm(`Are you absolutely sure you want to archive data older than ${days} days? This cannot be undone.`)) return;
    setLoading(true);
    setResult(null);
    try {
      const response = await archiveOldData(days);
      setResult({ success: response.success, message: response.success ? `Successfully archived ${response.count} records.` : response.error || 'Failed to archive data.' });
    } catch (error: any) {
      setResult({ success: false, message: error.message || 'Error occurred.' });
    } finally {
      setLoading(false);
    }
  };

  const [purgeId, setPurgeId] = useState('');

  const handlePurge = async () => {
    if (!purgeId) return;
    if (!confirm(`WARNING: This will permanently delete Store ${purgeId} and all associated data. Type OK to continue.`)) return;
    setLoading(true);
    try {
      const { purgeStoreData } = await import('@/app/actions');
      const res = await purgeStoreData(purgeId);
      if (res.success) {
        alert("Store purged successfully and audit log created.");
        setPurgeId('');
      } else {
        alert("Purge failed: " + res.error);
      }
    } catch (e: any) {
      alert("Error: " + e.message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center mb-6">
        <div>
          <h1 className="text-2xl font-bold text-gray-900 dark:text-white flex items-center gap-2">
            <Database className="text-teal-500" />
            Data Center
          </h1>
          <p className="text-gray-500 dark:text-gray-400 text-sm mt-1">Manage platform-wide database performance, GDPR purges, and immutable audit logs.</p>
        </div>
      </div>

      {result && (
        <div className={`p-4 rounded-xl flex items-center gap-3 border ${result.success ? 'bg-emerald-50 border-emerald-200 text-emerald-800 dark:bg-emerald-900/30 dark:border-emerald-800 dark:text-emerald-400' : 'bg-red-50 border-red-200 text-red-800 dark:bg-red-900/30 dark:border-red-800 dark:text-red-400'}`}>
          {result.success ? <CheckCircle size={20} /> : <AlertTriangle size={20} />}
          <p className="font-medium">{result.message}</p>
        </div>
      )}

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Archival & Purge */}
        <div className="space-y-6">
          <div className="glass-card overflow-hidden border border-gray-200 dark:border-gray-800 rounded-xl">
            <div className="p-6 border-b border-gray-100 dark:border-gray-800 bg-gray-50/50 dark:bg-gray-900/50">
              <h2 className="text-lg font-semibold text-gray-900 dark:text-white flex items-center gap-2">
                <Clock className="text-orange-500" size={20} />
                Data Archival System
              </h2>
            </div>
            <div className="p-6 space-y-6">
              <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">Archive records older than:</label>
              <select value={days} onChange={(e) => setDays(Number(e.target.value))} className="w-full p-3 bg-white dark:bg-gray-800 border border-gray-200 dark:border-gray-700 rounded-xl">
                <option value={30}>30 Days</option>
                <option value={180}>180 Days (6 Months)</option>
              </select>
              <button onClick={handleArchive} disabled={loading} className="w-full py-3 bg-orange-600 hover:bg-orange-700 text-white font-bold rounded-xl flex items-center justify-center gap-2">
                <Database size={18} /> Execute Archival
              </button>
            </div>
          </div>

          <div className="glass-card overflow-hidden border border-red-200 dark:border-red-900/30 rounded-xl bg-red-50/10">
            <div className="p-6 border-b border-red-100 dark:border-red-900/30">
              <h2 className="text-lg font-semibold text-red-700 dark:text-red-500 flex items-center gap-2">
                <Trash2 size={20} /> GDPR Data Purge
              </h2>
            </div>
            <div className="p-6">
              <p className="text-sm text-gray-600 dark:text-gray-400 mb-4">Permanently delete a store and all associated data to comply with &quot;Right to be Forgotten&quot; requests. This action is irreversible.</p>
              <div className="flex gap-2">
                <input aria-label="text" type="text" value={purgeId} onChange={(e) => setPurgeId(sanitizeInput(e.target.value))} placeholder="Store ID to Purge" className="flex-1 p-2 border rounded-lg dark:bg-gray-800 dark:border-gray-700 dark:text-white" />
                <button onClick={handlePurge} disabled={loading} className="px-4 bg-red-600 text-white rounded-lg font-medium hover:bg-red-700">Purge</button>
              </div>
            </div>
          </div>
        </div>

        {/* Audit Log */}
        <div className="glass-card overflow-hidden border border-gray-200 dark:border-gray-800 rounded-xl flex flex-col">
          <div className="p-6 border-b border-gray-100 dark:border-gray-800 bg-gray-50/50 dark:bg-gray-900/50">
            <h2 className="text-lg font-semibold text-gray-900 dark:text-white flex items-center gap-2">
              <Shield className="text-teal-500" size={20} />
              Immutable Admin Audit Log
            </h2>
          </div>
          <div className="flex-1 p-0 overflow-y-auto max-h-[600px]">
            {initialAuditLogs.length === 0 ? (
              <div className="p-8 text-center text-gray-500 text-sm">No audit logs found. System actions will be recorded here.</div>
            ) : (
              <table className="w-full text-left text-sm">
                <tbody className="divide-y divide-gray-100 dark:divide-gray-800">
                  {initialAuditLogs.map((log: any) => (
                    <tr key={log.id} className="hover:bg-gray-50/50 dark:hover:bg-gray-800/50">
                      <td className="p-4">
                        <span className="font-bold text-gray-900 dark:text-white">{log.adminUsername || log.adminId}</span>
                        <p className="text-xs text-gray-500 mt-1">{log.action} on {log.targetId}</p>
                      </td>
                      <td className="p-4 text-right text-xs text-gray-400">
                        {new Date(log.timestamp).toLocaleString()}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}
