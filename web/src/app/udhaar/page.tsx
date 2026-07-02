import UdhaarClient from './UdhaarClient';
import { getSession } from '@/lib/session';

export default async function UdhaarPage() {
  const session = await getSession();
  if (!session) return <div>Please login</div>;
  
  const udhaar: any[] = [];
  const storeName = "Your Store";
  const isPremium = true;

  return (
    <UdhaarClient 
      initialUdhaar={udhaar} 
      storeName={storeName}
      storeId={session.storeId}
      isPremium={isPremium}
    />
  );
}
