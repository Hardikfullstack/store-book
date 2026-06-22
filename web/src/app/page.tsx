import { adminDb } from '@/lib/firebaseAdmin';
import { getSession } from '@/lib/session';
import DashboardClient from './DashboardClient';

async function getStats(session: any) {
  let stats = { totalItems: 0, totalSales: 0, totalUdhaar: 0, totalExpenses: 0, totalStores: 0, salesData: [] as any[], itemsData: [] as any[] };
  try {
    if (session.role === 'admin') {
      // Admin sees aggregate metrics from across all stores
      const storesSnap = await adminDb.collection('stores').count().get();
      stats.totalStores = storesSnap.data().count;
      
      // In a real app with huge data, you'd use a cloud function to aggregate this,
      // but for this demo we'll query collectionGroups.
      const [itemsSnap, salesSnap, udhaarSnap, expensesSnap] = await Promise.all([
        adminDb.collectionGroup('items').get(),
        adminDb.collectionGroup('sales').get(),
        adminDb.collectionGroup('udhaar').get(),
        adminDb.collectionGroup('expenses').get()
      ]);
      
      stats.totalItems = itemsSnap.size;
      stats.totalSales = salesSnap.docs.reduce((acc, doc) => acc + (doc.data().total_amount || 0), 0);
      stats.totalUdhaar = udhaarSnap.docs.reduce((acc, doc) => acc + (doc.data().amount || 0), 0);
      stats.totalExpenses = expensesSnap.docs.reduce((acc, doc) => acc + (doc.data().amount || 0), 0);
      stats.salesData = salesSnap.docs.map(d => d.data());
      stats.itemsData = itemsSnap.docs.map(d => d.data());
    } else {
      // Client sees only their store
      const storeRef = adminDb.collection('stores').doc(session.storeId);
      const [itemsSnap, salesSnap, udhaarSnap, expensesSnap] = await Promise.all([
        storeRef.collection('items').get(),
        storeRef.collection('sales').get(),
        storeRef.collection('udhaar').get(),
        storeRef.collection('expenses').get()
      ]);
      
      stats.totalItems = itemsSnap.size;
      stats.totalSales = salesSnap.docs.reduce((acc, doc) => acc + (doc.data().total_amount || 0), 0);
      stats.totalUdhaar = udhaarSnap.docs.reduce((acc, doc) => acc + (doc.data().amount || 0), 0);
      stats.totalExpenses = expensesSnap.docs.reduce((acc, doc) => acc + (doc.data().amount || 0), 0);
      stats.salesData = salesSnap.docs.map(d => {
          const data = d.data();
          if (session.role === 'staff' && Array.isArray(data.items)) {
              data.items.forEach((i: any) => { delete i.buy_price; });
          }
          return data;
      });
      stats.itemsData = itemsSnap.docs.map(d => {
          const data = d.data();
          if (session.role === 'staff') delete data.buy_price;
          return data;
      });
    }
    
    return stats;
  } catch (error) {
    console.error("Error fetching stats from Firebase:", error);
    return stats;
  }
}

export default async function DashboardPage() {
  const session = await getSession();
  if (!session) return <div>Please login</div>;
  const stats = await getStats(session);

  let isPremium = false;
  if (session.storeId) {
    const storeDoc = await adminDb.collection('stores').doc(session.storeId).get();
    isPremium = storeDoc.exists ? (storeDoc.data()?.is_premium || false) : false;
  }

  return (
    <DashboardClient 
      initialStats={stats} 
      userRole={session.role}
      storeId={session.storeId}
      isPremium={isPremium}
    />
  );
}
