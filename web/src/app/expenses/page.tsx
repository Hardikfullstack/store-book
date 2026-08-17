import ExpensesClient from './ExpensesClient';
import { redirect } from 'next/navigation';
import { requirePermission } from '@/lib/permissions';

export default async function ExpensesPage() {
  let session;
  try {
    session = await requirePermission('canViewExpenses');
  } catch {
    return redirect('/login');
  }

  const expenses: any[] = [];
  const isPremium = session.isPremium;

  return (
    <ExpensesClient
      initialExpenses={expenses}
      storeId={session.storeId}
      isPremium={isPremium}
    />
  );
}
