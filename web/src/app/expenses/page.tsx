import { adminDb } from '@/lib/firebaseAdmin';
import ExpensesClient from './ExpensesClient';
import { getSession } from '@/lib/session';
import { serializeDoc } from '@/lib/serializeDoc';

async function getExpenses(session: any) {
  try {
    let snapshot;
    if (session.role === 'admin') {
      snapshot = await adminDb.collectionGroup('expenses').orderBy('updated_at', 'desc').limit(20).get();
    } else {
      snapshot = await adminDb.collection('stores').doc(session.storeId).collection('expenses').orderBy('updated_at', 'desc').limit(20).get();
    }
    return snapshot.docs
      .map(doc => serializeDoc({ id: doc.id, ...doc.data() }))
      .filter((data: any) => data.is_deleted !== 1);
  } catch (error) {
    console.error("Error fetching expenses from Firebase:", error);
    return [];
  }
}

export default async function ExpensesPage() {
  const session = await getSession();
  if (!session) return <div>Please login</div>;
  const expenses = await getExpenses(session);

  let isPremium = false;
  if (session.storeId) {
    const storeDoc = await adminDb.collection('stores').doc(session.storeId).get();
    isPremium = storeDoc.exists ? (storeDoc.data()?.is_premium || false) : false;
  }

  return (
    <ExpensesClient 
      initialExpenses={expenses} 
      storeId={session.storeId}
      isPremium={isPremium}
    />
  );
}
