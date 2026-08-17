import { redirect } from 'next/navigation';
import ReportsClient from './ReportsClient';
import { requirePermission } from '@/lib/permissions';

export default async function ReportsPage() {
  let session;
  try {
    session = await requirePermission('canViewReports');
  } catch {
    return redirect('/login');
  }

  return (
    <div className="max-w-6xl mx-auto">
      <ReportsClient storeId={session.storeId || ''} />
    </div>
  );
}
