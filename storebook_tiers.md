# StoreBook: Comprehensive Feature Catalog & Tiers

An exhaustive catalog of all features, modules, and billing tier differences in the StoreBook application.

---

## 📱 Core Application Modules

StoreBook is a complete POS (Point-of-Sale), inventory management, and customer ledger app. Below is an overview of every feature module implemented in the application:

### 1. Sales & Billing (POS)
* **Direct-To-Cart Stepper:** Search products and adjust quantities instantly using inline `+` and `−` steppers without leaving the search results.
* **Haptic Feedback:** Tactile vibrations confirming successful additions and adjustments.
* **Flexible Checkout Options:** Check out with cash, online payment, or mark it as Udhaar (credit) mapped to a specific customer.
* **Invoice Generation:** Automatic calculation of totals, taxes, discounts, and outstanding balances.

### 2. Inventory & Stock Management
* **Basic Fields:** Track product name, quantity, category, units (e.g., pcs, kg, ltr), and pricing.
* **Low-Stock Alerts:** Threshold tracking indicating products nearing depletion.
* **Advanced Tracking:** Progressive disclosure toggle to manage HSN/SAC codes, Tax Rates (GST %), Batch/Lot numbers, and Expiry dates.
* **CSV Bulk Actions:** Import entire inventories from CSV files, and export them to local device memory.

### 3. Udhaar (Customer Ledger)
* **Digital Khata:** Maintain clean credit ledgers for all customers.
* **Sign-Based Balance Rules:** Accurate tracking showing if money is *Due* (receivable) or *Advance* (payable).
* **Speedy Reminders:** Directly share transaction receipts and outstanding statements.

### 4. Financial Dashboard & Analytics
* **Sparkline Visualizations:** Canvas-drawn 7-day trendlines for Sales, Purchases, and Expenses.
* **Performance Indicators:** Traffic-light warning system indicating operational health.
* **Sales Analytics Screen:** Interactive metrics revealing top-selling items and busy hours.

### 5. Multi-Store & Cloud Sync
* **Multi-Store Management:** Create separate stores to run multiple businesses or branches.
* **Cloud Sync Engine:** Real-time synchronization powered by Firebase Cloud Firestore.

### 6. Team & Staff Management
* **RBAC Security:** Invite staff with predefined roles:
  * **Owner:** Full privileges.
  * **Manager:** Access to financials, inventory, and reports.
  * **Biller:** Restricted access to record sales only.

---

## 📊 Summary Comparison Table

| Feature / Limit | Free Tier | Pro (Premium) Tier |
| :--- | :--- | :--- |
| **Store Count Limit** | Max 2 local stores | Unlimited stores |
| **Data Sync** | Local storage only | ☁️ Real-time Cloud backup & sync |
| **Inventory Size** | Limited items | 📦 Unlimited inventory items |
| **Financial Insights** | Standard dashboard | 📊 Detailed P&L (Profit & Loss) reports |
| **Stock Management** | Manual checks | 🔔 Smart low-stock alerts |
| **Invoice Sharing** | Standard sharing | 📱 WhatsApp invoice sharing |
| **App Theme Customization** | System default theme only | 🎨 Light / Dark Mode selector & theme engine |
| **GST Taxation Reports** | Local view | 📁 Export GSTR-1, 2, 3B Excel files |

---

## 🚫 Free Tier Limitations

Free users have local-only access to StoreBook and face several boundaries designed to encourage upgrading:
1. **Local-Only Database:** All data is saved on the device's SQLite/Preferences storage. If the app is uninstalled or the device is lost, data is lost.
2. **Store Count Cap:** Restricted to a maximum of **2 local stores**.
3. **Basic Dashboards:** Access to basic sales summaries but without deep financial breakdowns or trend forecasts.
4. **Locked Appearance:** Restricted to the default system appearance (no inline Dark/Light theme switching).

---

## 💎 Pro (Premium) Features

Upgrade to Pro unlocks full cloud integration and premium tools for high-growth shops:
1. **Cloud Backup & Sync:** Keeps store data secure on Firebase Cloud Firestore with automatic real-time sync across multiple devices.
2. **Unlimited Stores & Items:** Scale the business without caps on how many stores or products can be created.
3. **Detailed P&L Reports:** Generate tax breakdowns (GST/HSN reports) and export transactions directly to Excel.
4. **Smart Low-Stock Alerts:** Automatic alerts when product quantities drop below the safety threshold.
5. **WhatsApp Invoice Sharing:** Share receipts directly with customers via WhatsApp with one click.
6. **Premium Theme Customization:** Toggle between Light/Dark modes and access advanced theme customization.

---

## 👥 Staff & Biller License Inheritance

* **Automatic Pro Status:** Staff members (users registered under the `staff` role or `UserRole.BILLER`) automatically inherit the store owner's Cloud Sync and Pro privileges. This ensures employees can record sales, sync data, and manage inventory seamlessly without requiring individual Google Play subscriptions.

---

## 🏷️ Pricing & Subscription Plans

StoreBook offers three simple billing structures:
* **Monthly Pro:** **₹79 / month** (Auto-renewing subscription via Play Store Billing)
* **Annual Pro:** **₹299 / year** (Save over 65%, billed yearly)
* **Lifetime Pro:** **One-Time Purchase** (Pay once, get forever access to all updates)
