const { getApps, initializeApp, cert } = require('firebase-admin/app');
const { getAuth } = require('firebase-admin/auth');
const fs = require('fs');
const crypto = require('crypto');

// Ensure you have service-account.json in the same directory!
if (!fs.existsSync('./service-account.json')) {
  console.error("ERROR: service-account.json not found! Please place your Firebase Admin SDK service account file in this directory.");
  process.exit(1);
}

if (getApps().length === 0) {
  const serviceAccount = JSON.parse(fs.readFileSync('./service-account.json', 'utf8'));
  initializeApp({
    credential: cert(serviceAccount)
  });
}

const auth = getAuth();

async function generateSeed() {
  let sql = 'BEGIN;\n\n';
  
  console.log("Fetching Firebase Auth users...");
  let users = [];
  try {
    const listUsersResult = await auth.listUsers(1000);
    users = listUsersResult.users;
  } catch (err) {
    console.error("Failed to fetch users from Firebase Auth:", err.message);
    process.exit(1);
  }
  
  if (users.length === 0) {
    console.log("No users found in Firebase Auth.");
    process.exit(0);
  }
  
  console.log(`Found ${users.length} users. Generating PostgreSQL seed statements...`);

  // First, ensure the User table exists in case the schema deploy hasn't run yet.
  sql += `CREATE TABLE IF NOT EXISTS "public"."user" (
  "id" text NOT NULL,
  "created_at" integer NOT NULL,
  "phone_number" text NULL,
  "role" text NOT NULL DEFAULT 'owner',
  PRIMARY KEY ("id")
);\n\n`;

  for (const user of users) {
    const uid = user.uid;
    const phone = user.phoneNumber || '';
    const createdAt = Math.floor(new Date(user.metadata.creationTime).getTime() / 1000) || Math.floor(Date.now() / 1000);
    
    // 1. Insert into User table
    sql += `-- Seed User: ${uid} (Phone: ${phone})\n`;
    sql += `INSERT INTO "public"."user" (id, phone_number, created_at, role) VALUES ('${uid}', '${phone}', ${createdAt}, 'owner') ON CONFLICT (id) DO NOTHING;\n`;
    
    // 2. Seed dummy items for this specific user's store
    const item1 = crypto.randomUUID();
    const item2 = crypto.randomUUID();
    sql += `INSERT INTO "public"."item" (id, store_id, name, quantity, unit, buy_price, sell_price, low_stock_threshold, category, is_deleted, updated_at) VALUES ('${item1}', '${uid}', 'Seeded Milk 1L', 50, 'litre', 45, 55, 10, 'Dairy', false, ${createdAt}) ON CONFLICT DO NOTHING;\n`;
    sql += `INSERT INTO "public"."item" (id, store_id, name, quantity, unit, buy_price, sell_price, low_stock_threshold, category, is_deleted, updated_at) VALUES ('${item2}', '${uid}', 'Seeded Bread', 20, 'pcs', 30, 40, 5, 'Grocery', false, ${createdAt}) ON CONFLICT DO NOTHING;\n\n`;
  }
  
  sql += 'COMMIT;\n';
  
  fs.writeFileSync('pg_seed.sql', sql);
  console.log(`\nSuccess! Generated 'pg_seed.sql' containing ${users.length} users and dummy items.`);
  console.log(`\nTo execute this in your Firebase Data Connect PostgreSQL instance, run:`);
  console.log(`  firebase dataconnect:sql:shell --service dataconnect < pg_seed.sql`);
  process.exit(0);
}

generateSeed().catch(console.error);
