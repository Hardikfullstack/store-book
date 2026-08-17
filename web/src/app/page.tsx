import { getSession } from "@/lib/session";
import DashboardClient from "./DashboardClient";
import { resolvePermissions } from "@/lib/roleMatrix";

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

    return (
        <DashboardClient
            initialStats={stats}
            canAccessCost={perms.canViewProfit}
            isAdmin={perms.canAccessAdmin}
            storeId={session.storeId}
            isPremium={isPremium}
        />
    );
}
