import { getSession } from '@/lib/session';
import { getStoresPaginated } from '@/app/actions';
import { redirect } from 'next/navigation';
import BillingClient from './BillingClient';

export default async function AdminBillingPage() {
  const session = await getSession();
  
  if (!session || (session.role !== 'admin' && session.role !== 'super_admin')) {
    redirect('/');
  }

  // Load all stores for billing overview
  const stores = await getStoresPaginated(undefined, 100);

  return <BillingClient stores={stores} />;
}
