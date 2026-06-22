'use client';

import { useState } from 'react';
import { Settings, XCircle } from 'lucide-react';

export default function ManageSubscription({ platform }: { platform: string }) {
  const [loading, setLoading] = useState(false);

  const handleCancel = async () => {
    if (!confirm('Are you sure you want to cancel your Pro Subscription? You will lose access to premium features immediately.')) return;

    setLoading(true);
    try {
      const res = await fetch('/api/cancel-subscription', {
        method: 'POST',
      });
      const data = await res.json();
      
      if (data.success) {
        alert('Your subscription has been cancelled.');
        window.location.reload();
      } else {
        alert('Failed to cancel subscription: ' + data.error);
      }
    } catch (err) {
      console.error(err);
      alert('An error occurred while cancelling.');
    } finally {
      setLoading(false);
    }
  };

  const handleManage = () => {
    if (platform === 'play_store' || platform === 'android') {
      alert("Please manage your subscription from the Google Play Store on your Android device.");
    } else {
      // For web, we simulate opening a Razorpay customer portal or billing dashboard
      alert("Redirecting to Razorpay Billing Portal... (Simulated)");
    }
  };

  return (
    <div className="mt-4 flex flex-col sm:flex-row gap-3">
      <button
        onClick={handleManage}
        className="flex-1 flex items-center justify-center px-4 py-2 border border-gray-300 dark:border-gray-600 text-sm font-medium rounded-md text-gray-700 dark:text-gray-200 bg-white dark:bg-gray-800 hover:bg-gray-50 dark:hover:bg-gray-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-amber-500 shadow-sm"
      >
        <Settings size={18} className="mr-2" />
        Manage Plan
      </button>
      <button
        onClick={handleCancel}
        disabled={loading}
        className="flex-1 flex items-center justify-center px-4 py-2 border border-transparent text-sm font-medium rounded-md text-white bg-red-600 hover:bg-red-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-red-500 shadow-sm disabled:opacity-50"
      >
        <XCircle size={18} className="mr-2" />
        {loading ? 'Cancelling...' : 'Cancel Subscription'}
      </button>
    </div>
  );
}
