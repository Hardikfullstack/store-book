import ItemsClient from './ItemsClient';
import { getSession } from '@/lib/session';
import { serializeDoc } from '@/lib/serializeDoc';

async function getItems(session: any) {
  try {
    let snapshot;
    if (session.role === 'admin') {
      snapshot = await adminDb.collectionGroup('items').orderBy('updated_at', 'desc').limit(20).get();
    } else {
      snapshot = await adminDb.collection('stores').doc(session.storeId).collection('items').orderBy('updated_at', 'desc').limit(20).get();
    }
    return snapshot.docs
      .map(doc => {
        const data = doc.data();
        // Data Leak Fix: Strip sensitive financial fields at the server level for staff
        if (session.role === 'staff') {
          if (data.buy_price !== undefined) delete data.buy_price;
        }
        return { id: doc.id, ...data };
      })
      .filter((data: any) => data.is_deleted !== 1);
  } catch (error) {
    console.error("Error fetching items from Firebase:", error);
    return [];
  }
}

export default async function ItemsPage() {
  const session = await getSession();
  if (!session) return <div>Please login</div>;
  
  const items: any[] = [];
  const isPremium = true;

  return (
    <ItemsClient 
      initialItems={items} 
      userRole={session.role} 
      storeId={session.storeId} 
      isPremium={isPremium} 
    />
  );
}
