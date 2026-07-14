import ExpensesClient from './ExpensesClient';
import { getSession } from '@/lib/session';
import { redirect } from 'next/navigation';

export default async function ExpensesPage() {
  const session = await getSession();
  if (!session) return <div>Please login</div>;
  // E04-S2: Staff cannot view Expenses
  if (session.role === 'staff') redirect('/');
  // E04-S2: Staff/cashier cannot view Expenses
  if (session.role === 'staff') redirect('/');
  
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


export default async function ExpensesPage() {
  const session = await getSession();
  if (!session) return <div>Please login</div>;
  if (session.role === 'staff') redirect('/');
  if (session.role === 'staff') redirect('/');
  
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
