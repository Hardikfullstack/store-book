import AuditClient from './AuditClient';
import { getSession } from '@/lib/session';
import { redirect } from 'next/navigation';

export default async function AuditPage() {
  const session = await getSession();
  if (!session) return <div>Please login</div>;
  
  // Stock audit report is only for managers/owners
  if (session.role === 'staff' || session.role === 'cashier') redirect('/');

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
