const fs = require('fs');
const path = require('path');

const fbToolsPath = 'C:\\Users\\limba\\npm\\node_modules\\firebase-tools';

async function runSeed() {
  console.log('--- StoreBook Data Connect Database Seeder ---');
  
  const pg = require(path.join(fbToolsPath, 'node_modules', 'pg'));
  const { Connector, IpAddressTypes, AuthTypes } = require(path.join(fbToolsPath, 'node_modules', '@google-cloud', 'cloud-sql-connector'));
  const cloudSqlAdminClient = require(path.join(fbToolsPath, 'lib', 'gcp', 'cloudsql', 'cloudsqladmin'));
  const { FBToolsAuthClient } = require(path.join(fbToolsPath, 'lib', 'gcp', 'cloudsql', 'fbToolsAuthClient'));
  const { getIAMUser } = require(path.join(fbToolsPath, 'lib', 'gcp', 'cloudsql', 'connect'));
  const { selectAccount, setActiveAccount } = require(path.join(fbToolsPath, 'lib', 'auth'));
  const { requireAuth } = require(path.join(fbToolsPath, 'lib', 'requireAuth'));

  const projectId = 'new-storebook';
  const instanceId = 'new-storebook-instance';
  const databaseId = 'new-storebook-database';

  const options = { project: projectId };
  const projectRoot = path.resolve(__dirname, '..');
  const account = selectAccount(undefined, projectRoot);
  console.log(`Authenticating as: ${account?.user?.email || 'default account'}`);
  if (account) {
    setActiveAccount(options, account);
  }
  await requireAuth(options);

  console.log(`Connecting to Cloud SQL instance: ${instanceId} in project: ${projectId}...`);
  const instance = await cloudSqlAdminClient.getInstance(projectId, instanceId);
  const connectionName = instance.connectionName;
  console.log(`Connection Name: ${connectionName}`);

  const { user: username } = await getIAMUser(options);
  console.log(`IAM DB User: ${username}`);

  const connector = new Connector({
    auth: new FBToolsAuthClient(),
  });

  const clientOpts = await connector.getOptions({
    instanceConnectionName: connectionName,
    ipType: IpAddressTypes.PUBLIC,
    authType: AuthTypes.IAM,
  });

  const pool = new pg.Pool({
    ...clientOpts,
    user: username,
    database: databaseId,
  });

  const conn = await pool.connect();
  console.log('Successfully connected to PostgreSQL database!');

  const seedFilePath = path.join(__dirname, 'seed.sql');
  console.log(`Reading SQL seed file from: ${seedFilePath}...`);
  const sqlContent = fs.readFileSync(seedFilePath, 'utf8');

  console.log('Executing seed SQL transaction (1,800+ records)...');
  const startTime = Date.now();
  await conn.query(sqlContent);
  const duration = ((Date.now() - startTime) / 1000).toFixed(2);
  console.log(`Seed SQL executed successfully in ${duration}s!`);

  // Verify counts
  const storeCount = await conn.query('SELECT count(*) FROM "public"."store"');
  const catCount = await conn.query('SELECT count(*) FROM "public"."category"');
  const itemCount = await conn.query('SELECT count(*) FROM "public"."item"');
  const saleCount = await conn.query('SELECT count(*) FROM "public"."sale"');

  console.log('--- Verification Counts in Database ---');
  console.log(`Stores:     ${storeCount.rows[0].count}`);
  console.log(`Categories: ${catCount.rows[0].count}`);
  console.log(`Items:      ${itemCount.rows[0].count}`);
  console.log(`Sales:      ${saleCount.rows[0].count}`);

  conn.release();
  await pool.end();
  connector.close();
  console.log('Database connection closed. Seeding complete!');
}

runSeed().catch(err => {
  console.error('Seeding failed with error:', err);
  process.exit(1);
});
