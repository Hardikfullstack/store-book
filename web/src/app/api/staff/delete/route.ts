import { NextResponse } from 'next/server';
import { adminAuth } from '@/lib/firebaseAdmin';
import { getDataConnect } from 'firebase-admin/data-connect';

export async function POST(request: Request) {
  try {
    const body = await request.json();
    const { uid, ownerId } = body;

    if (!uid || !ownerId) {
      return NextResponse.json({ error: 'Missing required fields' }, { status: 400 });
    }

    const dc = getDataConnect({ serviceId: 'store-book', location: 'us-central1' });

    // Verify owner has access to this staff (basic security check)
    const staffRes = await dc.executeGraphql(
      `query GetUser($id: String!) { user(id: $id) { ownerId } }`,
      { variables: { id: uid } }
    ) as any;
    
    if (!staffRes.data.user || staffRes.data.user.ownerId !== ownerId) {
       return NextResponse.json({ error: 'Unauthorized to delete this staff account' }, { status: 403 });
    }

    // Delete user from Firebase Auth
    await adminAuth.deleteUser(uid);

    // Delete user from Data Connect
    await dc.executeGraphql(
      `mutation DeleteUser($id: String!) { user_delete(id: $id) }`,
      { variables: { id: uid } }
    );

    return NextResponse.json({ success: true });
  } catch (error) {
    console.error('Error deleting staff:', error);
    return NextResponse.json({ error: 'Internal server error' }, { status: 500 });
  }
}
