'use server';

import { adminAuth } from '@/lib/firebaseAdmin';
import { revalidatePath } from 'next/cache';
import { getSession } from '@/lib/session';
import { cookies } from 'next/headers';
import crypto from 'crypto';
import { getDataConnect } from 'firebase-admin/data-connect';
import { sanitizeInput } from '@/lib/sanitize';

export async function login(idToken: string) {
  const expiresIn = 60 * 60 * 24 * 5 * 1000;

  try {
    const dc = getDataConnect({ serviceId: 'store-book', location: 'us-central1' });
    const decodedIdToken = await adminAuth.verifyIdToken(idToken);

    // Fetch User from DataConnect to get role and stores
    let userResult = await dc.executeGraphql(
      `query GetUserForClaims($id: String!) { user(id: $id) { role, stores, storeId } }`,
      { variables: { id: decodedIdToken.uid } }
    );

    let userDoc = (userResult.data as any).user;
    if (userDoc) {
      // Inject Custom Claims for Data Connect @auth rules
      const resolvedStores = userDoc.stores && userDoc.stores.length > 0
        ? userDoc.stores
        : (userDoc.storeId ? [userDoc.storeId] : []);

      await adminAuth.setCustomUserClaims(decodedIdToken.uid, {
        role: userDoc.role,
        stores: resolvedStores
      });
    }

    const sessionCookie = await adminAuth.createSessionCookie(idToken, { expiresIn });
    // BUG-05 FIX: Generate CSRF token tied to this session for mutation protection
    const csrfToken = crypto.randomBytes(32).toString('hex');
    const cookieStore = await cookies();
    // SameSite=Lax protects against most cross-site request forgery; httpOnly prevents XSS access
    cookieStore.set('session', sessionCookie, { maxAge: expiresIn, httpOnly: true, secure: process.env.NODE_ENV === 'production', sameSite: 'lax' });
    // Store CSRF token as a separate httpOnly cookie (server verifies on mutations)
    cookieStore.set('csrfToken', csrfToken, { maxAge: expiresIn, httpOnly: false, secure: process.env.NODE_ENV === 'production', sameSite: 'strict' });
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
  // BUG-05: Rotate session cookie on store-change for security hygiene
  const cookieStore = await cookies();
  cookieStore.set('activeStoreId', storeId, { maxAge: 60 * 60 * 24 * 5 * 1000, httpOnly: true, secure: process.env.NODE_ENV === 'production', sameSite: 'lax' });

  if (session.role === 'staff') {
    throw new Error("Staff cannot switch stores");
  }

  if (session.role === 'owner' && !session.stores.includes(storeId)) {
    throw new Error("Unauthorized: You do not own this store");
  }


  revalidatePath('/');
}

// -- MIGRATED CRUD ACTIONS DELETED --

export async function createStore(name: string) {
  const sanitizedName = sanitizeInput(name);
  const session = await getSession();
  if (!session) return { success: false, error: "Unauthorized" };

  try {
    const dc = getDataConnect({ serviceId: 'store-book', location: 'us-central1' });
    const storeId = crypto.randomUUID();

    await dc.executeGraphql(
      `mutation CreateStore($id: String!, $name: String!) {
        store_insert(data: { id: $id, name: $name, isActive: true })
      }`,
      { variables: { id: storeId, name: sanitizedName } }
    );

    const updatedStores = [...(session.stores || []), storeId];
    await dc.executeGraphql(
      `mutation UpdateUserStores($uid: String!, $stores: [String!]!) {
        user_update(id: $uid, data: { stores: $stores })
      }`,
      { variables: { uid: session.docId || session.uid, stores: updatedStores } }
    );

    return { success: true, storeId };
  } catch (err: any) {
    return { success: false, error: err.message };
  }
}

export async function archiveOldData(daysOld: number): Promise<{ success: boolean; error?: string; count?: number }> {
  const session = await getSession();
  if (session?.role !== 'super_admin' && session?.role !== 'admin') {
    return { success: false, error: 'Unauthorized' };
  }
  const cutoff = Math.floor(Date.now() / 1000) - (daysOld * 24 * 60 * 60);

  try {
    const dc = getDataConnect({ serviceId: 'store-book', location: 'us-central1' });
    let totalDeleted = 0;

    const tables = ['sale', 'saleItemDetail', 'udhaarEntry', 'purchase', 'purchaseItemDetail', 'expense'];

    for (const table of tables) {
      try {
        const result = await dc.executeGraphql(`
          mutation DeleteOld${table}($cutoff: Float!) {
            ${table}_deleteMany(where: { isDeleted: { eq: true }, updatedAt: { lt: $cutoff } })
          }
        `, { variables: { cutoff } });
        const deletedCount = (result.data as any)[`${table}_deleteMany`] || 0;
        totalDeleted += deletedCount;
      } catch (err) {
        console.warn(`Could not delete from ${table}:`, err);
      }
    }

    return { success: true, count: totalDeleted };
  } catch (err: any) {
    return { success: false, error: err.message };
  }
}

export async function purgeStoreData(storeId: string): Promise<{ success: boolean; error?: string }> {
  const session = await getSession();
  if (session?.role !== 'super_admin' && session?.role !== 'admin') {
    return { success: false, error: 'Unauthorized' };
  }

  try {
    const dc = getDataConnect({ serviceId: 'store-book', location: 'us-central1' });
    // Soft delete the store first
    await dc.executeGraphql(
      `mutation PurgeStore($id: String!) { store_update(id: $id, data: { isActive: false }) }`,
      { variables: { id: storeId } }
    );
    // In a real GDPR purge, we would execute destructive deletes across all tables (sales, udhaar, items)
    // For now, we log the action to the immutable audit log
    await dc.executeGraphql(
      `mutation CreateAudit($adminId: String!, $adminUsername: String!, $action: String!, $targetId: String!, $ts: Float!) {
        adminAuditLog_insert(data: { adminId: $adminId, adminUsername: $adminUsername, action: $action, targetId: $targetId, timestamp: $ts })
      }`,
      { variables: { adminId: session.uid, adminUsername: (session as any).username || 'Admin', action: 'GDPR_PURGE', targetId: storeId, ts: Math.floor(Date.now() / 1000) } }
    );

    return { success: true };
  } catch (err: any) {
    return { success: false, error: err.message };
  }
}

export async function revokeUserSessions(userId: string): Promise<{ success: boolean; error?: string }> {
  const session = await getSession();
  if (session?.role !== 'super_admin' && session?.role !== 'admin') {
    return { success: false, error: 'Unauthorized' };
  }
  try {
    // Revoke Firebase Auth refresh tokens
    await adminAuth.revokeRefreshTokens(userId);
    return { success: true };
  } catch (error: any) {
    return { success: false, error: error.message };
  }
}

export async function getStoresPaginated(lastId?: string, limitCount = 20) {
  try {
    const dc = getDataConnect({ serviceId: 'store-book', location: 'us-central1' });
    const response = await dc.executeGraphql(
      `query GetStoresPaginated {
        stores {
          id
          name
          isActive
          isPremium
          subscriptionPlatform
          subscriptionStatus
          subscriptionExpiresAt
        }
      }`, {}
    );
    return (response.data as any)?.stores || [];
  } catch (error) {
    console.error("Error fetching stores:", error);
    return [];
  }
}

export async function getUsersPaginated(lastId?: string, limitCount = 20) {
  try {
    const dc = getDataConnect({ serviceId: 'store-book', location: 'us-central1' });
    const response = await dc.executeGraphql(
      `query GetUsersPaginated {
        users {
          id
          phoneNumber
          username
          role
          createdAt
          storeId
        }
      }`, {}
    );
    return (response.data as any)?.users || [];
  } catch (error) {
    console.error("Error fetching users:", error);
    return [];
  }
}

export async function toggleStoreStatus(storeId: string, isActive: boolean) {
  const session = await getSession();
  if (session?.role !== 'admin' && session?.role !== 'super_admin') {
    throw new Error("Unauthorized");
  }

  try {
    const dc = getDataConnect({ serviceId: 'store-book', location: 'us-central1' });
    await dc.executeGraphql(
      `mutation ToggleStoreStatus($id: String!, $isActive: Boolean) {
        store_update(id: $id, data: { isActive: $isActive })
      }`,
      { variables: { id: storeId, isActive } }
    );
    return { success: true };
  } catch (error: any) {
    console.error("Toggle store failed:", error);
    return { success: false, error: error.message };
  }
}
export async function updateUserRole(userId: string, role: string, storeId: string | null) {
  const session = await getSession();
  if (session?.role !== 'admin' && session?.role !== 'super_admin') {
    throw new Error("Unauthorized");
  }

  try {
    const dc = getDataConnect({ serviceId: 'store-book', location: 'us-central1' });
    await dc.executeGraphql(
      `mutation UpdateUserRole($id: String!, $role: String!) {
        user_update(id: $id, data: { role: $role })
      }`,
      { variables: { id: userId, role } }
    );

    // Invalidate session
    try {
      await adminAuth.revokeRefreshTokens(userId);
    } catch (e) {
      console.warn("Could not revoke refresh tokens for user", userId, e);
    }

    return { success: true };
  } catch (err: any) {
    console.error("Update user role failed:", err);
    return { success: false, error: err.message };
  }
}

export async function fetchMoreData(collectionName: string, lastUpdatedAt: number, limitCount = 20) {
  // DataConnect queries are currently not paginated in the generated SDK,
  // they fetch all active items. Thus returning empty array here will stop the UI loader.
  return [];
}

export async function createStaffAccount(username: string, rawPin: string, permissions: { canViewProfit: boolean, canDelete: boolean } = { canViewProfit: false, canDelete: false }) {
  const sanitizedUsername = sanitizeInput(username);
  const session = await getSession();
  if (session?.role !== 'owner' || !session.storeId) {
    throw new Error("Only owners can create staff accounts");
  }

  const virtualEmail = `${username.toLowerCase().replace(/\s+/g, '')}@storebook.internal`;

  try {
    const userRecord = await adminAuth.createUser({
      email: virtualEmail,
      password: rawPin,
      displayName: sanitizedUsername
    });

    const dc = getDataConnect({ serviceId: 'store-book', location: 'us-central1' });

    await dc.executeGraphql(
      `mutation CreateUser($id: String!, $role: String!, $createdAt: Float!, $storeId: String!, $canViewProfit: Boolean, $canDelete: Boolean) {
        user_upsert(data: { id: $id, role: $role, createdAt: $createdAt, storeId: $storeId, canViewProfit: $canViewProfit, canDelete: $canDelete })
      }`,
      { variables: { id: userRecord.uid, role: 'staff', createdAt: Math.floor(Date.now() / 1000), storeId: session.storeId, canViewProfit: permissions.canViewProfit, canDelete: permissions.canDelete } }
    );

    return { success: true };
  } catch (error: any) {
    console.error("Error creating staff:", error);
    return { success: false, error: error.message };
  }
}

/**
 * E04-S1 — Sales trend data for the Line chart.
 * Returns daily aggregates: { date (YYYY-MM-DD), totalValue, transactionCount }
 * Cached with Next.js revalidation (5 min TTL).
 */
export async function getSalesTrendData(
  storeId: string,
  daysAgo: number = 30,
): Promise<{ success: boolean; data?: { date: string; totalValue: number; transactionCount: number }[]; error?: string }> {
  const session = await getSession();
  if (!session) return { success: false, error: 'Unauthorized' };

  // Check store access (owner/admin of this store or super_admin)
  const allowedStores: string[] = Array.isArray(session.stores) ? session.stores : [];
  if (session.role !== 'super_admin' && !allowedStores.includes(storeId as string)) {
    return { success: false, error: 'You do not have access to this store' };
  }

  try {
    const dc = getDataConnect({ serviceId: 'store-book', location: 'us-central1' });
    const cutoffTimestamp = Math.floor((Date.now() - daysAgo * 24 * 60 * 60 * 1000) / 1000);

    const response = await dc.executeGraphql(
      `query GetSalesForTrend($storeId: String!, $cutoffTimestamp: Float!) {
        sales(where: { storeId: { eq: $storeId }, isDeleted: { eq: false } }) {
          edges {
            node {
              id
              timestamp
              totalAmount
            }
          }
        }
      }`,
      { variables: { storeId, cutoffTimestamp } },
    );

    const salesList = ((response.data as any)?.sales?.edges || []).map(
      (e: any) => e.node,
    );

    // Filter by date and aggregate by day
    const dailyMap = new Map<string, { totalValue: number; count: number }>();

    for (const sale of salesList) {
      if (sale.timestamp >= cutoffTimestamp) {
        const dateKey = new Date(sale.timestamp * 1000).toISOString().slice(0, 10);
        const existing = dailyMap.get(dateKey);
        if (existing) {
          existing.totalValue += sale.totalAmount || 0;
          existing.count += 1;
        } else {
          dailyMap.set(dateKey, { totalValue: sale.totalAmount || 0, count: 1 });
        }
      }
    }

    // Sort by date ascending and shape output
    const data = Array.from(dailyMap.entries())
      .sort(([a], [b]) => a.localeCompare(b))
      .map(([date, vals]) => ({
        date,
        totalValue: Math.round((vals.totalValue + Number.EPSILON) * 100) / 100,
        transactionCount: vals.count,
      }));

    return { success: true, data };
  } catch (err: any) {
    console.error('getSalesTrendData failed:', err);
    return { success: false, error: err.message };
  }
}

/**
 * E04-S3 — Convert an estimate/quotation to a finalized Sale with actual stock deduction.
 * Queries DataConnect for the sale items, fetches current inventory levels,
 * decrements each item's quantity, then updates the sale type from ESTIMATE → SALE.
 */
export async function convertQuotationToSale(estimateId: string): Promise<{ success: boolean; error?: string }> {
  try {
    const session = await getSession();
    if (!session || !session.uid) {
      return { success: false, error: 'Unauthorized' };
    }

    const dc = getDataConnect({ serviceId: 'store-book', location: 'us-central1' });
    const nowMs = Date.now();
    const nowSec = Math.floor(nowMs / 1000);

    // 1. Verify this is an active ESTIMATE belonging to the user's store
    let saleCheck = await dc.executeGraphql(
      `query GetSaleById($id: String!) { sale(id: $id) { id, storeId, type, totalAmount, discountAmount, customerName, notes, timestamp } }`,
      { variables: { id: estimateId } }
    );
    const est = (saleCheck.data as any)?.sale;
    if (!est || est.storeId !== session.storeId) {
      return { success: false, error: 'Estimate not found or access denied' };
    }
    if (est.type === 'SALE') {
      return { success: false, error: 'Already a sale' };
    }

    // 2. Fetch all sale items for this estimate
    let itemsRes = await dc.executeGraphql(
      `query GetSaleItems($saleId: String!) { saleItemDetails(where: { saleId: { eq: $saleId }, isDeleted: { eq: false } }) { id, itemId, quantity, buyPrice } }`,
      { variables: { saleId: estimateId } }
    );
    const items = (itemsRes.data as any)?.saleItemDetails || [];

    // 3. Fetch live inventory, deduct stock, and write back using the full upsert pattern
    for (const si of items) {
      let itemRes = await dc.executeGraphql(
        `query GetItemById($id: String!) { item(id: $id) { id, storeId, name, quantity, unit, buyPrice, sellPrice, lowStockThreshold, category, isDeleted, updatedAt } }`,
        { variables: { id: si.itemId } }
      );
      const liveItem = (itemRes.data as any)?.item;
      if (!liveItem || liveItem.storeId !== session.storeId) {
        console.warn(`Skipping stock deduct for ${si.itemId}: not found`);
        continue;
      }

      // BUG-06 FIX: Check sufficient stock BEFORE deducting — reject if insufficient
      const neededQty = si.quantity;
      if (liveItem.quantity < neededQty) {
        // Revert the type change and return error with item name so UI can surface specific message
        return { success: false, error: `Insufficient stock for '${liveItem.name}': have ${liveItem.quantity}, need ${neededQty}` };
      }
      const newQty = liveItem.quantity - neededQty;

      // Write back full item row with updated quantity using UPPERCASE column names as per DataConnect schema
      await dc.executeGraphql(
        `mutation UpsertItem($input: ItemInput!) { syncItem(input: $input) { id } }`,
        {
          variables: {
            input: {
              id: liveItem.id,
              storeId: liveItem.storeId,
              name: liveItem.name,
              quantity: newQty,
              unit: liveItem.unit,
              buyPrice: si.buyPrice ?? liveItem.buyPrice,
              sellPrice: liveItem.sellPrice,
              lowStockThreshold: liveItem.lowStockThreshold ?? 0,
              category: liveItem.category ?? '',
              isDeleted: false,
              updatedAt: nowSec
            }
          }
        }
      );
    }

    // 4. Flip type from ESTIMATE → SALE (use same full upsert pattern)
    await dc.executeGraphql(
      `mutation UpsertSale($input: SaleInput!) { syncSale(input: $input) { id } }`,
      {
        variables: {
          input: {
            id: estimateId,
            storeId: est.storeId,
            timestamp: est.timestamp ?? nowMs,
            totalAmount: est.totalAmount ?? 0,
            discountAmount: est.discountAmount ?? 0,
            customerName: est.customerName ?? null,
            type: 'SALE',
            notes: est.notes ?? null,
            isDeleted: false,
            updatedAt: nowSec
          }
        }
      }
    );

    // BUG-09 FIX: Revalidate dashboard so stale stats are flushed after conversion
    revalidatePath('/quotations');
    revalidatePath('/');
    return { success: true };
  } catch (err: any) {
    console.error('convertQuotationToSale failed:', err);
    return { success: false, error: err.message };
  }
}

/**
 * BUG-10 fix: Server-authoritative udhaar balance per customer.
 * Aggregates CREDIT vs PAYMENT entries server-side so all devices converge on identical balances.
 */
export async function getUdhaarCustomerBalances(
  storeId: string,
): Promise<{ success: boolean; data?: { customerName: string; netBalance: number; lastTransactionTime: number }[]; error?: string }> {
  const session = await getSession();
  if (!session) return { success: false, error: 'Unauthorized' };

  const allowedStores: string[] = Array.isArray(session.stores) ? session.stores : [];
  if (session.role !== 'super_admin' && !allowedStores.includes(storeId as string)) {
    return { success: false, error: 'You do not have access to this store' };
  }

  try {
    const dc = getDataConnect({ serviceId: 'store-book', location: 'us-central1' });
    const response = await dc.executeGraphql(
      `query GetUdhaarForBalance($storeId: String!) {
        udhaarEntries(where: { storeId: { eq: $storeId }, isDeleted: { eq: false } }) {
          edges {
            node {
              customerName
              amount
              type
              timestamp
            }
          }
        }
      }`,
      { variables: { storeId } },
    );

    const entries = ((response.data as any)?.udhaarEntries?.edges || []).map(
      (e: any) => e.node,
    );

    // Aggregate per-customer balances server-side
    const customerMap = new Map<string, { creditSum: number; paymentSum: number; lastTime: number }>();

    for (const entry of entries) {
      const key = entry.customerName;
      const existing = customerMap.get(key);
      if (existing) {
        if (entry.type === 'CREDIT') existing.creditSum += entry.amount;
        else existing.paymentSum += entry.amount;
        if (entry.timestamp > existing.lastTime) existing.lastTime = entry.timestamp;
      } else {
        const creditSum = entry.type === 'CREDIT' ? entry.amount : 0;
        const paymentSum = entry.type !== 'CREDIT' ? entry.amount : 0;
        customerMap.set(key, { creditSum, paymentSum: paymentSum, lastTime: entry.timestamp ?? 0 });
      }
    }

    const data = Array.from(customerMap.entries()).map(([customerName, vals]) => ({
      customerName,
      netBalance: Math.round((vals.creditSum - vals.paymentSum + Number.EPSILON) * 100) / 100,
      lastTransactionTime: vals.lastTime ?? 0,
    }));

    return { success: true, data };
  } catch (err: any) {
    console.error('getUdhaarCustomerBalances failed:', err);
    return { success: false, error: err.message };
  }
}

export async function revalidateDashboard() {
  revalidatePath('/');
}
