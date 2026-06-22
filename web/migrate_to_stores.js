const { getApps, initializeApp, cert } = require('firebase-admin/app');
const { getFirestore } = require('firebase-admin/firestore');
const crypto = require('crypto');
const fs = require('fs');

if (getApps().length === 0) {
  const serviceAccount = JSON.parse(fs.readFileSync('./service-account.json', 'utf8'));
  initializeApp({
    credential: cert(serviceAccount)
  });
}

const db = getFirestore();

async function migrateData() {
  console.log("Starting Migration from users/{uid} to stores/{storeId}...");

  try {
    const usersSnapshot = await db.collection('users').get();
    let migratedCount = 0;

    for (const userDoc of usersSnapshot.docs) {
      const userData = userDoc.data();
      const uid = userDoc.id;

      // Check if user has legacy subcollections (items, sales, udhaar, expenses)
      const legacyCollections = ['items', 'sales', 'udhaar', 'expenses'];
      let hasLegacyData = false;

      // We need to check if any of these subcollections exist and have data
      for (const collName of legacyCollections) {
        const snapshot = await db.collection('users').doc(uid).collection(collName).limit(1).get();
        if (!snapshot.empty) {
          hasLegacyData = true;
          break;
        }
      }

      if (hasLegacyData) {
        console.log(`\nFound legacy data for user: ${uid}. Checking for storeId...`);
        let storeId = userData.storeId;

        if (!storeId) {
          storeId = crypto.randomUUID();
          console.log(`Provisioning new store: ${storeId}`);
          
          // Create new store
          await db.collection('stores').doc(storeId).set({
            name: `${userData.phone || uid}'s Store`,
            is_active: true,
            created_at: Date.now()
          });

          // Link user to store
          await db.collection('users').doc(uid).set({
            storeId: storeId,
            role: 'admin'
          }, { merge: true });
        } else {
          console.log(`User already linked to store: ${storeId}`);
        }

        // Migrate all collections
        for (const collName of legacyCollections) {
          const oldSnapshot = await db.collection('users').doc(uid).collection(collName).get();
          if (oldSnapshot.empty) continue;

          console.log(`Migrating ${oldSnapshot.size} documents in '${collName}' for user ${uid}`);
          
          const batch = db.batch();
          oldSnapshot.docs.forEach(doc => {
            const newRef = db.collection('stores').doc(storeId).collection(collName).doc(doc.id);
            // Include is_deleted if not present, and map the document ID to cloud_id to ensure UUIDs are matched
            batch.set(newRef, {
              ...doc.data(),
              cloud_id: doc.data().cloud_id || doc.id,
              is_deleted: doc.data().is_deleted || 0
            }, { merge: true });
            
            // Optional: Delete the old document to clean up space
            // batch.delete(doc.ref);
          });

          await batch.commit();
          console.log(`Successfully migrated ${collName} for user ${uid}`);
        }
        migratedCount++;
      }
    }

    console.log(`\nMigration completed successfully! Processed ${migratedCount} legacy users.`);
    process.exit(0);
  } catch (error) {
    console.error("Migration failed:", error);
    process.exit(1);
  }
}

migrateData();
