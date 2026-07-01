import ExpensesClient from './ExpensesClient';
import { getSession } from '@/lib/session';

export default async function ExpensesPage() {
  const session = await getSession();
  if (!session) return <div>Please login</div>;
  
  const expenses: any[] = [];
  const isPremium = true;

  return (
    <ExpensesClient 
      initialExpenses={expenses} 
      storeId={session.storeId}
      isPremium={isPremium}
    />
  );
}
