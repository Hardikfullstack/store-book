import { getSession } from '@/lib/session';
import { Store, User, ShieldCheck, Crown } from 'lucide-react';
import { redirect } from 'next/navigation';
import SubscriptionButton from './SubscriptionButton';
import ManageSubscription from './ManageSubscription';
import StaffManagement from './StaffManagement';
import { resolvePermissions } from '@/lib/roleMatrix';

export default async function SettingsPage() {
  const session = await getSession();
  if (!session) redirect('/login');
  const perms = resolvePermissions(session.role ?? 'staff');
  if (!perms.canViewSettings) {
    redirect('/')
  }


  let storeData = null;
  if (session.storeId) {
    storeData = { name: 'Migrated Store', is_premium: session.isPremium, subscription_platform: 'web' };
  }

  const userData = session;

  return (
    <div className="space-y-8 max-w-4xl">
      <div>
        <h1 className="text-2xl font-bold text-gray-900 dark:text-white">Settings & Profile</h1>
        <p className="text-gray-500 dark:text-gray-400 text-sm mt-1">Manage your account and business details.</p>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        {/* User Profile */}
        <div className="glass-card p-6">
          <div className="flex items-center space-x-3 mb-6 border-b border-gray-100 dark:border-gray-800 pb-4">
            <div className="p-3 rounded-xl bg-blue-50 dark:bg-blue-900/30 text-blue-600 dark:text-blue-400">
              <User size={24} />
            </div>
            <div>
              <h2 className="text-lg font-bold text-gray-900 dark:text-white">User Profile</h2>
              <p className="text-sm text-gray-500 dark:text-gray-400">Your personal details</p>
            </div>
          </div>
          
          <div className="space-y-4">
            <div>
              <label className="text-xs font-semibold text-gray-500 uppercase tracking-wider">Phone Number</label>
              <div className="mt-1 text-gray-900 dark:text-white font-medium">{session.phone || 'N/A'}</div>
            </div>
            <div>
              <label className="text-xs font-semibold text-gray-500 uppercase tracking-wider">System Role</label>
              <div className="mt-2 flex items-center space-x-2">
                <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-gray-100 text-gray-800 dark:bg-gray-800 dark:text-gray-200">
                  <ShieldCheck size={14} className="mr-1" />
                  {session.role?.toUpperCase()}
                </span>
              </div>
            </div>
          </div>
        </div>

        {/* Business Profile */}
        <div className="glass-card p-6">
          <div className="flex items-center space-x-3 mb-6 border-b border-gray-100 dark:border-gray-800 pb-4">
            <div className="p-3 rounded-xl bg-teal-50 dark:bg-teal-900/30 text-teal-600 dark:text-teal-400">
              <Store size={24} />
            </div>
            <div>
              <h2 className="text-lg font-bold text-gray-900 dark:text-white">Business Details</h2>
              <p className="text-sm text-gray-500 dark:text-gray-400">Store configuration</p>
            </div>
          </div>
          
          <div className="space-y-4">
            <div>
              <label className="text-xs font-semibold text-gray-500 uppercase tracking-wider">Store ID</label>
              <div className="mt-1 text-gray-900 dark:text-white font-mono text-sm">{session.storeId || 'Not Assigned'}</div>
            </div>
            {storeData && (
              <>
                <div>
                  <label className="text-xs font-semibold text-gray-500 uppercase tracking-wider">Store Name</label>
                  <div className="mt-1 text-gray-900 dark:text-white font-medium">{storeData.name || 'Unnamed Store'}</div>
                </div>
                <div>
                  <label className="text-xs font-semibold text-gray-500 uppercase tracking-wider">Subscription Status</label>
                  <div className="mt-2 flex items-center space-x-2">
                    {storeData.is_premium ? (
                      <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-amber-100 text-amber-800 dark:bg-amber-900/30 dark:text-amber-400">
                        <Crown size={14} className="mr-1" />
                        PRO PLAN ACTIVE
                      </span>
                    ) : (
                      <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-gray-100 text-gray-800 dark:bg-gray-800 dark:text-gray-300">
                        FREE PLAN
                      </span>
                    )}
                  </div>
                  {storeData.is_premium ? (
                    <ManageSubscription platform={storeData.subscription_platform || 'web'} />
                  ) : (
                    <SubscriptionButton />
                  )}
                </div>
              </>
            )}
          </div>
        </div>
      </div>
      
      {perms.canManageStaff && session.storeId && (
        <StaffManagement storeId={session.storeId} />
      )}
      
      <div className="glass-card p-6 text-sm text-gray-500 dark:text-gray-400 bg-gray-50 dark:bg-gray-900/50 mt-6">
        Note: Store Context Switching feature is coming soon. Currently, each user account is mapped to a single primary store.
      </div>
    </div>
  );
}
