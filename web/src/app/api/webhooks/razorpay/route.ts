import { NextResponse } from 'next/server';
import crypto from 'crypto';
import { adminDb } from '@/lib/firebaseAdmin';
import { FieldValue } from 'firebase-admin/firestore';

export async function POST(request: Request) {
  try {
    const bodyText = await request.text();
    const signature = request.headers.get('x-razorpay-signature');

    const webhookSecret = process.env.RAZORPAY_WEBHOOK_SECRET;

    if (!webhookSecret) {
      console.error("RAZORPAY_WEBHOOK_SECRET is not configured.");
      return NextResponse.json({ error: 'Webhook secret not configured' }, { status: 500 });
    }

    if (!signature) {
      return NextResponse.json({ error: 'No signature provided' }, { status: 400 });
    }

    // Verify webhook signature
    const expectedSignature = crypto
      .createHmac('sha256', webhookSecret)
      .update(bodyText)
      .digest('hex');

    if (expectedSignature !== signature) {
      console.error("Invalid webhook signature.");
      return NextResponse.json({ error: 'Invalid signature' }, { status: 400 });
    }

    const event = JSON.parse(bodyText);

    // Identify the user based on subscription notes, or we can look up the user by subscription_id if we stored it
    // Wait, Razorpay subscriptions don't inherently know our user's ID unless we pass it in notes during subscription creation.
    // For now, let's assume we update Firestore by querying for the user with this subscription ID.
    
    // We will handle specific events
    if (event.event === 'subscription.charged') {
      const subscriptionId = event.payload.subscription.entity.id;
      // Handle renewal
      await handleSubscriptionRenewal(subscriptionId);
    } else if (event.event === 'subscription.halted' || event.event === 'subscription.cancelled') {
      const subscriptionId = event.payload.subscription.entity.id;
      // Handle cancellation or failure
      await handleSubscriptionCancellation(subscriptionId);
    }

    return NextResponse.json({ status: 'ok' });

  } catch (error) {
    console.error('Webhook error:', error);
    return NextResponse.json({ error: 'Webhook processing failed' }, { status: 500 });
  }
}

async function handleSubscriptionRenewal(subscriptionId: string) {
  // Query stores collection for this subscription_id
  const storesSnapshot = await adminDb.collection('stores').where('subscription_id', '==', subscriptionId).get();
  
  if (storesSnapshot.empty) {
    console.error(`Webhook: No store found with subscription_id ${subscriptionId}`);
    return;
  }

  const storeDoc = storesSnapshot.docs[0];
  const newExpiresAt = Date.now() + (31 * 24 * 60 * 60 * 1000); // Add ~1 month

  await storeDoc.ref.update({
    is_premium: true,
    subscription_expires_at: newExpiresAt,
    subscription_status: 'active'
  });
}

async function handleSubscriptionCancellation(subscriptionId: string) {
  // Query stores collection for this subscription_id
  const storesSnapshot = await adminDb.collection('stores').where('subscription_id', '==', subscriptionId).get();
  
  if (storesSnapshot.empty) {
    console.error(`Webhook: No store found with subscription_id ${subscriptionId}`);
    return;
  }

  const storeDoc = storesSnapshot.docs[0];

  await storeDoc.ref.update({
    is_premium: false,
    subscription_status: 'cancelled',
    subscription_expires_at: FieldValue.delete()
  });
}
