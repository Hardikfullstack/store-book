import ItemsClient from './ItemsClient';
import { getSession } from '@/lib/session';

export default async function ItemsPage() {
  const session = await getSession();
  if (!session) return <div>Please login</div>;
  
  const items: any[] = [];
  const isPremium = true;

  return (
    <ItemsClient 
      initialItems={items} 
      userRole={session.role} 
      storeId={session.storeId} 
      isPremium={isPremium} 
    />
  );
}
