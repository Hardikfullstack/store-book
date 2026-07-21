const { initializeApp, cert } = require('firebase-admin/app');
const { getAuth } = require('firebase-admin/auth');
const fs = require('fs');
const path = require('path');
const { v4: uuidv4 } = require('uuid');

const serviceAccountPath = path.resolve(__dirname, 'service-account.json');
const serviceAccount = JSON.parse(fs.readFileSync(serviceAccountPath, 'utf8'));

initializeApp({
  credential: cert(serviceAccount),
});

const storeTypes = [
  { type: 'Kirana Store', categories: ['Groceries', 'Snacks', 'Beverages', 'Spices'] },
  { type: 'Medical Shop', categories: ['Medicines', 'Supplements', 'First Aid', 'Personal Care'] },
  { type: 'Electronics Shop', categories: ['Mobiles', 'Accessories', 'Appliances', 'Laptops'] },
  { type: 'Hardware Store', categories: ['Tools', 'Paints', 'Plumbing', 'Electrical'] },
  { type: 'Clothing Boutique', categories: ['Men', 'Women', 'Kids', 'Accessories'] },
  { type: 'Stationery Shop', categories: ['Books', 'Pens', 'Crafts', 'Office Supplies'] },
  { type: 'Dairy & Sweets', categories: ['Milk', 'Sweets', 'Bakery', 'Ice Cream'] },
  { type: 'Footwear Store', categories: ['Sneakers', 'Formals', 'Sandals', 'Socks'] },
  { type: 'Sports Shop', categories: ['Equipments', 'Apparel', 'Nutrition', 'Accessories'] },
  { type: 'Auto Parts', categories: ['Tyres', 'Oils', 'Spares', 'Accessories'] }
];

const unitOptions = ['pcs', 'kg', 'box', 'packet', 'litre', 'bottle', 'grams'];

function randomInt(min, max) {
  return Math.floor(Math.random() * (max - min + 1)) + min;
}

function randomFloat(min, max) {
  return parseFloat((Math.random() * (max - min) + min).toFixed(2));
}

function randomChoice(arr) {
  return arr[Math.floor(Math.random() * arr.length)];
}

