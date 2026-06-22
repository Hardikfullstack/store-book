'use client';

import { useState, useEffect } from 'react';
import { Plus, Search, UserCheck, UserMinus, Trash2, Loader2, ArrowDownCircle, MessageCircle } from 'lucide-react';
import { addUdhaar, deleteUdhaar, fetchMoreData } from '@/app/actions';
import { db } from '@/lib/firebase';
import { collection, onSnapshot } from 'firebase/firestore';

export default function UdhaarClient({ 
  initialUdhaar, 
  storeName,
  storeId,
  isPremium
}: { 
  initialUdhaar: any[], 
  storeName?: string,
  storeId?: string,
  isPremium?: boolean
}) {
  const [udhaar, setUdhaar] = useState(initialUdhaar);

  useEffect(() => {
    if (!isPremium || !storeId) return;

    const q = collection(db, 'stores', storeId, 'udhaar');
    const unsubscribe = onSnapshot(q, (snapshot) => {
      const updated = snapshot.docs
        .map(doc => ({ id: doc.id, ...doc.data() } as any))
        .filter(record => record.is_deleted !== 1);

      // Sort by updated_at or timestamp desc in memory
      updated.sort((a, b) => (b.updated_at || b.timestamp || 0) - (a.updated_at || a.timestamp || 0));

      setUdhaar(updated);
    }, (error) => {
      console.error("Real-time udhaar sync error:", error);
    });

    return () => unsubscribe();
  }, [isPremium, storeId]);
  const [showModal, setShowModal] = useState(false);
  const [searchQuery, setSearchQuery] = useState('');
  const [loadingMore, setLoadingMore] = useState(false);
  const [hasMore, setHasMore] = useState(initialUdhaar.length === 20);

  const handleLoadMore = async () => {
    if (loadingMore || !hasMore || udhaar.length === 0) return;
    setLoadingMore(true);
    try {
      const lastItem = udhaar[udhaar.length - 1];
      const nextBatch = await fetchMoreData('udhaar', lastItem.updated_at || lastItem.timestamp || Date.now(), 20);
      if (nextBatch.length < 20) {
        setHasMore(false);
      }
      setUdhaar(prev => [...prev, ...nextBatch]);
    } catch (error) {
      console.error("Load more failed", error);
    } finally {
      setLoadingMore(false);
    }
  };
  
  const [formData, setFormData] = useState({
    customer_name: '',
    type: 'given',
    amount: 0,
    notes: ''
  });

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    await addUdhaar(formData);
    setShowModal(false);
    window.location.reload(); 
  };

  const handleDelete = async (id: string) => {
    if (confirm('Are you sure you want to delete this record?')) {
      await deleteUdhaar(id);
      window.location.reload();
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center mb-6">
        <div>
          <h1 className="text-2xl font-bold text-gray-900 dark:text-white">Udhaar Ledger</h1>
          <p className="text-gray-500 dark:text-gray-400 text-sm mt-1">Track credit given and taken.</p>
        </div>
        <button 
          onClick={() => { setFormData({customer_name:'',type:'given',amount:0,notes:''}); setShowModal(true); }}
          className="btn-primary flex items-center space-x-2 bg-blue-600 hover:bg-blue-700 shadow-blue-600/30"
        >
          <Plus size={18} />
          <span>New Entry</span>
        </button>
      </div>

      <div className="glass-card overflow-hidden">
        <div className="p-4 border-b border-gray-100 dark:border-gray-800 flex justify-between items-center bg-gray-50/50 dark:bg-gray-900/50">
          <div className="relative w-64">
            <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
              <Search size={16} className="text-gray-400" />
            </div>
            <input 
              type="text" 
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              className="block w-full pl-10 pr-3 py-2 border border-gray-200 dark:border-gray-700 bg-white dark:bg-gray-800 rounded-lg text-sm placeholder-gray-400 dark:placeholder-gray-500 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all dark:text-gray-100"
              placeholder="Search customer or notes..."
            />
          </div>
        </div>

        <div className="overflow-x-auto">
          <table className="w-full text-left text-sm text-gray-600 dark:text-gray-300">
            <thead className="bg-gray-50/50 dark:bg-gray-900/50 text-gray-500 dark:text-gray-400 text-xs uppercase tracking-wider border-b border-gray-100 dark:border-gray-800">
              <tr>
                <th className="px-6 py-4 font-medium">Date</th>
                <th className="px-6 py-4 font-medium">Customer Name</th>
                <th className="px-6 py-4 font-medium">Type</th>
                <th className="px-6 py-4 font-medium">Notes</th>
                <th className="px-6 py-4 font-medium text-right">Amount</th>
                <th className="px-6 py-4 font-medium text-right">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-50 dark:divide-gray-800">
              {(() => {
                const filteredUdhaar = udhaar.filter((record: any) => {
                  return (
                    (record.customer_name || '').toLowerCase().includes(searchQuery.toLowerCase()) || 
                    (record.notes || '').toLowerCase().includes(searchQuery.toLowerCase())
                  );
                });

                if (filteredUdhaar.length === 0) {
                  return (
                    <tr>
                      <td colSpan={6} className="px-6 py-8 text-center text-gray-500 dark:text-gray-400">
                        No udhaar records found matching your search.
                      </td>
                    </tr>
                  );
                }

                return (
                  <>
                    {filteredUdhaar.map((record: any) => (
                      <tr key={record.id} className="hover:bg-gray-50/50 dark:hover:bg-gray-800/50 transition-colors">
                        <td className="px-6 py-4 whitespace-nowrap">
                          {new Date(record.timestamp || record.updated_at).toLocaleDateString('en-IN')}
                        </td>
                  <td className="px-6 py-4 font-medium text-gray-900 dark:text-gray-100">{record.customer_name}</td>
                  <td className="px-6 py-4">
                    {record.type?.toLowerCase() === 'given' ? (
                      <span className="inline-flex items-center px-2 py-1 rounded-md text-xs font-medium bg-red-50 dark:bg-red-900/30 text-red-700 dark:text-red-400">
                        <UserMinus size={14} className="mr-1" /> Given
                      </span>
                    ) : (
                      <span className="inline-flex items-center px-2 py-1 rounded-md text-xs font-medium bg-teal-50 dark:bg-teal-900/30 text-teal-700 dark:text-teal-400">
                        <UserCheck size={14} className="mr-1" /> Received
                      </span>
                    )}
                  </td>
                  <td className="px-6 py-4 text-gray-500 dark:text-gray-400 truncate max-w-xs" title={record.notes}>
                    {record.notes || '-'}
                  </td>
                  <td className={`px-6 py-4 text-right font-bold ${record.type?.toLowerCase() === 'given' ? 'text-red-600 dark:text-red-400' : 'text-teal-600 dark:text-teal-400'}`}>
                    ₹{record.amount}
                  </td>
                  <td className="px-6 py-4 text-right space-x-3">
                    <button 
                      onClick={() => {
                        const text = encodeURIComponent(storeName ? `Hi ${record.customer_name}, this is a reminder regarding your pending Udhaar balance of Rs. ${record.amount} at ${storeName}. Please clear your dues at the earliest. Thank you!` : `Hi ${record.customer_name}, this is a reminder regarding your pending Udhaar balance of Rs. ${record.amount}. Please clear your dues at the earliest. Thank you!`);
                        window.open(`https://wa.me/?text=${text}`, '_blank');
                      }} 
                      className="text-green-500 hover:text-green-700 transition-colors" 
                      title="Send WhatsApp Reminder"
                    >
                      <MessageCircle size={16} />
                    </button>
                    <button onClick={() => handleDelete(record.id)} className="text-red-500 hover:text-red-700 transition-colors"><Trash2 size={16} /></button>
                  </td>
                </tr>
                    ))}
                  </>
                );
              })()}
            </tbody>
          </table>
        </div>
        
        {hasMore && !searchQuery && (
          <div className="p-4 border-t border-gray-100 dark:border-gray-800 flex justify-center">
            <button 
              onClick={handleLoadMore} 
              disabled={loadingMore}
              className="px-6 py-2 bg-gray-100 dark:bg-gray-800 text-gray-700 dark:text-gray-300 rounded-xl text-sm font-medium hover:bg-gray-200 dark:hover:bg-gray-700 transition-colors flex items-center gap-2"
            >
              {loadingMore ? <Loader2 size={16} className="animate-spin" /> : <ArrowDownCircle size={16} />}
              Load More from Server
            </button>
          </div>
        )}
      </div>

      {showModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 backdrop-blur-sm">
          <div className="bg-white dark:bg-gray-900 rounded-xl shadow-xl w-full max-w-md p-6">
            <h2 className="text-xl font-bold mb-4 dark:text-white">New Udhaar</h2>
            <form onSubmit={handleSubmit} className="space-y-4">
              <div>
                <label className="block text-sm font-medium dark:text-gray-300">Customer Name</label>
                <input required type="text" value={formData.customer_name} onChange={e => setFormData({...formData, customer_name: e.target.value})} className="mt-1 w-full p-2 border dark:border-gray-700 rounded dark:bg-gray-800 dark:text-white" />
              </div>
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-medium dark:text-gray-300">Type</label>
                  <select value={formData.type} onChange={e => setFormData({...formData, type: e.target.value})} className="mt-1 w-full p-2 border dark:border-gray-700 rounded dark:bg-gray-800 dark:text-white">
                    <option value="given">Given</option>
                    <option value="received">Received</option>
                  </select>
                </div>
                <div>
                  <label className="block text-sm font-medium dark:text-gray-300">Amount</label>
                  <input required type="number" step="any" value={formData.amount} onChange={e => setFormData({...formData, amount: parseFloat(e.target.value)})} className="mt-1 w-full p-2 border dark:border-gray-700 rounded dark:bg-gray-800 dark:text-white" />
                </div>
              </div>
              <div>
                <label className="block text-sm font-medium dark:text-gray-300">Notes</label>
                <input type="text" value={formData.notes} onChange={e => setFormData({...formData, notes: e.target.value})} className="mt-1 w-full p-2 border dark:border-gray-700 rounded dark:bg-gray-800 dark:text-white" />
              </div>
              <div className="flex justify-end space-x-3 mt-6">
                <button type="button" onClick={() => setShowModal(false)} className="px-4 py-2 text-gray-500 hover:text-gray-700 dark:text-gray-400 dark:hover:text-gray-200">Cancel</button>
                <button type="submit" className="btn-primary bg-blue-600 hover:bg-blue-700">Save</button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
