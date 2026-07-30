import { NextResponse } from 'next/server';
import Razorpay from 'razorpay';
import { getSession } from '@/lib/session';

export async function POST(request: Request) {
  try {
    // Only the account owner may initiate a paid subscription
    const session = await getSession();
    if (!session) return NextResponse.json({ error: 'Unauthorized' }, { status: 401 });
    if (session.role !== 'owner') return NextResponse.json({ error: 'Forbidden — only owners may create subscriptions' }, { status: 403 });

    // Prevent duplicate subscription creation while one is already active
    if (session.isPremium) {
      return NextResponse.json({ error: 'A premium subscription is already active for this store' }, { status: 409 });
    }

    const key_id = process.env.RAZORPAY_KEY_ID;
    const key_secret = process.env.RAZORPAY_KEY_SECRET;

    if (!key_id || !key_secret) {
        return NextResponse.json({ error: 'Razorpay keys are not configured.' }, { status: 500 });
    }

    const razorpay = new Razorpay({
      key_id,
      key_secret,
    });

    // RAZORPAY_PRO_PLAN_ID must be set in .env.local to the Plan ID created in the Razorpay Dashboard.
    const planId = process.env.RAZORPAY_PRO_PLAN_ID;
    if (!planId) {
      return NextResponse.json({ error: 'RAZORPAY_PRO_PLAN_ID is not configured in environment variables.' }, { status: 500 });
    }

    // Create a subscription (recurring billing)
    const subscription = await razorpay.subscriptions.create({
      plan_id: planId,
      customer_notify: 1,
      total_count: 120, // 10 years by default for a monthly plan
    });

    return NextResponse.json({ success: true, subscription });

  } catch (error) {
    console.error('Error creating subscription:', error);
    return NextResponse.json({ error: 'Internal server error' }, { status: 500 });
  }
}
