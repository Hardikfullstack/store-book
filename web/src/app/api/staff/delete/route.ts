import { NextResponse } from "next/server";
import { adminAuth } from "@/lib/firebaseAdmin";
import { getDataConnect } from "firebase-admin/data-connect";
import { requirePermission } from "@/lib/permissions";

export async function POST(request: Request) {
    try {
        const session = await requirePermission("canManageStaff");

        const body = await request.json();
        const { uid, ownerId } = body;

        if (!uid) {
            return NextResponse.json(
                { error: "Missing required fields" },
                { status: 400 },
            );
        }

        if (session.role === "owner" && ownerId !== session.uid) {
            return NextResponse.json(
                {
                    error: "Unauthorized — caller must be the account owner",
                },
                { status: 403 },
            );
        }

        const dc = getDataConnect({
            serviceId: "store-book",
            location: "us-central1",
        });

        const staffRes: { data: { user?: { storeId: string; ownerId: string } }, errors?: unknown[] } = await dc.executeGraphql(
            `query GetUser($id: String!) { user(id: $id) { storeId ownerId } }`,
            { variables: { id: uid } },
        );

        if (!staffRes.data.user || staffRes.data.user.ownerId !== ownerId) {
            return NextResponse.json(
                {
                    error: "Unauthorized to delete this staff account",
                },
                { status: 403 },
            );
        }

        await adminAuth.deleteUser(uid);

        await dc.executeGraphql(
            `mutation DeleteUser($id: String!) { user_delete(id: $id) }`,
            { variables: { id: uid } },
        );

        return NextResponse.json({ success: true });
    } catch (error) {
        console.error("Error deleting staff:", error);
        return NextResponse.json(
            { error: "Internal server error" },
            { status: 500 },
        );
    }
}
