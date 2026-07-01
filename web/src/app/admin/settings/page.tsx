import { getSession } from '@/lib/session';
import SettingsClient from './SettingsClient';
import { redirect } from 'next/navigation';
import { getDataConnect } from 'firebase-admin/data-connect';

export default async function AdminSettingsPage() {
  const session = await getSession();
  if (!session || (session.role !== 'admin' && session.role !== 'super_admin')) {
    redirect('/');
  }

  // Fetch initial data for the settings dashboard
  try {
    const dc = getDataConnect({ serviceId: 'store-book', location: 'us-central1' });
    const [settingsRes, promosRes, announcementsRes] = await Promise.all([
      dc.executeGraphql('query GetGlobalSettings { globalSettings { id key value description } }', {}),
      dc.executeGraphql('query GetPromoCodes { promoCodes { id code discountPercent discountAmount maxUses currentUses expiresAt isActive } }', {}),
      dc.executeGraphql('query GetAnnouncements { announcements { id title message type isActive createdAt } }', {})
    ]);

    return (
      <SettingsClient 
        initialSettings={settingsRes.data?.globalSettings || []}
        initialPromoCodes={promosRes.data?.promoCodes || []}
        initialAnnouncements={announcementsRes.data?.announcements || []}
      />
    );
  } catch (error) {
    console.error("Failed to fetch admin settings:", error);
    return <SettingsClient initialSettings={[]} initialPromoCodes={[]} initialAnnouncements={[]} />;
  }
}
