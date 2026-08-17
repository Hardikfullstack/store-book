import UdhaarClient from './UdhaarClient';
import { redirect } from 'next/navigation';
import { requirePermission } from '@/lib/permissions';

export default async function UdhaarPage() {
  let session;
  try {
    session = await requirePermission('canViewUdhaar');
  } catch {
    return redirect('/login');
  }

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
