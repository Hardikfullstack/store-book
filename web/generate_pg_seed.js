const { getApps, initializeApp, cert } = require('firebase-admin/app');
const { getAuth } = require('firebase-admin/auth');
const fs = require('fs');

if (getApps().length === 0) {
  const serviceAccount = JSON.parse(fs.readFileSync('../service-account.json', 'utf8'));
  initializeApp({
    credential: cert(serviceAccount)
  });
}

const auth = getAuth();

async function generateSeed() {
  let sql = 'BEGIN;\n';
  
  console.log("Fetching Firebase users...");
  const listUsersResult = await auth.listUsers(1000);
  const users = listUsersResult.users;
  
  if (users.length === 0) {
    console.log("No users found.");
    process.exit(0);
  }

  for (const user of users) {
    const uid = user.uid;
    const phone = user.phoneNumber || '';
    const createdAt = Math.floor(new Date(user.metadata.creationTime).getTime() / 1000) || Math.floor(Date.now() / 1000);
    
    sql += `INSERT INTO "public"."user" (id, phone_number, created_at, role) VALUES ('${uid}', '${phone}', ${createdAt}, 'owner') ON CONFLICT (id) DO NOTHING;\n`;
    
    // Seed some dummy items for this user (storeId = uid)
    const itemId = crypto.randomUUID();
    sql += `INSERT INTO "public"."item" (id, store_id, name, quantity, unit, buy_price, sell_price, low_stock_threshold, category, is_deleted, updated_at) VALUES ('${itemId}', '${uid}', 'Dummy Milk 1L', 50, 'litre', 45, 55, 10, 'Dairy', false, ${createdAt}) ON CONFLICT DO NOTHING;\n`;
    
    const itemId2 = crypto.randomUUID();
    sql += `INSERT INTO "public"."item" (id, store_id, name, quantity, unit, buy_price, sell_price, low_stock_threshold, category, is_deleted, updated_at) VALUES ('${itemId2}', '${uid}', 'Dummy Bread', 20, 'pcs', 30, 40, 5, 'Grocery', false, ${createdAt}) ON CONFLICT DO NOTHING;\n`;
  }
  
  sql += 'COMMIT;\n';
  
  fs.writeFileSync('pg_seed.sql', sql);
  console.log(`Generated pg_seed.sql with ${users.length} users and dummy data.`);
  process.exit(0);
}

generateSeed().catch(console.error);
