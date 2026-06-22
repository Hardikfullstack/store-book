import { getStoresPaginated } from '@/app/actions';
import StoresClient from './StoresClient';
import { getSession } from '@/lib/session';

export default async function StoresPage() {
  const session = await getSession();
  if (!session || session.role !== 'admin') {
    return <div className="p-8 text-red-500">Unauthorized. Admin access required.</div>;
  }

  // Load first page of 20 stores Server-Side for instant render
  const initialStores = await getStoresPaginated();

  return <StoresClient initialStores={initialStores} />;
}
