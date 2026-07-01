'use server';

import { adminDb, adminAuth } from '@/lib/firebaseAdmin';
import { revalidatePath } from 'next/cache';
import { getSession } from '@/lib/session';
import { cookies } from 'next/headers';
import crypto from 'crypto';
import { serializeDoc } from '@/lib/serializeDoc';

export async function login(idToken: string) {
  const expiresIn = 60 * 60 * 24 * 5 * 1000; // 5 days
  try {
    const sessionCookie = await adminAuth.createSessionCookie(idToken, { expiresIn });
    const cookieStore = await cookies();
    cookieStore.set('session', sessionCookie, { maxAge: expiresIn, httpOnly: true, secure: process.env.NODE_ENV === 'production' });
    return { success: true };
  } catch (error) {
    console.error("Login failed:", error);
    return { success: false, error: "Failed to create session" };
  }
}

export async function logout() {
  const cookieStore = await cookies();
  cookieStore.delete('session');
  cookieStore.delete('activeStoreId');
  revalidatePath('/');
}

export async function switchStore(storeId: string) {
  const session = await getSession();
  if (!session) throw new Error("Unauthorized");
  
  if (session.role === 'staff') {
    throw new Error("Staff cannot switch stores");
  }
  
  if (session.role === 'owner' && !session.stores.includes(storeId)) {
    throw new Error("Unauthorized: You do not own this store");
  }

  const cookieStore = await cookies();
  const expiresIn = 60 * 60 * 24 * 5 * 1000;
  cookieStore.set('activeStoreId', storeId, { maxAge: expiresIn, httpOnly: true, secure: process.env.NODE_ENV === 'production' });
  revalidatePath('/');
}

// Helper to get the correct path based on user role
async function getCollectionRef(collectionName: string) {
  const session = await getSession();
  if (!session) throw new Error("Unauthorized");
  
  if (session.role === 'admin') {
    throw new Error("Admins should not mutate store data directly without specifying a storeId");
  }

  return adminDb.collection('stores').doc(session.storeId).collection(collectionName);
}

// -- STORE MANAGEMENT --
export async function createStore(name: string) {
  const session = await getSession();
  if (!session || session.role !== 'owner') {
    throw new Error("Unauthorized: Only owners can create stores");
  }

  const newStoreId = crypto.randomUUID();
  
  // 1. Create the store document
  await adminDb.collection('stores').doc(newStoreId).set({
    name,
    owner_phone: session.phone,
    is_premium: false,
    created_at: Date.now()
  });

  // 2. Add store to user's array
  const userRef = adminDb.collection('users').doc(session.docId);
  const userDoc = await userRef.get();
  const stores = userDoc.data()?.stores || [];
  if (!stores.includes(newStoreId)) {
    await userRef.update({ stores: [...stores, newStoreId] });
  }

  // 3. Switch to it
  await switchStore(newStoreId);
  return { success: true, storeId: newStoreId };
}

// -- ITEMS --
export async function addItem(data: any) {
  const ref = await getCollectionRef('items');
  const cloudId = crypto.randomUUID();
  await ref.doc(cloudId).set({
    ...data,
    cloud_id: cloudId,
    updated_at: Date.now(),
    is_deleted: 0
  });
  revalidatePath('/items');
  revalidatePath('/');
}

export async function updateItem(id: string, data: any) {
  const ref = await getCollectionRef('items');
  await ref.doc(id).update({
    ...data,
    updated_at: Date.now()
  });
  revalidatePath('/items');
  revalidatePath('/');
}

export async function deleteItem(id: string) {
  const session = await getSession();
  if (session?.role === 'staff') throw new Error("Staff cannot delete items");
  const ref = await getCollectionRef('items');
  await ref.doc(id).update({ is_deleted: 1, updated_at: Date.now() });
  revalidatePath('/items');
  revalidatePath('/');
}