async function generateSeed() {
  const auth = getAuth();
  let usersList = [];
  try {
    const listUsersResult = await auth.listUsers(10);
    usersList = listUsersResult.users;
  } catch (err) {
    console.error('Error fetching users:', err);
    return;
  }

  if (usersList.length === 0) {
    console.log("No users found in Firebase Auth! Create some users first.");
    return;
  }

  let sql = 'BEGIN;\n\n';

  // Generate 10 stores
  const stores = [];
  const allItems = [];

  for (let i = 0; i < 10; i++) {
    const storeType = storeTypes[i % storeTypes.length];
    const user = usersList[i % usersList.length]; // cycle if less than 10 users
    
    const storeId = uuidv4();
    stores.push({ id: storeId, ownerId: user.uid, type: storeType.type });

    sql += `INSERT INTO "public"."store" (id, name, is_active, is_premium, subscription_status) VALUES ('${storeId}', 'Dummy ${storeType.type} ${i+1}', true, false, 'inactive') ON CONFLICT (id) DO NOTHING;\n`;
    sql += `INSERT INTO "public"."user" (id, phone_number, created_at, role, store_id) VALUES ('${user.uid}', '${user.phoneNumber || '+91000000000' + i}', ${Date.now()}, 'owner', '${storeId}') ON CONFLICT (id) DO NOTHING;\n`;

    // Generate ~50 items for each store to reach 500+ items total
    const storeItems = [];
    for (let j = 0; j < 55; j++) {
      const itemId = uuidv4();
      const buyPrice = randomFloat(10, 500);
      const sellPrice = parseFloat((buyPrice * randomFloat(1.1, 1.5)).toFixed(2));
      const category = randomChoice(storeType.categories);
      const name = `${category} Item ${j+1}`;
      const quantity = randomInt(5, 100);
      
      sql += `INSERT INTO "public"."item" (id, store_id, name, quantity, unit, buy_price, sell_price, low_stock_threshold, category, is_deleted, updated_at) VALUES ('${itemId}', '${storeId}', '${name}', ${quantity}, '${randomChoice(unitOptions)}', ${buyPrice}, ${sellPrice}, 5, '${category}', false, ${Date.now()}) ON CONFLICT (id) DO NOTHING;\n`;
      
      storeItems.push({ id: itemId, name, sellPrice, buyPrice });
      allItems.push(itemId);
    }

    // Generate Sales
    for (let s = 0; s < 20; s++) {
      const saleId = uuidv4();
      const numItems = randomInt(1, 5);
      let totalAmount = 0;
      
      let saleItemsSql = '';
      for (let k = 0; k < numItems; k++) {
        const sItem = randomChoice(storeItems);
        const qty = randomInt(1, 3);
        const amount = sItem.sellPrice * qty;
        totalAmount += amount;
        const saleItemId = uuidv4();
        
        saleItemsSql += `INSERT INTO "public"."sale_item_detail" (id, store_id, sale_id, item_id, item_name, quantity, unit, sell_price, buy_price, is_deleted, updated_at) VALUES ('${saleItemId}', '${storeId}', '${saleId}', '${sItem.id}', '${sItem.name}', ${qty}, 'pcs', ${sItem.sellPrice}, ${sItem.buyPrice}, false, ${Date.now()}) ON CONFLICT (id) DO NOTHING;\n`;
      }

      sql += `INSERT INTO "public"."sale" (id, store_id, timestamp, total_amount, discount_amount, type, is_deleted, updated_at) VALUES ('${saleId}', '${storeId}', ${Date.now() - randomInt(0, 30*24*60*60*1000)}, ${totalAmount}, 0, 'SALE', false, ${Date.now()}) ON CONFLICT (id) DO NOTHING;\n`;
      sql += saleItemsSql;
    }

    // Generate Udhaar
    for (let u = 0; u < 10; u++) {
      const udhaarId = uuidv4();
      const amount = randomFloat(50, 1000);
      const type = randomChoice(['GIVEN', 'RECEIVED']);
      sql += `INSERT INTO "public"."udhaar_entry" (id, store_id, customer_name, amount, type, timestamp, is_deleted, updated_at) VALUES ('${udhaarId}', '${storeId}', 'Customer ${u}', ${amount}, '${type}', ${Date.now() - randomInt(0, 30*24*60*60*1000)}, false, ${Date.now()}) ON CONFLICT (id) DO NOTHING;\n`;
    }

    // Generate Expenses
    for (let e = 0; e < 15; e++) {
      const expenseId = uuidv4();
      const amount = randomFloat(100, 2000);
      sql += `INSERT INTO "public"."expense_entry" (id, store_id, type, description, amount, timestamp, is_deleted, updated_at) VALUES ('${expenseId}', '${storeId}', 'Store Maintenance', 'Dummy Expense ${e}', ${amount}, ${Date.now() - randomInt(0, 30*24*60*60*1000)}, false, ${Date.now()}) ON CONFLICT (id) DO NOTHING;\n`;
    }
    
    // Generate Suppliers and Purchases
    for (let sup = 0; sup < 3; sup++) {
      const supplierId = uuidv4();
      const supplierName = `Supplier ${sup} for ${storeType.type}`;
      sql += `INSERT INTO "public"."supplier" (id, store_id, name, is_deleted, updated_at) VALUES ('${supplierId}', '${storeId}', '${supplierName}', false, ${Date.now()}) ON CONFLICT (id) DO NOTHING;\n`;
      
      const purchaseId = uuidv4();
      const totalPurchaseAmount = randomFloat(1000, 5000);
      sql += `INSERT INTO "public"."purchase" (id, store_id, supplier_id, supplier_name, total_amount, tax_amount, type, timestamp, is_deleted, updated_at) VALUES ('${purchaseId}', '${storeId}', '${supplierId}', '${supplierName}', ${totalPurchaseAmount}, 0, 'BILL', ${Date.now()}, false, ${Date.now()}) ON CONFLICT (id) DO NOTHING;\n`;
      
      for(let pi = 0; pi < 3; pi++) {
        const pItem = randomChoice(storeItems);
        const purchaseItemId = uuidv4();
        sql += `INSERT INTO "public"."purchase_item_detail" (id, store_id, purchase_id, item_id, item_name, quantity, unit, buy_price, is_deleted, updated_at) VALUES ('${purchaseItemId}', '${storeId}', '${purchaseId}', '${pItem.id}', '${pItem.name}', ${randomInt(10,50)}, 'pcs', ${pItem.buyPrice}, false, ${Date.now()}) ON CONFLICT (id) DO NOTHING;\n`;
      }
    }
  }

  sql += '\nCOMMIT;\n';

  fs.writeFileSync('seed.sql', sql);
  console.log(`Successfully generated seed.sql with 10 stores, ${allItems.length} items, and related data!`);
}

generateSeed();
