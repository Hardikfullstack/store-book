import ItemsClient from "./ItemsClient";
import { getSession } from "@/lib/session";
import { resolvePermissions } from "@/lib/roleMatrix";

export default async function ItemsPage() {
    const session = await getSession();
    if (!session) return <div>Please login</div>;

    const perms = resolvePermissions(session.role ?? "staff");
    const canAccessCost = perms.canViewProfit;
    const canDeleteRecords = perms.canDeleteRecords;

    const items: any[] = [];
    const isPremium = session.isPremium;

    return (
        <ItemsClient
            initialItems={items}
            canAccessCost={canAccessCost}
            canDeleteRecords={canDeleteRecords}
            storeId={session.storeId}
            isPremium={isPremium}
        />
    );
}
