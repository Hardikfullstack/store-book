import { getUsersPaginated, getStoresPaginated } from '@/app/actions';
import UsersClient from './UsersClient';
import { requirePermission } from '@/lib/permissions';

type UserRow = {
  id: string;
  role: string;
  storeId?: string | null;
};

type StoreRow = {
  id: string;
  name: string;
};

export default async function UsersPage() {
  try {
    await requirePermission('canAccessAdmin');
  } catch {
    return <div className="p-8 text-red-500">Unauthorized. Admin access required.</div>;
  }

  // Load first page of users Server-Side
  const initialUsers = await getUsersPaginated();
  // Fetch a list of stores to map users to them
  const initialStores = await getStoresPaginated(undefined, 100);

  return <UsersClient initialUsers={initialUsers as UserRow[]} availableStores={initialStores as StoreRow[]} />;
}
