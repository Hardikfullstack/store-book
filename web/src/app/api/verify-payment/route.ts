import { NextResponse } from 'next/server';
import crypto from 'crypto';
import { adminDb } from '@/lib/firebaseAdmin';
import { getSession } from '@/lib/session';

export async function POST(request: Request) {
  try {
    const session = await getSession();
    if (!session) return NextResponse.json({ error: 'Unauthorized' }, { status: 401 });

    const body = await request.json();
    const { razorpay_subscription_id, razorpay_payment_id, razorpay_signature } = body;

    const secret = process.env.RAZORPAY_KEY_SECRET;
    if (!secret) {
        return NextResponse.json({ error: 'Razorpay secret not configured' }, { status: 500 });
    }
    
    // Verify signature for Subscriptions
    // Format: razorpay_payment_id + "|" + razorpay_subscription_id
    const generated_signature = crypto.createHmac('sha256', secret)
      .update(razorpay_payment_id + "|" + razorpay_subscription_id)
      .digest('hex');

    if (generated_signature !== razorpay_signature) {
      console.error("Invalid Razorpay signature during verification.");
      return NextResponse.json({ error: 'Invalid signature' }, { status: 400 });
    }

    // 1 Month Subscription initial setup (will be extended by webhooks on subsequent charges)
    const expiresAt = Date.now() + (31 * 24 * 60 * 60 * 1000); 

    await adminDb.collection('stores').doc(session.storeId).update({
      is_premium: true,
      subscription_expires_at: expiresAt,
      subscription_platform: 'web',
      subscription_id: razorpay_subscription_id,
      subscription_status: 'active'
    });

    await adminDb.collection('users').doc(session.docId).update({
      subscription: {
        status: 'active',
        plan: 'pro',
        expiresAt: expiresAt,
        platform: 'web',
        subscriptionId: razorpay_subscription_id
      }
    });

    return NextResponse.json({ success: true });
  } catch (error) {
    console.error('Error verifying payment:', error);
    return NextResponse.json({ error: 'Internal server error' }, { status: 500 });
  }
}
