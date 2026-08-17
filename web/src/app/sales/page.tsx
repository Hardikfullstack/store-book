import SalesClient from "./SalesClient";
import { getSession } from "@/lib/session";
import { resolvePermissions } from "@/lib/roleMatrix";

export default async function SalesPage() {
    const session = await getSession();
    if (!session) return <div>Please login</div>;

    const perms = resolvePermissions(session.role ?? "staff");

    const sales: any[] = [];
    const isPremium = session.isPremium;

    return (
        <SalesClient
            initialSales={sales}
            maxDiscountPercent={perms.maxDiscountPercent}
            canDeleteRecords={perms.canDeleteRecords}
            storeId={session.storeId}
            isPremium={isPremium}
        />
    );
}
