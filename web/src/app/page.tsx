import { getSession } from "@/lib/session";
import DashboardClient from "./DashboardClient";
import { resolvePermissions } from "@/lib/roleMatrix";
import { getConsolidatedPLData } from "./actions";
import type { ConsolidatedPLResult } from "@/types/dataconnect";

export default async function DashboardPage() {
    const session = await getSession();
    if (!session) return <div>Please login</div>;

    const stats = {
        totalItems: 0,
        totalSales: 0,
        totalUdhaar: 0,
        totalExpenses: 0,
        totalStores: 0,
        salesData: [],
        itemsData: [],
        saleItemsData: [],
    };

    const isPremium = session.isPremium;
    const perms = resolvePermissions(session.role ?? "staff");
    const isConsolidatedMode = session.isConsolidatedMode ?? false;

    let consolidatedPL: ConsolidatedPLResult | null = null;
    if (isConsolidatedMode && isPremium) {
        const result = await getConsolidatedPLData(session.stores, 30);
        if (result.success && result.data) {
            consolidatedPL = result.data;
        }
    }

    return (
        <DashboardClient
            initialStats={stats}
            canAccessCost={perms.canViewProfit}
            isAdmin={perms.canAccessAdmin}
            storeId={session.storeId}
            isPremium={isPremium}
            isConsolidatedMode={isConsolidatedMode}
            consolidatedPL={consolidatedPL}
        />
    );
}
