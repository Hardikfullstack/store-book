const { initializeApp, cert } = require('firebase-admin/app');
const { getFirestore } = require('firebase-admin/firestore');
const path = require('path');
const fs = require('fs');
const crypto = require('crypto');

const serviceAccountPath = path.resolve(__dirname, 'service-account.json');
let serviceAccount;
try {
  serviceAccount = JSON.parse(fs.readFileSync(serviceAccountPath, 'utf8'));
} catch (e) {
  console.log("No service-account.json found. Skipping Firestore seeding, but will generate SQL.");
}

if (serviceAccount) {
  initializeApp({
    credential: cert(serviceAccount)
  });
}

const db = serviceAccount ? getFirestore() : null;

async function seedData() {
  console.log("Seeding Database and generating dummy_data.sql...");

  const timestamp = Date.now();

  const users = [
    { phone: '+919999999999', role: 'admin', storeId: null, stores: [] },
    { 
      phone: '+918888888888', 
      role: 'owner', 
      stores: ['store_1', 'store_2'], 
      subscription: { status: 'active', plan: 'pro', expiresAt: timestamp + 31536000000, platform: 'web' }
    },
    { 
      phone: '+917777777777', 
      role: 'staff', 
      storeId: 'store_1', 
      ownerId: 'owner_1', 
      username: 'staff1', 
      stores: [] 
    },
    { 
      phone: '+916666666666', 
      role: 'owner', 
      stores: ['store_3'], 
      subscription: null
    }
  ];

  const stores = [
    { id: 'store_1', name: "Premium Kirana", location: "Mumbai", owner_phone: '+918888888888', is_premium: true },
    { id: 'store_2', name: "Premium Electronics", location: "Delhi", owner_phone: '+918888888888', is_premium: true },
    { id: 'store_3', name: "Free Mart", location: "Bangalore", owner_phone: '+916666666666', is_premium: false }
  ];

  const items = [];
  const sales = [];
  const udhaar = [];
  const expenses = [];

  const categories = ["Groceries", "Snacks", "Dairy", "Beverages", "Personal Care", "Cleaning", "Stationery", "Hardware", "Electronics", "Misc"];
  const brands = ["Aashirvaad", "Tata", "Maggi", "Amul", "Britannia", "Parle", "Haldiram", "Lays", "Kurkure", "Nestle", "Cadbury", "Dabur", "Patanjali", "Himalaya", "Surf Excel", "Tide", "Ariel", "Vim", "Dettol", "Lifebuoy", "Lux", "Pears", "Dove", "Pantene", "Head & Shoulders", "Sunsilk", "Clinic Plus", "Gillette", "Whisper", "Stayfree", "Colgate", "Pepsodent", "Close Up", "Oral B", "Everest", "MDH", "Catch", "Mother Dairy", "Saffola", "Fortune", "Gemini", "Sunflower", "Gagan", "P&G", "Unilever", "ITC"];
  const productTypes = ["Atta", "Salt", "Noodles", "Butter", "Biscuits", "Chips", "Namkeen", "Chocolate", "Honey", "Chyawanprash", "Face Wash", "Shampoo", "Soap", "Detergent", "Dishwash", "Handwash", "Toothpaste", "Toothbrush", "Spices", "Milk", "Curd", "Paneer", "Ghee", "Oil", "Rice", "Dal", "Sugar", "Tea", "Coffee", "Juice", "Cold Drink", "Energy Drink", "Mineral Water", "Deodorant", "Talcum Powder", "Hair Oil", "Hair Color", "Shaving Cream", "Razor", "Sanitary Pads", "Diapers", "Wipes", "Toilet Paper", "Garbage Bags", "Mosquito Repellent", "Room Freshener", "Shoe Polish", "Batteries", "Bulbs", "Matches", "Candles", "Incense Sticks", "Broom", "Mop", "Bucket", "Mug", "Dustpan", "Scrub Pad", "Sponges", "Clips", "Pins", "Rubber Bands", "Pens", "Pencils", "Erasers", "Sharpeners", "Rulers", "Notebooks", "Glue", "Tape", "Scissors", "Stapler", "Staples", "Paper", "Envelopes", "Folders", "Files", "Calculators", "Batteries", "Bulbs", "Extension Cords", "Plugs", "Sockets", "Switches", "Wires", "Cables", "Chargers", "Earphones", "Headphones", "Power Banks", "Memory Cards", "Pen Drives", "Mouse", "Keyboard", "Screwdrivers", "Pliers", "Wrenches", "Hammers", "Nails", "Screws", "Nuts", "Bolts", "Washers", "Hinges", "Locks", "Keys", "Handles", "Knobs", "Hooks", "Ropes", "Chains", "Wires", "Cables", "Pipes", "Tubes", "Fittings", "Valves", "Taps", "Faucets", "Showers", "Sprinklers", "Hoses", "Nozzles", "Pumps", "Motors", "Generators", "Inverters", "Batteries", "Solar Panels", "Lights", "Lamps", "Lanterns", "Torches", "Flashlights", "Heaters", "Coolers", "Fans", "ACs", "Refrigerators", "Washing Machines", "Microwaves", "Ovens", "Stoves", "Mixers", "Grinders", "Blenders", "Juicers", "Toasters", "Sandwich Makers", "Irons", "Vacuum Cleaners", "Water Purifiers", "Geysers"];
  const units = ["pcs", "kg", "grams", "litre", "ml", "bags", "packet", "box", "bottle", "can", "jar"];
  const names = ["Rahul", "Priya", "Ramesh", "Suresh", "Amit", "Neha", "Vikas", "Pooja", "Raj", "Anita", "Sanjay", "Kavita", "Sunil", "Sunita", "Anil", "Anju", "Ashok", "Asha", "Vijay", "Vinita", "Rajesh", "Rajni", "Sandeep", "Sangita", "Deepak", "Deepa", "Praveen", "Preeti", "Manoj", "Meena", "Ravi", "Rekha", "Arun", "Aruna", "Sanjay", "Sanjana", "Vikram", "Vidya", "Karan", "Kiran", "Nitin", "Nitika", "Tarun", "Taruna", "Varun", "Varsha", "Yash", "Yamini", "Zakir", "Zoya"];
  const surnames = ["Sharma", "Verma", "Gupta", "Singh", "Kumar", "Patel", "Shah", "Reddy", "Rao", "Nair", "Menon", "Pillai", "Das", "Bose", "Ghosh", "Mitra", "Chatterjee", "Banerjee", "Mukherjee", "Roy", "Sen", "Sinha", "Mishra", "Pandey", "Tiwari", "Dubey", "Shukla", "Agnihotri", "Dixit", "Joshi", "Bhatt", "Vyas", "Kulkarni", "Deshmukh", "Patil", "Jadhav", "Gaikwad", "Pawar", "Shinde", "Kadam", "Chavan", "More", "Bhosle", "Mane", "Wagh", "Khan", "Syed", "Shaikh", "Ansari", "Qureshi", "Malik", "Chaudhary", "Chauhan", "Rajput", "Thakur", "Yadav", "Ahir", "Gurjar", "Jat", "Saini", "Mali", "Sonar", "Lohar", "Sutar", "Kumbhar", "Nhavi", "Parit", "Dobi", "Chamar", "Mochi", "Bhangi", "Mehtar", "Valmiki"];
  const expensesTypes = ["OVERHEAD", "RESTOCK", "SALARY", "MAINTENANCE", "TRANSPORT", "UTILITIES", "MISC"];

  // Generate 1000 items
  for (let i = 0; i < 1000; i++) {
    const brand = brands[Math.floor(Math.random() * brands.length)];
    const productType = productTypes[Math.floor(Math.random() * productTypes.length)];
    const unit = units[Math.floor(Math.random() * units.length)];
    const buyPrice = Math.floor(Math.random() * 500) + 10;
    const sellPrice = buyPrice + Math.floor(Math.random() * (buyPrice * 0.5));
    const quantity = Math.floor(Math.random() * 200);
    items.push({
      cloud_id: crypto.randomUUID(),
      name: `${brand} ${productType} ${i}`,
      category: categories[Math.floor(Math.random() * categories.length)],
      buy_price: buyPrice,
      sell_price: sellPrice,
      quantity: quantity,
      unit: unit,
      low_stock_threshold: Math.floor(Math.random() * 20),
      hsn_code: `100${Math.floor(Math.random() * 9)}`,
      tax_rate: [0, 5, 12, 18, 28][Math.floor(Math.random() * 5)],
      is_deleted: Math.random() < 0.05 ? 1 : 0, // 5% chance of being deleted
      deleted_timestamp: Math.random() < 0.05 ? timestamp - Math.floor(Math.random() * 86400000) : 0
    });
  }

  // Generate 2000 sales (distributed over last 180 days)
  for (let i = 0; i < 2000; i++) {
    const saleItemsCount = Math.floor(Math.random() * 5) + 1;
    const saleItems = [];
    let totalAmount = 0;
    for (let j = 0; j < saleItemsCount; j++) {
      const item = items[Math.floor(Math.random() * items.length)];
      const qty = Math.floor(Math.random() * 5) + 1;
      const subtotal = item.sell_price * qty;
      totalAmount += subtotal;
      saleItems.push({
        cloud_id: crypto.randomUUID(),
        item_name: item.name,
        quantity: qty,
        sell_price: item.sell_price,
        buy_price: item.buy_price,
        unit: item.unit,
        is_deleted: 0
      });
    }

    const isDeleted = Math.random() < 0.02 ? 1 : 0; // 2% chance of being deleted
    const saleTimestamp = timestamp - Math.floor(Math.random() * 15552000000); // Past 180 days
    const customerName = `${names[Math.floor(Math.random() * names.length)]} ${surnames[Math.floor(Math.random() * surnames.length)]}`;
    
    sales.push({
      cloud_id: crypto.randomUUID(),
      customer_name: customerName,
      total_amount: totalAmount,
      discount_amount: Math.random() > 0.8 ? Math.floor(Math.random() * (totalAmount * 0.1)) : 0,
      notes: Math.random() > 0.5 ? "Paid via UPI" : "Cash payment",
      timestamp: saleTimestamp,
      is_deleted: isDeleted,
      deleted_timestamp: isDeleted ? saleTimestamp + 3600000 : 0,
      items: saleItems
    });
  }

  // Generate 500 Udhaar records
  for (let i = 0; i < 500; i++) {
    const customerName = `${names[Math.floor(Math.random() * names.length)]} ${surnames[Math.floor(Math.random() * surnames.length)]}`;
    const type = Math.random() > 0.4 ? "CREDIT" : "PAYMENT"; // 60% credit, 40% payment
    const udhaarTimestamp = timestamp - Math.floor(Math.random() * 15552000000);
    udhaar.push({
      cloud_id: crypto.randomUUID(),
      customer_name: customerName,
      amount: Math.floor(Math.random() * 5000) + 50,
      type: type,
      timestamp: udhaarTimestamp,
      notes: type === "CREDIT" ? `Sale bill #${Math.floor(Math.random() * 1000)}` : "Settle balance payment",
      is_deleted: Math.random() < 0.05 ? 1 : 0
    });
  }

  // Generate 300 Expenses records
  for (let i = 0; i < 300; i++) {
    const type = expensesTypes[Math.floor(Math.random() * expensesTypes.length)];
    let supplierName = "";
    let description = "";
    if (type === "RESTOCK") {
       supplierName = "Bulk Supplier Ltd";
       const sampleItem = items[Math.floor(Math.random() * items.length)];
       description = `Restocked ${sampleItem.name}`;
    } else {
       description = `${type.toLowerCase()} expense for month`;
    }
    const expTimestamp = timestamp - Math.floor(Math.random() * 15552000000);
    expenses.push({
      cloud_id: crypto.randomUUID(),
      type: type,
      description: description,
      amount: Math.floor(Math.random() * 10000) + 500,
      timestamp: expTimestamp,
      supplier_name: supplierName,
      supplier_phone: supplierName ? "9876543210" : "",
      is_deleted: Math.random() < 0.03 ? 1 : 0
    });
  }

  if (db) {
    console.log("Writing to Firestore...");
    // Insert Users
    for (const user of users) {
      await db.collection('users').doc(user.phone).set({ ...user, created_at: timestamp });
      console.log(`Added user: ${user.phone}`);
    }

    // Insert Stores
    for (const store of stores) {
      await db.collection('stores').doc(store.id).set({ ...store, created_at: timestamp });
      console.log(`Added store: ${store.name}`);

      // Seed items, sales, udhaar, expenses ONLY to store_1 for simplicity
      if (store.id === 'store_1') {
        const storeRef = db.collection('stores').doc(store.id);

        async function writeInBatches(collectionRef, dataArray) {
          const batchSize = 400; // Firestore limit is 500
          for (let i = 0; i < dataArray.length; i += batchSize) {
            const batch = db.batch();
            const chunk = dataArray.slice(i, i + batchSize);
            for (const docData of chunk) {
              const docRef = collectionRef.doc(docData.cloud_id);
              batch.set(docRef, { ...docData, updated_at: timestamp });
            }
            await batch.commit();
            console.log(`Wrote batch of ${chunk.length} to ${collectionRef.path}... (${i + chunk.length}/${dataArray.length})`);
          }
        }

        console.log("Writing 1000 items...");
        await writeInBatches(storeRef.collection('items'), items);

        console.log("Writing 2000 sales and their sale_items...");
        const salesBatchSize = 100; // Sales have subcollections so we keep it smaller
        for (let i = 0; i < sales.length; i += salesBatchSize) {
          const batch = db.batch();
          const chunk = sales.slice(i, i + salesBatchSize);
          let opCount = 0;
          for (const sale of chunk) {
            const { items: saleItems, ...saleData } = sale;
            const saleRef = storeRef.collection('sales').doc(sale.cloud_id);
            batch.set(saleRef, { ...saleData, updated_at: timestamp });
            opCount++;
            for (const sItem of saleItems) {
              const sItemRef = saleRef.collection('items').doc(sItem.cloud_id);
              batch.set(sItemRef, { ...sItem, updated_at: timestamp });
              opCount++;
            }
          }
          await batch.commit();
          console.log(`Wrote batch of sales (${i + chunk.length}/${sales.length}) with ${opCount} writes...`);
        }

        console.log("Writing 500 udhaar...");
        await writeInBatches(storeRef.collection('udhaar'), udhaar);

        console.log("Writing 300 expenses...");
        await writeInBatches(storeRef.collection('expenses'), expenses);
      }
    }
    console.log("Firestore seeding complete.");
  }

  // GENERATE dummy_data.sql for Android App
  console.log("Generating dummy_data.sql...");
  let sql = `PRAGMA user_version = 6;
DROP TABLE IF EXISTS items;
DROP TABLE IF EXISTS sales;
DROP TABLE IF EXISTS sale_items;
DROP TABLE IF EXISTS udhaar;
DROP TABLE IF EXISTS expenses;
DROP TABLE IF EXISTS android_metadata;
DROP INDEX IF EXISTS idx_items_deleted;
DROP INDEX IF EXISTS idx_items_category;
DROP INDEX IF EXISTS idx_sale_items_sale_id;
DROP INDEX IF EXISTS idx_udhaar_customer;
DROP INDEX IF EXISTS idx_sales_timestamp;
DROP INDEX IF EXISTS idx_expenses_timestamp;

CREATE TABLE items (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL UNIQUE,
    quantity REAL NOT NULL DEFAULT 0.0,
    unit TEXT NOT NULL,
    buy_price REAL NOT NULL DEFAULT 0.0,
    sell_price REAL NOT NULL DEFAULT 0.0,
    low_stock_threshold REAL NOT NULL DEFAULT 0.0,
    category TEXT NOT NULL,
    photo_path TEXT,
    hsn_code TEXT,
    tax_rate REAL NOT NULL DEFAULT 0.0,
    is_deleted INTEGER NOT NULL DEFAULT 0,
    deleted_timestamp INTEGER DEFAULT 0,
    cloud_id TEXT,
    is_synced INTEGER NOT NULL DEFAULT 0,
    updated_at INTEGER NOT NULL DEFAULT 0
);

CREATE TABLE sales (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    timestamp INTEGER NOT NULL,
    total_amount REAL NOT NULL,
    discount_amount REAL NOT NULL DEFAULT 0.0,
    customer_name TEXT,
    customer_gstin TEXT,
    business_gstin TEXT,
    customer_address TEXT,
    business_address TEXT,
    notes TEXT,
    is_deleted INTEGER NOT NULL DEFAULT 0,
    cloud_id TEXT,
    is_synced INTEGER NOT NULL DEFAULT 0,
    updated_at INTEGER NOT NULL DEFAULT 0
);

CREATE TABLE sale_items (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    sale_id INTEGER NOT NULL,
    item_id INTEGER NOT NULL,
    item_name TEXT NOT NULL,
    unit TEXT NOT NULL,
    quantity REAL NOT NULL,
    sell_price REAL NOT NULL,
    buy_price REAL NOT NULL,
    is_deleted INTEGER NOT NULL DEFAULT 0,
    cloud_id TEXT,
    is_synced INTEGER NOT NULL DEFAULT 0,
    updated_at INTEGER NOT NULL DEFAULT 0
);

CREATE TABLE udhaar (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    customer_name TEXT NOT NULL,
    amount REAL NOT NULL,
    type TEXT NOT NULL,
    timestamp INTEGER NOT NULL,
    notes TEXT,
    is_deleted INTEGER NOT NULL DEFAULT 0,
    cloud_id TEXT,
    is_synced INTEGER NOT NULL DEFAULT 0,
    updated_at INTEGER NOT NULL DEFAULT 0
);

CREATE TABLE expenses (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    type TEXT NOT NULL,
    description TEXT NOT NULL,
    amount REAL NOT NULL,
    timestamp INTEGER NOT NULL,
    supplier_name TEXT,
    supplier_phone TEXT,
    is_deleted INTEGER NOT NULL DEFAULT 0,
    cloud_id TEXT,
    is_synced INTEGER NOT NULL DEFAULT 0,
    updated_at INTEGER NOT NULL DEFAULT 0
);

CREATE TABLE android_metadata (
    locale TEXT
);
INSERT INTO android_metadata (locale) VALUES ('en_US');

CREATE INDEX idx_items_deleted ON items(is_deleted);
CREATE INDEX idx_items_category ON items(category);
CREATE INDEX idx_sale_items_sale_id ON sale_items(sale_id);
CREATE INDEX idx_udhaar_customer ON udhaar(customer_name);
CREATE INDEX idx_sales_timestamp ON sales(timestamp);
CREATE INDEX idx_expenses_timestamp ON expenses(timestamp);
CREATE UNIQUE INDEX idx_items_cloud_id ON items(cloud_id) WHERE cloud_id IS NOT NULL;
CREATE INDEX idx_items_is_synced ON items(is_synced);
CREATE INDEX idx_items_updated_at ON items(updated_at);
CREATE UNIQUE INDEX idx_sales_cloud_id ON sales(cloud_id) WHERE cloud_id IS NOT NULL;
CREATE INDEX idx_sales_is_synced ON sales(is_synced);
CREATE INDEX idx_sales_updated_at ON sales(updated_at);
CREATE UNIQUE INDEX idx_sale_items_cloud_id ON sale_items(cloud_id) WHERE cloud_id IS NOT NULL;
CREATE INDEX idx_sale_items_is_synced ON sale_items(is_synced);
CREATE INDEX idx_sale_items_updated_at ON sale_items(updated_at);
CREATE UNIQUE INDEX idx_udhaar_cloud_id ON udhaar(cloud_id) WHERE cloud_id IS NOT NULL;
CREATE INDEX idx_udhaar_is_synced ON udhaar(is_synced);
CREATE INDEX idx_udhaar_updated_at ON udhaar(updated_at);
CREATE UNIQUE INDEX idx_expenses_cloud_id ON expenses(cloud_id) WHERE cloud_id IS NOT NULL;
CREATE INDEX idx_expenses_is_synced ON expenses(is_synced);
CREATE INDEX idx_expenses_updated_at ON expenses(updated_at);

`;

  // Insert items
  let itemCounter = 1;
  const itemMap = {}; // cloud_id -> local id
  for (const item of items) {
    itemMap[item.cloud_id] = itemCounter;
    sql += `INSERT INTO items (id, name, quantity, unit, buy_price, sell_price, low_stock_threshold, category, hsn_code, tax_rate, is_deleted, deleted_timestamp, cloud_id, is_synced, updated_at) VALUES (${itemCounter}, '${item.name}', ${item.quantity}, '${item.unit}', ${item.buy_price}, ${item.sell_price}, ${item.low_stock_threshold}, '${item.category}', '${item.hsn_code || ''}', ${item.tax_rate}, ${item.is_deleted}, ${item.deleted_timestamp || 0}, '${item.cloud_id}', 1, ${timestamp});\n`;
    itemCounter++;
  }

  // Insert Sales
  let saleCounter = 1;
  let saleItemCounter = 1;
  for (const sale of sales) {
    sql += `INSERT INTO sales (id, timestamp, total_amount, discount_amount, customer_name, notes, is_deleted, cloud_id, is_synced, updated_at) VALUES (${saleCounter}, ${sale.timestamp}, ${sale.total_amount}, ${sale.discount_amount}, '${sale.customer_name}', '${sale.notes}', ${sale.is_deleted}, '${sale.cloud_id}', 1, ${timestamp});\n`;
    for (const sItem of sale.items) {
      const parentItem = items.find(i => i.name === sItem.item_name);
      const itemId = parentItem ? itemMap[parentItem.cloud_id] : 0;
      sql += `INSERT INTO sale_items (id, sale_id, item_id, item_name, unit, quantity, sell_price, buy_price, is_deleted, cloud_id, is_synced, updated_at) VALUES (${saleItemCounter}, ${saleCounter}, ${itemId}, '${sItem.item_name}', '${sItem.unit}', ${sItem.quantity}, ${sItem.sell_price}, ${sItem.buy_price}, ${sItem.is_deleted}, '${sItem.cloud_id}', 1, ${timestamp});\n`;
      saleItemCounter++;
    }
    saleCounter++;
  }

  // Insert Udhaar
  let udhaarCounter = 1;
  for (const u of udhaar) {
    sql += `INSERT INTO udhaar (id, customer_name, amount, type, timestamp, notes, is_deleted, cloud_id, is_synced, updated_at) VALUES (${udhaarCounter}, '${u.customer_name}', ${u.amount}, '${u.type}', ${u.timestamp}, '${u.notes}', ${u.is_deleted}, '${u.cloud_id}', 1, ${timestamp});\n`;
    udhaarCounter++;
  }

  // Insert Expenses
  let expCounter = 1;
  for (const e of expenses) {
    sql += `INSERT INTO expenses (id, type, description, amount, timestamp, supplier_name, supplier_phone, is_deleted, cloud_id, is_synced, updated_at) VALUES (${expCounter}, '${e.type}', '${e.description}', ${e.amount}, ${e.timestamp}, '${e.supplier_name}', '${e.supplier_phone}', ${e.is_deleted}, '${e.cloud_id}', 1, ${timestamp});\n`;
    expCounter++;
  }

  // No commit needed here

  const sqlPath = path.resolve(__dirname, '../app/src/main/assets/dummy_data.sql');
  fs.writeFileSync(sqlPath, sql, 'utf8');
  console.log(`Successfully generated ${sqlPath}`);
  console.log("Seeding process completely finished.");
}

seedData().catch(console.error).finally(() => process.exit(0));
