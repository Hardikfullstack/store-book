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

    let userDoc = (result.data as { user?: { id?: string; phoneNumber?: string; role?: string; stores?: string[]; storeId?: string; subscriptionPlan?: string; subscriptionStatus?: string } }).user;

    // Fallback for legacy phone-number based IDs if not found
    if (!userDoc && decodedClaims.phone_number) {
      result = await dc.executeGraphql(
        `query GetUser($id: String!) { user(id: $id) { id, phoneNumber, role, stores, storeId, subscriptionPlan, subscriptionStatus } }`,
        { variables: { id: decodedClaims.phone_number } }
      );
      userDoc = (result.data as { user?: { id?: string; phoneNumber?: string; role?: string; stores?: string[]; storeId?: string; subscriptionPlan?: string; subscriptionStatus?: string } }).user;
    }

    if (!userDoc) {
      console.error("User not found in DataConnect database for uid:", decodedClaims.uid, "or phone:", decodedClaims.phone_number);
      return null;
    }

    const userData = userDoc;
    const activeStoreIdCookie = cookieStore.get('activeStoreId')?.value;

    const role = userData?.role || 'owner';

    // Ensure stores array is always populated and deduplicated
    const rawStores = [
      ...(Array.isArray(userData?.stores) ? userData.stores : []),
      ...(userData?.storeId ? [userData.storeId] : []),
    ].filter((s): s is string => typeof s === 'string' && s.trim().length > 0);
    const resolvedStores = Array.from(new Set(rawStores));

    let activeStoreId = '';

    // IDOR Mitigation: Validate the requested store ID against the user's database permissions
    if (role === 'staff') {
      // Staff are strictly bound to their assigned store. Ignore the cookie completely.
      activeStoreId = userData?.storeId || (resolvedStores[0] || '');
    } else if (role === 'owner') {
      // Owners can switch stores, but ONLY to stores they actually have access to.
      if (activeStoreIdCookie && resolvedStores.includes(activeStoreIdCookie)) {
        activeStoreId = activeStoreIdCookie;
      } else {
        // Fallback to their first owned store if the cookie is forged or missing
        activeStoreId = resolvedStores[0] || (userData?.storeId || '');
      }
    } else if (role === 'admin') {
      // Platform Admins can view any store or default to admin_dashboard
      activeStoreId = activeStoreIdCookie || (resolvedStores[0] || userData?.storeId || 'admin_dashboard');
    } else {
      activeStoreId = resolvedStores[0] || (userData?.storeId || '');
    }

    // Fetch store metadata (names) for the user's stores or all stores for admin
    let storeDetails: { id: string; name: string }[] = [];
    try {
      const storesRes = await dc.executeGraphql(
        `query GetStoresForUser { stores { id, name } }`
      );
      const allStores = (storesRes.data as { stores?: { id: string; name?: string }[] })?.stores || [];

      if (role === 'admin') {
        storeDetails = allStores.map(s => ({
          id: s.id,
          name: s.name?.trim() || `Store (${s.id.slice(0, 8)}…)`
        }));
        if (storeDetails.length === 0 && resolvedStores.length > 0) {
          storeDetails = resolvedStores.map(sId => ({
            id: sId,
            name: `Store (${sId.slice(0, 8)}…)`
          }));
        }
      } else {
        storeDetails = resolvedStores.map(sId => {
          const matched = allStores.find(s => s.id === sId);
          return {
            id: sId,
            name: matched?.name?.trim() || `Store (${sId.slice(0, 8)}…)`
          };
        });
      }
    } catch (err) {
      console.warn("Could not fetch store metadata for user stores:", err);
      storeDetails = resolvedStores.map(sId => ({
        id: sId,
        name: `Store (${sId.slice(0, 8)}…)`
      }));
    }

    const isPremium = userDoc.subscriptionPlan === 'pro' && userDoc.subscriptionStatus === 'active';

    return {
      uid: decodedClaims.uid,
      phone: decodedClaims.phone_number,
      role: role,
      storeId: activeStoreId,
      stores: resolvedStores,
      storeDetails: storeDetails,
      docId: userDoc.id,
      isPremium: isPremium
    };
  } catch (error) {
    console.error("Session verification failed:", error);
    return null;
  }
}
