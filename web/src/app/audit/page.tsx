import AuditClient from './AuditClient';
import { redirect } from 'next/navigation';
import { requirePermission } from '@/lib/permissions';

export default async function AuditPage() {
  let session;
  try {
    session = await requirePermission('canViewReports');
  } catch {
    return redirect('/login');
  }

  const adjustments: any[] = [];
  const isPremium = session.isPremium;

  return (
    <AuditClient
      initialAdjustments={adjustments}
      storeId={session.storeId}
      isPremium={isPremium}
    />
  );
}