// -- SALES --
export async function addSale(data: any) {
  const ref = await getCollectionRef('sales');
  const cloudId = crypto.randomUUID();
  await ref.doc(cloudId).set({
    ...data,
    cloud_id: cloudId,
    timestamp: Date.now(),
    updated_at: Date.now(),
    is_deleted: 0
  });
  revalidatePath('/sales');
  revalidatePath('/');
}

export async function deleteSale(id: string) {
  const session = await getSession();
  if (session?.role === 'staff') throw new Error("Staff cannot delete sales");
  const ref = await getCollectionRef('sales');
  await ref.doc(id).update({ is_deleted: 1, updated_at: Date.now() });
  revalidatePath('/sales');
  revalidatePath('/');
}

// -- UDHAAR --
export async function addUdhaar(data: any) {
  const ref = await getCollectionRef('udhaar');
  const cloudId = crypto.randomUUID();
  await ref.doc(cloudId).set({
    ...data,
    cloud_id: cloudId,
    timestamp: Date.now(),
    updated_at: Date.now(),
    is_deleted: 0
  });
  revalidatePath('/udhaar');
  revalidatePath('/');
}

export async function deleteUdhaar(id: string) {
  const session = await getSession();
  if (session?.role === 'staff') throw new Error("Staff cannot delete udhaar");
  const ref = await getCollectionRef('udhaar');
  await ref.doc(id).update({ is_deleted: 1, updated_at: Date.now() });
  revalidatePath('/udhaar');
  revalidatePath('/');
}

// -- EXPENSES --
export async function addExpense(data: any) {
  const ref = await getCollectionRef('expenses');
  const cloudId = crypto.randomUUID();
  await ref.doc(cloudId).set({
    ...data,
    cloud_id: cloudId,
    timestamp: Date.now(),
    updated_at: Date.now(),
    is_deleted: 0
  });
  revalidatePath('/expenses');
  revalidatePath('/');
}

export async function deleteExpense(id: string) {
  const session = await getSession();
  if (session?.role === 'staff') throw new Error("Staff cannot delete expenses");
  const ref = await getCollectionRef('expenses');
  await ref.doc(id).update({ is_deleted: 1, updated_at: Date.now() });
  revalidatePath('/expenses');
  revalidatePath('/');
}

// -- ADMIN ACTIONS --
export async function archiveOldData(daysOld: number) {
  const session = await getSession();
  if (!session || session.role !== 'admin') {
    return { success: false, error: "Unauthorized" };
  }

  const thresholdDate = Date.now() - (daysOld * 24 * 60 * 60 * 1000);
  
  try {
    // In a real production app, you would use a batched or streaming query
    // and ideally run this in a background Cloud Function.
    // For this demonstration, we will query and mark as archived.
    const collectionsToArchive = ['sales', 'udhaar', 'expenses'];
    let totalArchived = 0;

    for (const coll of collectionsToArchive) {
      const snapshot = await adminDb.collectionGroup(coll)
        .where('updated_at', '<', thresholdDate)
        .where('is_archived', '!=', 1)
        .get();

      const batch = adminDb.batch();
      snapshot.docs.forEach((doc) => {
        batch.update(doc.ref, { is_archived: 1, archived_at: Date.now() });
        totalArchived++;
      });
      
      if (snapshot.docs.length > 0) {
        await batch.commit();
      }
    }

    return { success: true, count: totalArchived };
  } catch (error: any) {
    console.error("Archival failed:", error);
    return { success: false, error: error.message };
  }
}

// -- SECURE PAGINATED ADMIN FETCHING --
export async function getStoresPaginated(lastId?: string, limitCount = 20) {
  const session = await getSession();
  if (!session || session.role !== 'admin') throw new Error("Unauthorized");

  let query = adminDb.collection('stores').orderBy('created_at', 'desc').limit(limitCount);
  
  if (lastId) {
    const lastDoc = await adminDb.collection('stores').doc(lastId).get();
    if (lastDoc.exists) {
      query = query.startAfter(lastDoc);
    }
  }

  const snapshot = await query.get();
  return snapshot.docs.map(doc => ({ id: doc.id, ...doc.data() }));
}

