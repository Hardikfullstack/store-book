import { getSession } from '@/lib/session';
import AdminDashboardClient from './AdminDashboardClient';
import { getStoresPaginated, getUsersPaginated } from '@/app/actions';
import { redirect } from 'next/navigation';

export default async function AdminPage() {
  const session = await getSession();
  
  if (!session || (session.role !== 'admin' && session.role !== 'super_admin')) {
    redirect('/');
  }

  // In a real implementation, we would query DataConnect for analytics here.
  // For the MVP, we will pass some aggregate counts from our existing paginated methods, 
  // or the client component can fetch them.
  const stores = await getStoresPaginated(undefined, 100);
  const users = await getUsersPaginated(undefined, 100);

  return <AdminDashboardClient stores={stores} users={users} />;
}
