import UdhaarClient from './UdhaarClient';
import { getSession } from '@/lib/session';
import { redirect } from 'next/navigation';

export default async function UdhaarPage() {
  const session = await getSession();
  if (!session) return <div>Please login</div>;
  // E04-S2: Staff/cashier cannot view Udhaar
  if (session.role === 'staff' || session.role === 'cashier') redirect('/');

  const udhaar: any[] = [];
  const storeName = "Your Store";
  const isPremium = session.isPremium;

  return (
    <UdhaarClient
      initialUdhaar={udhaar}
      storeName={storeName}
      storeId={session.storeId}
      isPremium={isPremium}
    />
  );
}
