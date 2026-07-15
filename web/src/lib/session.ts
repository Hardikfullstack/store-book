import { cookies } from 'next/headers';
import { adminAuth } from '@/lib/firebaseAdmin';
import { getDataConnect } from 'firebase-admin/data-connect';

export async function getSession() {
  const cookieStore = await cookies();
  const sessionCookie = cookieStore.get('session')?.value;
  if (!sessionCookie) return null;

  try {
    const decodedClaims = await adminAuth.verifySessionCookie(sessionCookie, true);
    const dc = getDataConnect({ serviceId: 'store-book', location: 'us-central1' });
    
    // Attempt to fetch user by UID first
    let result = await dc.executeGraphql(
      `query GetUser($id: String!) { user(id: $id) { id, phoneNumber, role, stores, storeId, subscriptionPlan, subscriptionStatus } }`,
      { variables: { id: decodedClaims.uid } }
    );
    
    let userDoc = (result.data as any).user;

    // Fallback for legacy phone-number based IDs if not found
    if (!userDoc && decodedClaims.phone_number) {
      result = await dc.executeGraphql(
        `query GetUser($id: String!) { user(id: $id) { id, phoneNumber, role, stores, storeId, subscriptionPlan, subscriptionStatus } }`,
        { variables: { id: decodedClaims.phone_number } }
      );
      userDoc = (result.data as any).user;
    }
    
    if (!userDoc) {
      console.error("User not found in DataConnect database for uid:", decodedClaims.uid, "or phone:", decodedClaims.phone_number);
      return null;
    }

    const userData = userDoc;
    const activeStoreIdCookie = cookieStore.get('activeStoreId')?.value;
    
    const role = userData?.role || 'owner';
    let activeStoreId = '';

    // IDOR Mitigation: Validate the requested store ID against the user's database permissions
    if (role === 'staff') {
        // Staff are strictly bound to their assigned store. Ignore the cookie completely.
        activeStoreId = userData?.storeId || '';
    } else if (role === 'owner') {
        // Owners can switch stores, but ONLY to stores they actually own.
        if (activeStoreIdCookie && (userData?.stores?.includes(activeStoreIdCookie) || userData?.storeId === activeStoreIdCookie)) {
            activeStoreId = activeStoreIdCookie;
        } else {
            // Fallback to their first owned store if the cookie is forged or missing
            activeStoreId = (userData?.stores && userData.stores.length > 0) 
              ? userData.stores[0] 
              : (userData?.storeId || '');
        }
    } else if (role === 'admin') {
        // Admins can view any store
        activeStoreId = activeStoreIdCookie || 'admin_dashboard';
    }

    // Ensure stores array is always populated if storeId exists
    const resolvedStores = userData?.stores && userData.stores.length > 0 
      ? userData.stores 
      : (userData?.storeId ? [userData.storeId] : []);

    const isPremium = userDoc.subscriptionPlan === 'pro' && userDoc.subscriptionStatus === 'active';

    return { 
      uid: decodedClaims.uid, 
      phone: decodedClaims.phone_number,
      role: role, 
      storeId: activeStoreId,
      stores: resolvedStores,
      docId: userDoc.id,
      isPremium: isPremium
    };
  } catch (error) {
    console.error("Session verification failed:", error);
    return null;
  }
}
