import { NextResponse } from 'next/server';
import { adminAuth, adminDb } from '@/lib/firebaseAdmin';

export async function POST(request: Request) {
  try {
    const body = await request.json();
    const { uid, newPassword, ownerId } = body;

    if (!uid || !newPassword || !ownerId) {
      return NextResponse.json({ error: 'Missing required fields' }, { status: 400 });
    }

    // Verify owner has access to this staff (basic security check)
    const staffDoc = await adminDb.collection('users').doc(uid).get();
    if (!staffDoc.exists || staffDoc.data()?.ownerId !== ownerId) {
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
