import type { Timestamp } from 'firebase-admin/firestore';

type SerializedValue = string | number | boolean | null | undefined | SerializedValue[] | Record<string, unknown>;

function serializeValue(value: unknown): SerializedValue {
  if (value === null || value === undefined) return value;
  // Firestore Timestamp (both admin and client SDK shapes)
  if (typeof value === 'object' && typeof (value as Timestamp).toMillis === 'function') {
    return (value as Timestamp).toMillis();
  }
  if (typeof value === 'object' && '_seconds' in value && '_nanoseconds' in value) {
    const ts = value as { _seconds: number; _nanoseconds: number };
    return ts._seconds * 1000 + Math.floor(ts._nanoseconds / 1e6);
  }
  if (Array.isArray(value)) return value.map(serializeValue);
  if (typeof value === 'object') {
    return Object.fromEntries(Object.entries(value).map(([k, v]) => [k, serializeValue(v)]));
  }
  return value as SerializedValue;
}

type SerializedRecord = { [key: string]: SerializedValue };

export function serializeDoc(data: Record<string, unknown>): SerializedRecord {
  return serializeValue(data) as SerializedRecord;
}
