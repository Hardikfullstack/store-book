import SalesClient from './SalesClient';
import { getSession } from '@/lib/session';

export default async function SalesPage() {
  const session = await getSession();
  if (!session) return <div>Please login</div>;
  
  const sales: any[] = [];
  const isPremium = session.isPremium;

  return (
    <SalesClient 
      initialSales={sales} 
      userRole={session.role}
      storeId={session.storeId}
      isPremium={isPremium}
    />
  );
}
