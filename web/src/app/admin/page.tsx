import AdminDashboardClient from './AdminDashboardClient';
import { getStoresPaginated, getUsersPaginated } from '@/app/actions';
import { redirect } from 'next/navigation';
import { requirePermission } from '@/lib/permissions';

export default async function AdminPage() {
  let session;
  try {
    session = await requirePermission('canAccessAdmin');
  } catch {
    return redirect('/');
  }

  // In a real implementation, we would query DataConnect for analytics here.
  // For the MVP, we will pass some aggregate counts from our existing paginated methods, 
  // or the client component can fetch them.
  const stores = await getStoresPaginated(undefined, 100);
  const users = await getUsersPaginated(undefined, 100);

  return <AdminDashboardClient stores={stores} users={users} />;
}
