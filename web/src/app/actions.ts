'use server';

import { adminAuth } from '@/lib/firebaseAdmin';
import { revalidatePath } from 'next/cache';
import { getSession } from '@/lib/session';
import { cookies } from 'next/headers';
import crypto from 'crypto';
import { getDataConnect } from 'firebase-admin/data-connect';
import { serializeDoc } from '@/lib/serializeDoc';

export async function login(idToken: string) {
  const expiresIn = 60 * 60 * 24 * 5 * 1000;
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

// -- MIGRATED CRUD ACTIONS (No longer used as client calls DataConnect directly) --
export async function addItem(data: any) {}
export async function updateItem(id: string, data: any) {}
export async function deleteItem(id: string) {}
export async function addSale(data: any) {}
export async function deleteSale(id: string) {}
export async function addUdhaar(data: any) {}
export async function deleteUdhaar(id: string) {}
export async function addExpense(data: any) {}
export async function deleteExpense(id: string) {}

export async function createStore(name: string) {
  // Requires Store table migration in schema.gql
  return { success: false, error: "Not implemented in DataConnect yet." };
}

export async function archiveOldData(daysOld: number): Promise<{ success: boolean; error?: string; count?: number }> {
  return { success: false, error: "Not implemented in DataConnect yet." };
}

export async function getStoresPaginated(lastId?: string, limitCount = 20) {
  return [];
}

export async function getUsersPaginated(lastId?: string, limitCount = 20) {
  return [];
}

export async function toggleStoreStatus(storeId: string, isActive: boolean) {}
export async function updateUserRole(userId: string, role: string, storeId: string | null) {}

export async function fetchMoreData(collectionName: string, lastUpdatedAt: number, limitCount = 20) {
  // DataConnect queries are currently not paginated in the generated SDK,
  // they fetch all active items. Thus returning empty array here will stop the UI loader.
  return [];
}

export async function createStaffAccount(username: string, rawPin: string) {
  const session = await getSession();
  if (session?.role !== 'owner' || !session.storeId) {
    throw new Error("Only owners can create staff accounts");
  }

  const virtualEmail = `${username.toLowerCase().replace(/\s+/g, '')}@storebook.internal`;

  try {
    const userRecord = await adminAuth.createUser({
      email: virtualEmail,
      password: rawPin,
      displayName: username
    });
    
    const dc = getDataConnect({ serviceId: 'store-book', location: 'us-central1' });
    
    await dc.executeGraphql(
      `mutation CreateUser($id: String!, $role: String!, $createdAt: Float!, $storeId: String!) {
        user_upsert(data: { id: $id, role: $role, createdAt: $createdAt, storeId: $storeId }) { id }
      }`,
      { variables: { id: userRecord.uid, role: 'staff', createdAt: Math.floor(Date.now() / 1000), storeId: session.storeId } }
    );

    return { success: true };
  } catch (error: any) {
    console.error("Error creating staff:", error);
    return { success: false, error: error.message };
  }
}
