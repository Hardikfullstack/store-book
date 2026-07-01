import type { Timestamp } from 'firebase-admin/firestore';

function serializeValue(value: any): any {
  if (value === null || value === undefined) return value;
  // Firestore Timestamp (both admin and client SDK shapes)
  if (typeof value === 'object' && typeof value.toMillis === 'function') {
    return (value as Timestamp).toMillis();
  }
  if (typeof value === 'object' && '_seconds' in value && '_nanoseconds' in value) {
    return value._seconds * 1000 + Math.floor(value._nanoseconds / 1e6);
  }
  if (Array.isArray(value)) return value.map(serializeValue);
  if (typeof value === 'object') {
    return Object.fromEntries(Object.entries(value).map(([k, v]) => [k, serializeValue(v)]));
  }
  return value;
}

export function serializeDoc(data: Record<string, any>): Record<string, any> {
  return serializeValue(data);
}
