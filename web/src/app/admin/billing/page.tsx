import { getStoresPaginated } from '@/app/actions';
import { redirect } from 'next/navigation';
import BillingClient from './BillingClient';
import { requirePermission } from '@/lib/permissions';

export default async function AdminBillingPage() {
  try {
    await requirePermission('canAccessAdmin');
  } catch {
    return redirect('/');
  }

  // Load all stores for billing overview
  const stores = await getStoresPaginated(undefined, 100);

  return <BillingClient stores={stores} />;
}
