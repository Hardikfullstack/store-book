'use client';

import { useState } from 'react';
import { Users, Loader2, Save } from 'lucide-react';
import { getUsersPaginated, updateUserRole } from '@/app/actions';

export default function UsersClient({ initialUsers, availableStores }: { initialUsers: any[], availableStores: any[] }) {
  const [users, setUsers] = useState(initialUsers);
  const [loadingMore, setLoadingMore] = useState(false);
  const [hasMore, setHasMore] = useState(initialUsers.length === 20);
  const [savingId, setSavingId] = useState<string | null>(null);

  const loadMore = async () => {
    if (loadingMore || !hasMore) return;
    setLoadingMore(true);
    try {
      const lastId = users[users.length - 1]?.id;
      const nextBatch = await getUsersPaginated(lastId);
      if (nextBatch.length < 20) {
        setHasMore(false);
      }
      setUsers(prev => [...prev, ...nextBatch]);
    } catch (error) {
      console.error("Failed to load more users:", error);
    } finally {
      setLoadingMore(false);
    }
  };

  const handleRoleChange = (userId: string, newRole: string) => {
    setUsers(prev => prev.map(u => u.id === userId ? { ...u, role: newRole } : u));
  };

  const handleStoreChange = (userId: string, newStoreId: string) => {
    setUsers(prev => prev.map(u => u.id === userId ? { ...u, storeId: newStoreId === 'none' ? null : newStoreId } : u));
  };

  const saveUser = async (user: any) => {
    setSavingId(user.id);
    try {
      await updateUserRole(user.id, user.role, user.storeId);
      alert(`Successfully updated user ${user.id}`);
    } catch (error) {
      console.error("Save failed:", error);
      alert("Failed to update user.");
    } finally {
      setSavingId(null);
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center mb-6">
        <div>
          <h1 className="text-2xl font-bold text-gray-900 dark:text-white flex items-center gap-2">
            <Users className="text-blue-500" />
            Manage Users
          </h1>
          <p className="text-gray-500 dark:text-gray-400 text-sm mt-1">Assign roles and map clients to specific stores.</p>
        </div>
      </div>

      <div className="glass-card overflow-hidden border border-gray-200 dark:border-gray-800">
        <div className="overflow-x-auto">
          <table className="w-full text-left border-collapse">
            <thead>
              <tr className="bg-gray-50/50 dark:bg-gray-900/50 border-b border-gray-100 dark:border-gray-800">
                <th className="p-4 text-sm font-semibold text-gray-600 dark:text-gray-400">Mobile Number</th>
                <th className="p-4 text-sm font-semibold text-gray-600 dark:text-gray-400">Role</th>
                <th className="p-4 text-sm font-semibold text-gray-600 dark:text-gray-400">Assigned Store</th>
                <th className="p-4 text-sm font-semibold text-gray-600 dark:text-gray-400 text-right">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-100 dark:divide-gray-800">
              {users.map((user) => (
                <tr key={user.id} className="hover:bg-gray-50/30 dark:hover:bg-gray-800/30 transition-colors">
                  <td className="p-4 text-sm text-gray-900 dark:text-gray-100 font-medium font-mono">
                    {user.id}
                  </td>
                  <td className="p-4">
                    <select 
                      value={user.role || 'client'} 
                      onChange={(e) => handleRoleChange(user.id, e.target.value)}
                      disabled={user.id === '+919999999999'}
                      className="text-sm p-1.5 bg-white dark:bg-gray-800 border border-gray-200 dark:border-gray-700 rounded-lg text-gray-900 dark:text-white disabled:opacity-50"
                    >
                      <option value="client">Client</option>
                      <option value="admin">Admin</option>
                    </select>
                  </td>
                  <td className="p-4">
                    <select 
                      value={user.storeId || 'none'} 
                      onChange={(e) => handleStoreChange(user.id, e.target.value)}
                      disabled={user.role === 'admin' || user.id === '+919999999999'}
                      className="text-sm p-1.5 w-full max-w-[200px] bg-white dark:bg-gray-800 border border-gray-200 dark:border-gray-700 rounded-lg text-gray-900 dark:text-white disabled:opacity-50"
                    >
                      <option value="none">No Store Assigned</option>
                      {availableStores.map(store => (
                        <option key={store.id} value={store.id}>{store.name} ({store.id})</option>
                      ))}
                    </select>
                  </td>
                  <td className="p-4 flex items-center justify-end gap-2">
                    {user.id === '+919999999999' ? (
                      <span className="text-xs font-semibold text-gray-400 uppercase tracking-wider px-2">Root Admin</span>
                    ) : (
                      <>
                        <button 
                          onClick={async () => {
                            if (!confirm(`Revoke all sessions for ${user.id}?`)) return;
                            try {
                              const { revokeUserSessions } = await import('@/app/actions');
                              const res = await revokeUserSessions(user.id);
                              if (res.success) alert("Sessions revoked successfully.");
                              else alert("Failed to revoke: " + res.error);
                            } catch (e: any) {
                              alert("Error: " + e.message);
                            }
                          }}
                          className="p-2 bg-red-50 dark:bg-red-900/20 text-red-600 dark:text-red-400 rounded-lg hover:bg-red-100 dark:hover:bg-red-900/40 transition-colors flex items-center gap-2 text-sm font-medium"
                          title="Force Session Revoke"
                        >
                          Revoke
                        </button>
                        <button 
                          onClick={() => saveUser(user)}
                          disabled={savingId === user.id}
                          className="p-2 bg-teal-50 dark:bg-teal-900/20 text-teal-600 dark:text-teal-400 rounded-lg hover:bg-teal-100 dark:hover:bg-teal-900/40 transition-colors disabled:opacity-50 flex items-center gap-2 text-sm font-medium"
                        >
                          {savingId === user.id ? <Loader2 size={16} className="animate-spin" /> : <Save size={16} />}
                          Save
                        </button>
                      </>
                    )}
                  </td>
                </tr>
              ))}
              {users.length === 0 && (
                <tr>
                  <td colSpan={4} className="p-8 text-center text-gray-500 dark:text-gray-400">
                    No users found.
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
        
        {hasMore && (
          <div className="p-4 border-t border-gray-100 dark:border-gray-800 flex justify-center">
            <button 
              onClick={loadMore} 
              disabled={loadingMore}
              className="px-6 py-2 bg-gray-100 dark:bg-gray-800 text-gray-700 dark:text-gray-300 rounded-xl text-sm font-medium hover:bg-gray-200 dark:hover:bg-gray-700 transition-colors flex items-center gap-2"
            >
              {loadingMore ? <Loader2 size={16} className="animate-spin" /> : null}
              Load More Users
            </button>
          </div>
        )}
      </div>
    </div>
  );
}
