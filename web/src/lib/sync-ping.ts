import { ref, set } from 'firebase/database';
import { rtdb } from '@/lib/firebase';

export async function pingDashboardStore(storeId: string) {
  await set(ref(rtdb, `store_updates/${storeId}/last_update`), Date.now());
}
