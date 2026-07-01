import SalesClient from './SalesClient';
import { getSession } from '@/lib/session';
import { serializeDoc } from '@/lib/serializeDoc';

async function getSales(session: any) {
  try {
    let snapshot;
    if (session.role === 'admin') {
      snapshot = await adminDb.collectionGroup('sales').orderBy('updated_at', 'desc').limit(20).get();
    } else {
      snapshot = await adminDb.collection('stores').doc(session.storeId).collection('sales').orderBy('updated_at', 'desc').limit(20).get();
    }
    return snapshot.docs
      .map(doc => {
        const data = doc.data();
        // Data Leak Fix: Strip sensitive financial fields at the server level for staff
        if (session.role === 'staff') {
          if (Array.isArray(data.items)) {
            data.items.forEach((item: any) => {
              if (item.buy_price !== undefined) delete item.buy_price;
            });
          }
        }
        return { id: doc.id, ...data };
      })
      .filter((data: any) => data.is_deleted !== 1);
  } catch (error) {
    console.error("Error fetching sales from Firebase:", error);
    return [];
  }
}

export default async function SalesPage() {
  const session = await getSession();
  if (!session) return <div>Please login</div>;
  
  const sales: any[] = [];
  const isPremium = true;

  return (
    <SalesClient 
      initialSales={sales} 
      userRole={session.role}
      storeId={session.storeId}
      isPremium={isPremium}
    />
  );
}
