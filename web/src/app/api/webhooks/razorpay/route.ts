import { NextResponse } from 'next/server';
import crypto from 'crypto';
import { getDataConnect } from 'firebase-admin/data-connect';

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
  const dc = getDataConnect({ serviceId: 'store-book', location: 'us-central1' });
  
  // Query stores collection for this subscription_id
  const res = await dc.executeGraphql(
    `query GetStoreBySub($subId: String!) { stores(where: { subscriptionId: { eq: $subId } }, limit: 1) { id } }`,
    { variables: { subId: subscriptionId } }
  ) as any;
  
  if (!res.data.stores || res.data.stores.length === 0) {
    console.error(`Webhook: No store found with subscription_id ${subscriptionId}`);
    return;
  }

  const storeId = res.data.stores[0].id;
  const newExpiresAt = Date.now() + (31 * 24 * 60 * 60 * 1000); // Add ~1 month

  await dc.executeGraphql(
    `mutation RenewStore($id: String!, $expiresAt: Float!) { 
       store_update(id: $id, data: { isPremium: true, subscriptionExpiresAt: $expiresAt, subscriptionStatus: "active" }) 
     }`,
    { variables: { id: storeId, expiresAt: newExpiresAt } }
  );
}

async function handleSubscriptionCancellation(subscriptionId: string) {
  const dc = getDataConnect({ serviceId: 'store-book', location: 'us-central1' });

  // Query stores collection for this subscription_id
  const res = await dc.executeGraphql(
    `query GetStoreBySub($subId: String!) { stores(where: { subscriptionId: { eq: $subId } }, limit: 1) { id } }`,
    { variables: { subId: subscriptionId } }
  ) as any;
  
  if (!res.data.stores || res.data.stores.length === 0) {
    console.error(`Webhook: No store found with subscription_id ${subscriptionId}`);
    return;
  }

  const storeId = res.data.stores[0].id;

  await dc.executeGraphql(
    `mutation HaltStore($id: String!) { 
       store_update(id: $id, data: { isPremium: false, subscriptionStatus: "cancelled", subscriptionExpiresAt: null }) 
     }`,
    { variables: { id: storeId } }
  );
}
