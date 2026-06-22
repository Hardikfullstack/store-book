import { NextResponse } from 'next/server';
import { adminAuth, adminDb } from '@/lib/firebaseAdmin';

export async function POST(request: Request) {
  try {
    const body = await request.json();
    const { uid, ownerId } = body;

    if (!uid || !ownerId) {
      return NextResponse.json({ error: 'Missing required fields' }, { status: 400 });
    }

    // Verify owner has access to this staff (basic security check)
    const staffDoc = await adminDb.collection('users').doc(uid).get();
    if (!staffDoc.exists || staffDoc.data()?.ownerId !== ownerId) {
       return NextResponse.json({ error: 'Unauthorized to delete this staff account' }, { status: 403 });
    }

    // Delete user from Firebase Auth
    await adminAuth.deleteUser(uid);

    // Delete user from Firestore
    await adminDb.collection('users').doc(uid).delete();

    return NextResponse.json({ success: true });
  } catch (error) {
    console.error('Error deleting staff:', error);
    return NextResponse.json({ error: 'Internal server error' }, { status: 500 });
  }
}
