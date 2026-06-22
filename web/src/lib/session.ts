import { cookies } from 'next/headers';
import { adminAuth, adminDb } from '@/lib/firebaseAdmin';

export async function getSession() {
  const cookieStore = await cookies();
  const sessionCookie = cookieStore.get('session')?.value;
  if (!sessionCookie) return null;

  try {
    const decodedClaims = await adminAuth.verifySessionCookie(sessionCookie, true);
    
    // Fetch the user's role and store mapping from Firestore
    // Seed script used phone_number, but staff uses uid
    let userDoc = await adminDb.collection('users').doc(decodedClaims.uid).get();
    if (!userDoc.exists && decodedClaims.phone_number) {
        userDoc = await adminDb.collection('users').doc(decodedClaims.phone_number).get();
    }
    
    if (!userDoc.exists) {
      console.error("User not found in database for uid:", decodedClaims.uid, "or phone:", decodedClaims.phone_number);
      return null;
    }

    const userData = userDoc.data();
    const activeStoreIdCookie = cookieStore.get('activeStoreId')?.value;
    
    const role = userData?.role || 'owner';
    let activeStoreId = '';

    // IDOR Mitigation: Validate the requested store ID against the user's database permissions
    if (role === 'staff') {
        // Staff are strictly bound to their assigned store. Ignore the cookie completely.
        activeStoreId = userData?.storeId || '';
    } else if (role === 'owner') {
        // Owners can switch stores, but ONLY to stores they actually own.
        if (activeStoreIdCookie && userData?.stores?.includes(activeStoreIdCookie)) {
            activeStoreId = activeStoreIdCookie;
        } else {
            // Fallback to their first owned store if the cookie is forged or missing
            activeStoreId = userData?.stores && userData.stores.length > 0 ? userData.stores[0] : '';
        }
    } else if (role === 'admin') {
        // Admins can view any store
        activeStoreId = activeStoreIdCookie || 'admin_dashboard';
    }

    return { 
      uid: decodedClaims.uid, 
      phone: decodedClaims.phone_number,
      role: role, 
      storeId: activeStoreId,
      stores: userData?.stores || [],
      docId: userDoc.id
    };
  } catch (error) {
    console.error("Session verification failed:", error);
    return null;
  }
}
