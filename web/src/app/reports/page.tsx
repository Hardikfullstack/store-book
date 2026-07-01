import { getSession } from '@/lib/session';
import { redirect } from 'next/navigation';
import ReportsClient from './ReportsClient';

export default async function ReportsPage() {
  const session = await getSession();
  if (!session) redirect('/login');
  if (session.role === 'staff') redirect('/'); // Staff cannot see reports

  return (
    <div className="max-w-6xl mx-auto">
      <ReportsClient storeId={session.storeId || ''} />
    </div>
  );
}
