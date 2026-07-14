import ExpensesClient from './ExpensesClient';
import { getSession } from '@/lib/session';
import { redirect } from 'next/navigation';

export default async function ExpensesPage() {
  const session = await getSession();
  if (!session) return <div>Please login</div>;
  // E04-S2: Staff/cashier cannot view Expenses
  if (session.role === 'staff' || session.role === 'cashier') redirect('/');

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
