import { getStoresPaginated } from '@/app/actions';
import StoresClient from './StoresClient';
import { requirePermission } from '@/lib/permissions';

export default async function StoresPage() {
  try {
    await requirePermission('canAccessAdmin');
  } catch {
    return <div className="p-8 text-red-500">Unauthorized. Admin access required.</div>;
  }

  // Load first page of 20 stores Server-Side for instant render
  const initialStores = (await getStoresPaginated()) as unknown[] as { id: string; name: string; location?: string; is_active?: boolean }[];

  return <StoresClient initialStores={initialStores} />;
}
