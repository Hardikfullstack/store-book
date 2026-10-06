import { initializeApp, getApps, cert } from 'firebase-admin/app';
import { getAuth } from 'firebase-admin/auth';
import path from 'path';
import fs from 'fs';

if (process.env.NEXT_PUBLIC_USE_FIREBASE_EMULATOR === 'true') {
  process.env.FIREBASE_AUTH_EMULATOR_HOST = '127.0.0.1:9099';
  process.env.FIREBASE_STORAGE_EMULATOR_HOST = '127.0.0.1:9199';
  process.env.DATA_CONNECT_EMULATOR_HOST = 'http://127.0.0.1:9399';
}

if (getApps().length === 0) {
  try {
    const serviceAccountPath = path.resolve(process.cwd(), 'service-account.json');
    const serviceAccount = JSON.parse(fs.readFileSync(serviceAccountPath, 'utf8'));

    initializeApp({
      credential: cert(serviceAccount),
      projectId: process.env.NEXT_PUBLIC_FIREBASE_PROJECT_ID,
    });
  } catch (error) {
    console.error("Firebase Admin Initialization Error:", error);
  }
}

export const adminAuth = getAuth();