export async function getUsersPaginated(lastId?: string, limitCount = 20) {
  const session = await getSession();
  if (!session || session.role !== 'admin') throw new Error("Unauthorized");

  // In Firestore, paginating users by ID or a created_at field is necessary.
  // We'll sort by ID (phone) for stable pagination.
  let query = adminDb.collection('users').orderBy('__name__').limit(limitCount);
  
  if (lastId) {
    const lastDoc = await adminDb.collection('users').doc(lastId).get();
    if (lastDoc.exists) {
      query = query.startAfter(lastDoc);
    }
  }

  const snapshot = await query.get();
  return snapshot.docs.map(doc => ({ id: doc.id, ...doc.data() }));
}

// -- ADMIN CRUD --
export async function toggleStoreStatus(storeId: string, isActive: boolean) {
  const session = await getSession();
  if (!session || session.role !== 'admin') throw new Error("Unauthorized");
  
  await adminDb.collection('stores').doc(storeId).update({ is_active: isActive });
  revalidatePath('/admin/stores');
}

export async function updateUserRole(userId: string, role: string, storeId: string | null) {
  const session = await getSession();
  if (!session || session.role !== 'admin') throw new Error("Unauthorized");
  
  // Protect the root system generated admin from accidental/malicious changes
  if (userId === '+919999999999') {
    throw new Error("Action Forbidden: Cannot modify the root system admin.");
  }

  await adminDb.collection('users').doc(userId).update({ 
    role, 
    storeId: storeId || null 
  });
  revalidatePath('/admin/users');
}

// -- UNIVERSAL SERVER PAGINATION --
export async function fetchMoreData(collectionName: string, lastUpdatedAt: number, limitCount = 20) {
  const session = await getSession();
  if (!session) throw new Error("Unauthorized");

  let query: FirebaseFirestore.Query;
  
  if (session.role === 'admin') {
    query = adminDb.collectionGroup(collectionName);
  } else {
    query = adminDb.collection('stores').doc(session.storeId).collection(collectionName);
  }
  
  const snapshot = await query
    .orderBy('updated_at', 'desc')
    .startAfter(lastUpdatedAt)
    .limit(limitCount)
    .get();

  return snapshot.docs
    .map(doc => {
      const data = doc.data();
      // Data Leak Fix: Strip sensitive financial fields at the server level for staff
      if (session.role === 'staff') {
        if (data.buy_price !== undefined) delete data.buy_price;
        if (Array.isArray(data.items)) {
          data.items.forEach((item: any) => {
            if (item.buy_price !== undefined) delete item.buy_price;
          });
        }
      }
      return serializeDoc({ id: doc.id, ...data });
    })
    .filter((data: any) => data.is_deleted !== 1);
}

// ── Staff Management ────────────────────────────────────────────────────────
export async function createStaffAccount(username: string, rawPin: string) {
  const session = await getSession();
  if (session?.role !== 'owner' || !session.storeId) {
    throw new Error("Only owners can create staff accounts");
  }

  // Ensure owner actually owns this store
  const userDoc = await adminDb.collection('users').doc(session.uid).get();
  const userData = userDoc.data();
  if (!userData?.stores?.includes(session.storeId)) {
    throw new Error("Unauthorized to add staff to this store");
  }

  // Create virtual email for staff login
  const virtualEmail = `${username.toLowerCase().replace(/\s+/g, '')}@storebook.internal`;

  try {
    // 1. Create in Firebase Auth
    const userRecord = await adminAuth.createUser({
      email: virtualEmail,
      password: rawPin,
      displayName: username
    });

    // 2. Create user document
    await adminDb.collection('users').doc(userRecord.uid).set({
      username: username,
      role: 'staff',
      storeId: session.storeId,
      created_by: session.uid,
      created_at: Date.now()
    });

    return { success: true };
  } catch (error: any) {
    console.error("Error creating staff:", error);
    return { success: false, error: error.message };
  }
}
