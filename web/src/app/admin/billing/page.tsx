import { getStoresPaginated } from '@/app/actions';
import { redirect } from 'next/navigation';
import BillingClient from './BillingClient';
import { requirePermission } from '@/lib/permissions';

type StoreBillingRow = {
  id: string;
  name: string;
  isPremium?: boolean;
  subscriptionPlatform?: string;
  subscriptionExpiresAt?: string;
};

export default async function AdminBillingPage() {
  try {
    await requirePermission('canAccessAdmin');
  } catch {
    return redirect('/');
  }

  // Load all stores for billing overview
  const stores = (await getStoresPaginated(undefined, 100)) as StoreBillingRow[];

  return <BillingClient stores={stores} />;
}
