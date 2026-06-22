import { adminDb } from '@/lib/firebaseAdmin';
import UdhaarClient from './UdhaarClient';
import { getSession } from '@/lib/session';

async function getUdhaar(session: any) {
  try {
    let snapshot;
    if (session.role === 'admin') {
      snapshot = await adminDb.collectionGroup('udhaar').orderBy('updated_at', 'desc').limit(20).get();
    } else {
      snapshot = await adminDb.collection('stores').doc(session.storeId).collection('udhaar').orderBy('updated_at', 'desc').limit(20).get();
    }
    return snapshot.docs
      .map(doc => ({ id: doc.id, ...doc.data() }))
      .filter((data: any) => data.is_deleted !== 1);
  } catch (error) {
    console.error("Error fetching udhaar from Firebase:", error);
    return [];
  }
}

export default async function UdhaarPage() {
  const session = await getSession();
  if (!session) return <div>Please login</div>;
  const udhaar = await getUdhaar(session);

  let storeName = "";
  let isPremium = false;
  if (session.storeId) {
    try {
      const storeDoc = await adminDb.collection('stores').doc(session.storeId).get();
      if (storeDoc.exists) {
        storeName = storeDoc.data()?.name || "";
        isPremium = storeDoc.data()?.is_premium || false;
      }
    } catch(e) {
      console.error(e);
    }
  }

  return (
    <UdhaarClient 
      initialUdhaar={udhaar} 
      storeName={storeName}
      storeId={session.storeId}
      isPremium={isPremium}
    />
  );
}
