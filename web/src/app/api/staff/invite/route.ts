import { NextResponse } from 'next/server';
import { adminAuth } from '@/lib/firebaseAdmin';
import { getDataConnect } from 'firebase-admin/data-connect';

export async function POST(request: Request) {
  try {
    const body = await request.json();
    const { username, password, storeId, ownerId } = body;

    if (!username || !password || !storeId || !ownerId) {
      return NextResponse.json({ error: 'Missing required fields' }, { status: 400 });
    }

    // Since Firebase Auth requires an email, we create a dummy one
    const dummyEmail = `${username.toLowerCase().replace(/[^a-z0-9]/g, '')}@storebook.internal`;

    try {
      // Create user in Firebase Auth
      const userRecord = await adminAuth.createUser({
        email: dummyEmail,
        password: password,
        displayName: username,
      });

      // Save to DataConnect
      const dc = getDataConnect({ serviceId: 'store-book', location: 'us-central1' });
      await dc.executeGraphql(
        `mutation CreateStaff($id: String!, $username: String!, $role: String!, $storeId: String!, $ownerId: String!, $createdAt: Float!) {
          user_upsert(data: { id: $id, username: $username, role: $role, storeId: $storeId, ownerId: $ownerId, createdAt: $createdAt }) { id }
        }`,
        { variables: { 
            id: userRecord.uid, 
            username, 
            role: 'staff', 
            storeId, 
            ownerId, 
            createdAt: Date.now() 
        } }
      );

      return NextResponse.json({ success: true, uid: userRecord.uid, username });
    } catch (authError: any) {
      return NextResponse.json({ error: authError.message || 'Auth error' }, { status: 400 });
    }
  } catch (error) {
    console.error('Error creating staff:', error);
    return NextResponse.json({ error: 'Internal server error' }, { status: 500 });
  }
}
