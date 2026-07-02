import { getSession } from '@/lib/session';
import DashboardClient from './DashboardClient';

export default async function DashboardPage() {
  const session = await getSession();
  if (!session) return <div>Please login</div>;
  const stats = { totalItems: 0, totalSales: 0, totalUdhaar: 0, totalExpenses: 0, totalStores: 0, salesData: [], itemsData: [], saleItemsData: [] };
  const isPremium = true;

  return (
    <DashboardClient 
      initialStats={stats} 
      userRole={session.role}
      storeId={session.storeId}
      isPremium={isPremium}
    />
  );
}
