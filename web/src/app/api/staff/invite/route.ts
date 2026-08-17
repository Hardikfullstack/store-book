import { NextResponse } from "next/server";
import { adminAuth } from "@/lib/firebaseAdmin";
import { getDataConnect } from "firebase-admin/data-connect";
import { requirePermission } from "@/lib/permissions";

export async function POST(request: Request) {
    try {
        const session = await requirePermission("canManageStaff");

        const body = await request.json();
        const {
            username,
            password,
            storeId,
            ownerId,
            role,
            canViewProfit,
            canDelete,
        } = body;

        if (!username || !password || !storeId) {
            return NextResponse.json(
                { error: "Missing required fields" },
                { status: 400 },
            );
        }

        if (session.role === "owner" && ownerId !== session.uid) {
            return NextResponse.json(
                { error: "Unauthorized — caller must be the account owner" },
                { status: 403 },
            );
        }

        const resolvedRole = ["cashier", "manager"].includes(role)
            ? role
            : "cashier";
        const dummyEmail = `${username.toLowerCase().replace(/[^a-z0-9]/g, "")}@storebook.internal`;

        try {
            const userRecord = await adminAuth.createUser({
                email: dummyEmail,
                password,
                displayName: username,
            });

            const dc = getDataConnect({
                serviceId: "store-book",
                location: "us-central1",
            });
            await dc.executeGraphql(
                `mutation CreateStaff($id: String!, $username: String!, $role: String!, $storeId: String!, $ownerId: String!, $createdAt: Float!, $canViewProfit: Boolean, $canDelete: Boolean) {
              user_upsert(data: { id: $id, username: $username, role: $role, storeId: $storeId, ownerId: $ownerId, createdAt: $createdAt, canViewProfit: $canViewProfit, canDelete: $canDelete }) { id }
            }`,
                {
                    variables: {
                        id: userRecord.uid,
                        username,
                        role: resolvedRole,
                        storeId,
                        ownerId,
                        createdAt: Math.floor(Date.now() / 1000),
                        canViewProfit: !!canViewProfit,
                        canDelete: !!canDelete,
                    },
                },
            );

            return NextResponse.json({
                success: true,
                uid: userRecord.uid,
                username,
            });
        } catch (authError: any) {
            return NextResponse.json(
                { error: authError.message || "Auth error" },
                { status: 400 },
            );
        }
    } catch (error) {
        console.error("Error creating staff:", error);
        return NextResponse.json(
            { error: "Internal server error" },
            { status: 500 },
        );
    }
}
