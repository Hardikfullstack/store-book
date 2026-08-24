import SuppliersClient from './SuppliersClient';
import { getSession } from '@/lib/session';
import { redirect } from 'next/navigation';

export default async function SuppliersPage() {
  const session = await getSession();

  if (!session) {
    redirect('/login');
  }

  if (!session.storeId) {
    return <div>No active store selected</div>;
  }

  const isPremium = session.isPremium;

  return (
    <div className="flex flex-col h-full overflow-hidden">
      <SuppliersClient storeId={session.storeId} isPremium={isPremium} />
    </div>
  );
}
