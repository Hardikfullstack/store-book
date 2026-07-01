import { getSession } from '@/lib/session';
import DataClient from './DataClient';
import { redirect } from 'next/navigation';
import { getDataConnect } from 'firebase-admin/data-connect';

export default async function AdminDataPage() {
  const session = await getSession();
  if (!session || (session.role !== 'admin' && session.role !== 'super_admin')) {
    redirect('/');
  }

  try {
    const dc = getDataConnect({ serviceId: 'store-book', location: 'us-central1' });
    const auditRes = await dc.executeGraphql('query GetAdminAuditLogs { adminAuditLogs { id adminId adminUsername action targetId details timestamp } }', {});
    return <DataClient initialAuditLogs={auditRes.data?.adminAuditLogs || []} />;
  } catch (error) {
    console.error("Failed to fetch audit logs:", error);
    return <DataClient initialAuditLogs={[]} />;
  }
}
