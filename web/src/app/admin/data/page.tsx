import DataClient from './DataClient';
import { redirect } from 'next/navigation';
import { getDataConnect } from 'firebase-admin/data-connect';
import { requirePermission } from '@/lib/permissions';

export default async function AdminDataPage() {
  try {
    await requirePermission('canAccessAdmin');
  } catch {
    return redirect('/');
  }

  try {
    const dc = getDataConnect({ serviceId: 'store-book', location: 'us-central1' });
    const auditRes = await dc.executeGraphql('query GetAdminAuditLogs { adminAuditLogs { id adminId adminUsername action targetId details timestamp } }', {});
    const raw = auditRes.data as unknown as Record<string, unknown>;
    const typedAuditLogs = Array.isArray(raw?.adminAuditLogs) ? raw.adminAuditLogs : [];
    /* eslint-disable react-hooks/error-boundaries */
    return <DataClient initialAuditLogs={typedAuditLogs ?? []} />;
    /* eslint-enable react-hooks/error-boundaries */
  } catch (error) {
    console.error("Failed to fetch audit logs:", error);
    return <DataClient initialAuditLogs={[]} />;
  }
}
