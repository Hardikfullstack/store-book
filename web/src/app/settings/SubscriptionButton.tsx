'use client';

import { useState } from 'react';
import { CreditCard } from 'lucide-react';
import Script from 'next/script';

export default function SubscriptionButton() {
  const [loading, setLoading] = useState(false);

  const handleSubscribe = async () => {
    setLoading(true);
    try {
      // 1. Create Subscription
      const res = await fetch('/api/create-subscription', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ plan_id: "pro_plan" }) 
      });
      const data = await res.json();
      
      if (!data.success) {
        alert('Failed to initialize subscription: ' + (data.error || 'Unknown error'));
        setLoading(false);
        return;
      }

      // 2. Open Razorpay Checkout
      const options = {
        key: process.env.NEXT_PUBLIC_RAZORPAY_KEY_ID || 'rzp_test_dummy_key',
        subscription_id: data.subscription.id,
        name: "StoreBook Pro",
        description: "Monthly Premium Subscription",
        handler: async function (response: any) {
          // 3. Verify Payment
          const verifyRes = await fetch('/api/verify-payment', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
              razorpay_payment_id: response.razorpay_payment_id,
              razorpay_subscription_id: response.razorpay_subscription_id || data.subscription.id,
              razorpay_signature: response.razorpay_signature
            })
          });
          const verifyData = await verifyRes.json();
          if (verifyData.success) {
            alert('Payment successful! You are now a PRO user.');
            window.location.reload();
          } else {
            alert('Payment verification failed.');
          }
        },
        prefill: {
          name: "Store Owner",
          email: "owner@example.com",
          contact: "9999999999"
        },
        theme: {
          color: "#14b8a6" // teal-500
        }
      };

      if ((window as any).Razorpay) {
        const rzp = new (window as any).Razorpay(options);
        rzp.open();
      } else {
        alert("Razorpay SDK failed to load. Please check your internet connection.");
      }

    } catch (err) {
      console.error(err);
      alert('An error occurred during checkout.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <>
      <Script src="https://checkout.razorpay.com/v1/checkout.js" strategy="lazyOnload" />
      <button
        onClick={handleSubscribe}
        disabled={loading}
        className="mt-4 flex items-center justify-center w-full px-4 py-2 border border-transparent text-sm font-medium rounded-md text-white bg-amber-500 hover:bg-amber-600 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-amber-500 shadow-sm"
      >
        <CreditCard size={18} className="mr-2" />
        {loading ? 'Processing...' : 'Upgrade to PRO (₹999/yr)'}
      </button>
    </>
  );
}
