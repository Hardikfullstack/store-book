import { getUsersPaginated, getStoresPaginated } from '@/app/actions';
import UsersClient from './UsersClient';
import { getSession } from '@/lib/session';

export default async function UsersPage() {
  const session = await getSession();
  if (!session || session.role !== 'admin') {
    return <div className="p-8 text-red-500">Unauthorized. Admin access required.</div>;
  }

  // Load first page of users Server-Side
  const initialUsers = await getUsersPaginated();
  // Fetch a list of stores to map users to them
  const initialStores = await getStoresPaginated(undefined, 100);

  return <UsersClient initialUsers={initialUsers} availableStores={initialStores} />;
}
