import { NextResponse } from 'next/server';
import { adminAuth } from '@/lib/firebaseAdmin';
import { getDataConnect } from 'firebase-admin/data-connect';

import { getSession } from '@/lib/session';

export async function POST(request: Request) {
  try {
    // E04-S2: Only owner or admin can manage staff accounts
    const session = await getSession();
    if (!session) {
      return NextResponse.json({ error: 'Unauthenticated' }, { status: 401 });
    }
    if (session.role !== 'owner' && session.role !== 'admin' && session.role !== 'super_admin') {
      return NextResponse.json({ error: 'Forbidden — insufficient permissions' }, { status: 403 });
    }
    const body = await request.json();
    const { uid, newPassword, ownerId } = body;

    if (!uid || !newPassword || !ownerId) {
      return NextResponse.json({ error: 'Missing required fields' }, { status: 400 });
    }

    // Verify the caller is actually the owner of this staff's store
    if (session.role === 'owner' && ownerId !== session.uid) {
      return NextResponse.json({ error: 'Unauthorized — caller must be the account owner' }, { status: 403 });
    }

    const dc = getDataConnect({ serviceId: 'store-book', location: 'us-central1' });

    // Verify owner has access to this staff (basic security check)
    const staffRes = await dc.executeGraphql(
      `query GetUser($id: String!) { user(id: $id) { ownerId } }`,
      { variables: { id: uid } }
    ) as any;

    if (!staffRes.data.user || staffRes.data.user.ownerId !== ownerId) {
       return NextResponse.json({ error: 'Unauthorized to modify this staff account' }, { status: 403 });
    }

    // Update password
    await adminAuth.updateUser(uid, {
      password: newPassword,
    });

    return NextResponse.json({ success: true });
  } catch (error) {
    console.error('Error updating staff password:', error);
    return NextResponse.json({ error: 'Internal server error' }, { status: 500 });
  }
}
