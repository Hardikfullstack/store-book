'use client';

import { useState, useEffect } from 'react';
import { Plus, Search, Tag, Trash2, Loader2, ArrowDownCircle } from 'lucide-react';
import { fetchMoreData } from '@/app/actions';
import { dataConnect } from '@/lib/firebase';
import { sanitizeInput } from '@/lib/sanitize';
import { getActiveExpenses, syncExpense, softDeleteExpense } from '@/dataconnect';
import { FormattedAmount } from '@/components/FormattedAmount';
import Pagination from '@/app/components/Pagination';

export default function ExpensesClient({
  initialExpenses,
  storeId,
  isPremium
}: {
  initialExpenses: any[],
  storeId?: string,
  isPremium?: boolean
}) {
  const [expenses, setExpenses] = useState(initialExpenses);
  const [currentPage, setCurrentPage] = useState(1);
  const pageSize = 10;
  const [totalItems, setTotalItems] = useState(0);
  const [isLoading, setIsLoading] = useState(false);

  useEffect(() => {
    if (!isPremium || !storeId) return;
    // Fetch total count once
    const fetchTotal = async () => {
      try {
        const resp = await getActiveExpenses(dataConnect, { storeId });
        const filtered = resp.data.expenseEntries;
        setTotalItems(filtered.length);
      } catch (e) {
        console.error('Count fetch error:', e);
      }
    };
    fetchTotal();
  }, [isPremium, storeId]);

  useEffect(() => {
    if (!isPremium || !storeId) return;

    let isMounted = true;
    const fetchExpenses = async () => {
      setIsLoading(true);
      try {
        const offset = (currentPage - 1) * pageSize;
        const response = await getActiveExpenses(dataConnect, {
          storeId,
          limit: pageSize,
          offset
        });
        if (!isMounted) return;

        const updated = response.data.expenseEntries.map((record: any) => ({
          ...record,
          is_deleted: 0,
          updated_at: record.updatedAt || Date.now(),
          supplier_name: record.supplierName
        }));

        setExpenses(updated);
      } catch (error) {
        console.error("Data Connect expense sync error:", error);
      } finally {
        if (isMounted) setIsLoading(false);
      }
    };

    fetchExpenses();
    const intervalId = setInterval(fetchExpenses, 30000);

    return () => {
      isMounted = false;
      clearInterval(intervalId);
    };
  }, [isPremium, storeId, currentPage]);
  const [showModal, setShowModal] = useState(false);
  const [searchQuery, setSearchQuery] = useState('');
  const [formData, setFormData] = useState({
    type: 'supplies',
    description: '',
    amount: 0,
    supplier_name: ''
  });

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      const id = crypto.randomUUID();
      const now = Date.now();
      await syncExpense(dataConnect, {
        id,
        storeId: storeId as string,
        type: formData.type,
        description: formData.description,
        amount: formData.amount,
        timestamp: now,
        supplierName: formData.supplier_name,
        isDeleted: false,
        updatedAt: Math.floor(now / 1000)
      });
      setShowModal(false);
      window.location.reload();
    } catch (err) {
      console.error("Failed to save expense:", err);
    }
  };

  const handleDelete = async (id: string) => {
    if (confirm('Are you sure you want to delete this expense?')) {
      try {
        await softDeleteExpense(dataConnect, { id, updatedAt: Math.floor(Date.now() / 1000) });
        window.location.reload();
      } catch (err) {
        console.error("Failed to delete expense:", err);
      }
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center mb-6">
        <div>
          <h1 className="text-2xl font-bold text-gray-900 dark:text-white">Expenses Tracker</h1>
          <p className="text-gray-500 dark:text-gray-400 text-sm mt-1">Keep an eye on your business spending.</p>
        </div>
        <button
          onClick={() => { setFormData({ type: 'supplies', description: '', amount: 0, supplier_name: '' }); setShowModal(true); }}
          className="btn-primary flex items-center space-x-2 bg-orange-600 hover:bg-orange-700 shadow-orange-600/30"
        >
          <Plus size={18} />
          <span>Add Expense</span>
        </button>
      </div>

      <div className="glass-card overflow-hidden">
        <div className="p-4 border-b border-gray-100 dark:border-gray-800 flex justify-between items-center bg-gray-50/50 dark:bg-gray-900/50">
          <div className="relative w-64">
            <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
              <Search size={16} className="text-gray-400" />
            </div>
            <input aria-label="text"
              type="text"
              value={searchQuery}
              onChange={(e) => setSearchQuery(sanitizeInput(e.target.value))}
              className="block w-full pl-10 pr-3 py-2 border border-gray-200 dark:border-gray-700 bg-white dark:bg-gray-800 rounded-lg text-sm placeholder-gray-400 dark:placeholder-gray-500 focus:outline-none focus:ring-2 focus:ring-orange-500 focus:border-transparent transition-all dark:text-gray-100"
              placeholder="Search expenses by description, type or supplier..."
            />
          </div>
          <button className="flex items-center space-x-2 text-sm text-gray-600 dark:text-gray-300 bg-white dark:bg-gray-800 border border-gray-200 dark:border-gray-700 px-3 py-2 rounded-lg hover:bg-gray-50 dark:hover:bg-gray-700 transition-colors">
            <Tag size={16} />
            <span>Category</span>
          </button>
        </div>

        <div className="overflow-x-auto">
          <table className="w-full text-left text-sm text-gray-600 dark:text-gray-300">
            <thead className="bg-gray-50/50 dark:bg-gray-900/50 text-gray-500 dark:text-gray-400 text-xs uppercase tracking-wider border-b border-gray-100 dark:border-gray-800">
              <tr>
                <th className="px-6 py-4 font-medium">Date</th>
                <th className="px-6 py-4 font-medium">Type</th>
                <th className="px-6 py-4 font-medium">Description</th>
                <th className="px-6 py-4 font-medium">Supplier</th>
                <th className="px-6 py-4 font-medium text-right">Amount</th>
                <th className="px-6 py-4 font-medium text-right">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-50 dark:divide-gray-800">
              {isLoading ? (
                <tr>
                  <td colSpan={7} className="px-6 py-12 text-center">
                    <div className="flex items-center justify-center space-x-2 text-gray-400 dark:text-gray-500">
                      <Loader2 size={20} className="animate-spin" />
                      <span className="text-sm">Loading expenses...</span>
                    </div>
                  </td>
                </tr>
              ) : (() => {
                const filteredExpenses = expenses.filter((expense: any) => {
                  return (
                    (expense.description || '').toLowerCase().includes(searchQuery.toLowerCase()) ||
                    (expense.type || '').toLowerCase().includes(searchQuery.toLowerCase()) ||
                    (expense.supplier_name || '').toLowerCase().includes(searchQuery.toLowerCase())
                  );
                });

                if (filteredExpenses.length === 0) {
                  return (
                    <tr>
                      <td colSpan={6} className="px-6 py-8 text-center text-gray-500 dark:text-gray-400">
                        No expenses found matching your search.
                      </td>
                    </tr>
                  );
                }

                return (
                  <>
                    {filteredExpenses.map((expense: any) => (
                      <tr key={expense.id} className="hover:bg-gray-50/50 dark:hover:bg-gray-800/50 transition-colors">
                        <td className="px-6 py-4 whitespace-nowrap">
                          {new Date(expense.timestamp || expense.updated_at).toLocaleDateString('en-IN')}
                        </td>
                        <td className="px-6 py-4">
                          <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-gray-100 dark:bg-gray-800 text-gray-800 dark:text-gray-300 border border-gray-200 dark:border-gray-700">
                            {expense.type}
                          </span>
                        </td>
                        <td className="px-6 py-4 text-gray-900 dark:text-gray-100">{expense.description}</td>
                        <td className="px-6 py-4 text-gray-500 dark:text-gray-400">{expense.supplier_name || '-'}</td>
                        <td className="px-6 py-4 text-right font-bold text-orange-600 dark:text-orange-400">
                          <FormattedAmount amount={expense.amount} />
                        </td>
                        <td className="px-6 py-4 text-right space-x-3">
                          <button onClick={() => handleDelete(expense.id)} className="text-red-500 hover:text-red-700 transition-colors"><Trash2 size={16} /></button>
                        </td>
                      </tr>
                    ))}
                  </>
                );
              })()}
            </tbody>
          </table>
        </div>
        <Pagination
          currentPage={currentPage}
          pageSize={pageSize}
          totalItems={totalItems}
          isLoading={isLoading}
          onPageChange={setCurrentPage}
        />
      </div>

      {showModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 backdrop-blur-sm">
          <div className="bg-white dark:bg-gray-900 rounded-xl shadow-xl w-full max-w-md p-6">
            <h2 className="text-xl font-bold mb-4 dark:text-white">Add Expense</h2>
            <form onSubmit={handleSubmit} className="space-y-4">
              <div>
                <label className="block text-sm font-medium dark:text-gray-300">Description</label>
                <input aria-label="text" required type="text" value={formData.description} onChange={e => setFormData({ ...formData, description: sanitizeInput(e.target.value) })} className="mt-1 w-full p-2 border dark:border-gray-700 rounded dark:bg-gray-800 dark:text-white" />
              </div>
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-medium dark:text-gray-300">Type/Category</label>
                  <input aria-label="text" type="text" value={formData.type} onChange={e => setFormData({ ...formData, type: sanitizeInput(e.target.value) })} className="mt-1 w-full p-2 border dark:border-gray-700 rounded dark:bg-gray-800 dark:text-white" />
                </div>
                <div>
                  <label className="block text-sm font-medium dark:text-gray-300">Amount</label>
                  <input aria-label="number" required type="number" step="any" value={formData.amount} onChange={e => setFormData({ ...formData, amount: parseFloat(e.target.value) })} className="mt-1 w-full p-2 border dark:border-gray-700 rounded dark:bg-gray-800 dark:text-white" />
                </div>
              </div>
              <div>
                <label className="block text-sm font-medium dark:text-gray-300">Supplier (Optional)</label>
                <input aria-label="text" type="text" value={formData.supplier_name} onChange={e => setFormData({ ...formData, supplier_name: sanitizeInput(e.target.value) })} className="mt-1 w-full p-2 border dark:border-gray-700 rounded dark:bg-gray-800 dark:text-white" />
              </div>
              <div className="flex justify-end space-x-3 mt-6">
                <button type="button" onClick={() => setShowModal(false)} className="px-4 py-2 text-gray-500 hover:text-gray-700 dark:text-gray-400 dark:hover:text-gray-200">Cancel</button>
                <button type="submit" className="btn-primary bg-orange-600 hover:bg-orange-700">Save</button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
