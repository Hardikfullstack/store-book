import { NextResponse } from 'next/server';
import crypto from 'crypto';
import { getSession } from '@/lib/session';
import { getDataConnect } from 'firebase-admin/data-connect';

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

    const dc = getDataConnect({ serviceId: 'store-book', location: 'us-central1' });

    await dc.executeGraphql(
      `mutation UpdateStorePayment($id: String!, $expiresAt: Float!, $subId: String!) { 
         store_update(id: $id, data: { isPremium: true, subscriptionExpiresAt: $expiresAt, subscriptionPlatform: "web", subscriptionId: $subId, subscriptionStatus: "active" }) 
       }`,
      { variables: { id: session.storeId, expiresAt: expiresAt, subId: razorpay_subscription_id } }
    );

    await dc.executeGraphql(
      `mutation UpdateUserPayment($id: String!, $expiresAt: Float!, $subId: String!) { 
         user_update(id: $id, data: { subscriptionStatus: "active", subscriptionPlan: "pro", subscriptionExpiresAt: $expiresAt, subscriptionPlatform: "web", subscriptionId: $subId }) 
       }`,
      { variables: { id: session.docId, expiresAt: expiresAt, subId: razorpay_subscription_id } }
    );

    return NextResponse.json({ success: true });
  } catch (error) {
    console.error('Error verifying payment:', error);
    return NextResponse.json({ error: 'Internal server error' }, { status: 500 });
  }
}
