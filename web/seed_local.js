const fs = require('fs');
const path = require('path');
const { Client } = require('C:/Users/limba/npm/node_modules/firebase-tools/node_modules/pg');

async function seedLocal() {
  console.log('Connecting to local PostgreSQL database: new-storebook-database...');
  const client = new Client({
    connectionString: 'postgresql://postgres:postgres@127.0.0.1:5432/new-storebook-database?sslmode=disable'
  });

  await client.connect();
  console.log('Connected to local PostgreSQL successfully.');

  const seedFilePath = path.join(__dirname, 'seed.sql');
  console.log(`Reading SQL seed file: ${seedFilePath}...`);
  const sql = fs.readFileSync(seedFilePath, 'utf8');

  console.log('Executing seed.sql transaction...');
  const startTime = Date.now();
  await client.query(sql);
  const duration = ((Date.now() - startTime) / 1000).toFixed(2);
  console.log(`Seeding completed in ${duration}s!`);

  // Verification counts
  const storeCount = await client.query('SELECT count(*) FROM "public"."store"');
  const userCount = await client.query('SELECT count(*) FROM "public"."user"');
  const catCount = await client.query('SELECT count(*) FROM "public"."category"');
  const itemCount = await client.query('SELECT count(*) FROM "public"."item"');
  const saleCount = await client.query('SELECT count(*) FROM "public"."sale"');
  const users = await client.query('SELECT id, phone_number, store_id FROM "public"."user"');

  console.log('\n--- Local Database Summary ---');
  console.log(`Stores:     ${storeCount.rows[0].count}`);
  console.log(`Users:      ${userCount.rows[0].count}`);
  console.log(`Categories: ${catCount.rows[0].count}`);
  console.log(`Items:      ${itemCount.rows[0].count}`);
  console.log(`Sales:      ${saleCount.rows[0].count}`);
  console.log('\nUsers in Database:');
  console.log(users.rows);

  await client.end();
}

seedLocal().catch(err => {
  console.error('Local seeding failed:', err);
  process.exit(1);
});
