import { NextResponse } from 'next/server';
import { getSession } from '@/lib/session';
import { getDataConnect } from 'firebase-admin/data-connect';

export async function POST(request: Request) {
  try {
    const session = await getSession();
    if (!session) return NextResponse.json({ error: 'Unauthorized' }, { status: 401 });
    
    // Only the owner of a store can cancel its subscription
    if (session.role !== 'owner') return NextResponse.json({ error: 'Forbidden — only owners may cancel subscriptions' }, { status: 403 });

    const dc = getDataConnect({ serviceId: 'store-book', location: 'us-central1' });

    // 1. Update Store Document
    await dc.executeGraphql(
      `mutation CancelStore($id: String!) { store_update(id: $id, data: { isPremium: false, subscriptionExpiresAt: null, subscriptionPlatform: null, subscriptionStatus: "cancelled", subscriptionId: null }) }`,
      { variables: { id: session.storeId } }
    );

    // 2. Update User Document
    await dc.executeGraphql(
      `mutation CancelUser($id: String!) { user_update(id: $id, data: { subscriptionStatus: "inactive", subscriptionPlan: "free", subscriptionExpiresAt: null }) }`,
      { variables: { id: session.docId } }
    );

    return NextResponse.json({ success: true, message: 'Subscription cancelled successfully' });
  } catch (error) {
    console.error('Error cancelling subscription:', error);
    return NextResponse.json({ error: 'Internal server error' }, { status: 500 });
  }
}
