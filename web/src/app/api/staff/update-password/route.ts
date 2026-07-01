import { NextResponse } from 'next/server';
import { adminAuth } from '@/lib/firebaseAdmin';
import { getDataConnect } from 'firebase-admin/data-connect';

export async function POST(request: Request) {
  try {
    const body = await request.json();
    const { uid, newPassword, ownerId } = body;

    if (!uid || !newPassword || !ownerId) {
      return NextResponse.json({ error: 'Missing required fields' }, { status: 400 });
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
