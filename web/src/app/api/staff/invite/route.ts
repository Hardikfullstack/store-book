import { NextResponse } from 'next/server';
import { adminAuth, adminDb } from '@/lib/firebaseAdmin';

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

      // Save to Firestore
      await adminDb.collection('users').doc(userRecord.uid).set({
        username: username,
        role: 'staff',
        storeId: storeId,
        ownerId: ownerId,
        createdAt: new Date().getTime(),
      });

      return NextResponse.json({ success: true, uid: userRecord.uid, username });
    } catch (authError: any) {
      return NextResponse.json({ error: authError.message || 'Auth error' }, { status: 400 });
    }
  } catch (error) {
    console.error('Error creating staff:', error);
    return NextResponse.json({ error: 'Internal server error' }, { status: 500 });
  }
}
