import { NextResponse } from 'next/server';
import { adminDb } from '@/lib/firebaseAdmin';
import { getSession } from '@/lib/session';
import { FieldValue } from 'firebase-admin/firestore';

export async function POST(request: Request) {
  try {
    const session = await getSession();
    if (!session) return NextResponse.json({ error: 'Unauthorized' }, { status: 401 });

    // In a real application with Razorpay Subscriptions, we would call:
    // razorpay.subscriptions.cancel(subscription_id)
    // For this demonstration (since we used simulated one-time orders), 
    // we will directly downgrade the user's account in Firebase.

    // 1. Update Store Document
    await adminDb.collection('stores').doc(session.storeId).update({
      is_premium: false,
      subscription_expires_at: FieldValue.delete(),
      subscription_platform: FieldValue.delete()
    });

    // 2. Update User Document
    await adminDb.collection('users').doc(session.docId).update({
      subscription: {
        status: 'inactive',
        plan: 'free',
        cancelledAt: Date.now()
      }
    });

    return NextResponse.json({ success: true, message: 'Subscription cancelled successfully' });
  } catch (error) {
    console.error('Error cancelling subscription:', error);
    return NextResponse.json({ error: 'Internal server error' }, { status: 500 });
  }
}
