import { getSession } from "@/lib/session";
import {
    resolvePermissions,
    mergePermissions,
    PermissionSet,
    AllowedRole,
    hasRolePermission,
} from "@/lib/roleMatrix";

export type { AllowedRole, PermissionSet };
export {
    resolvePermissions,
    hasRolePermission,
    mergePermissions,
} from "@/lib/roleMatrix";

/**
 * E04-S2 / E28-S1 — Server-side permission enforcement.
 * Imports single source of truth from roleMatrix.ts.
 */

/**
 * Check if the current session is allowed to access resource `key`.
 * Returns null (allowed) or { redirectUrl, message } if denied.
 * Supports per-user overrides via `overrides` param.
 */
export async function assertAccess(
    key: keyof PermissionSet,
    overrides?: Partial<PermissionSet>,
): Promise<{ redirectUrl?: string; message?: string } | null> {
    const session = await getSession();
    if (!session)
        return { redirectUrl: "/login", message: "Please log in to continue." };

    const perms = mergePermissions(session.role ?? "staff", overrides);
    if (!perms[key]) {
        return {
            redirectUrl: "/",
            message: `Insufficient permissions for this section.`,
        };
    }
    return null;
}

/** For server components — quick boolean test */
export async function hasPermission(
    key: keyof PermissionSet,
): Promise<boolean> {
    const denied = await assertAccess(key);
    return denied === null;
}

/**
 * E28-S1 — Wrapper that returns a structured result for server actions.
 * Returns `{ success: true }` or throws with the error message.
 */
export async function assertPermission(
    key: keyof PermissionSet,
): Promise<void> {
    const denied = await assertAccess(key);
    if (denied) throw new Error(denied.message);
}

/**
 * E28-S2 — Server page guard helper.
 * Returns current session if allowed, or re-throws with redirect info.
 */
export async function requirePermission(
    key: keyof PermissionSet,
): Promise<NonNullable<Awaited<ReturnType<typeof getSession>>>> {
    const session = await getSession();
    if (!session) throw new Error("UNAUTHENTICATED");

    const perms = resolvePermissions(session.role ?? "staff");
    if (!perms[key]) throw Error("INSUFFICIENT_PERMISSIONS");

    return session;
}
