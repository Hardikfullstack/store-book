import { getSession } from '@/lib/session';
import { redirect } from 'next/navigation';
import QuotationsClient from './QuotationsClient';

export default async function QuotationsPage() {
  const session = await getSession();
  if (!session) redirect('/login');

  return (
    <div className="max-w-6xl mx-auto">
      <QuotationsClient 
        storeId={session.storeId || ''} 
        userRole={session.role}
      />
    </div>
  );
}
