package com.storebook.inventoryapp.shared.domain.repository

import com.storebook.inventoryapp.shared.data.local.StoreBookDatabase
import com.storebook.inventoryapp.shared.domain.models.getDefaultCategoriesForBusinessType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SystemRepository(
    private val database: StoreBookDatabase
) {
    private val queries = database.storeBookQueries

    suspend fun clearLocalDatabase() = withContext(Dispatchers.IO) {
        queries.deleteAllItems()
        queries.deleteAllSales()
        queries.deleteAllSaleItems()
        queries.deleteAllUdhaar()
        queries.deleteAllExpenses()
        queries.deleteAllSuppliers()
        queries.deleteAllPurchases()
        queries.deleteAllPurchaseItems()
        queries.deleteAllItemBatches()
        queries.clearStockAdjustments()
        queries.clearCategories()
    }

    suspend fun seedDummyData(
        businessType: String = "general",
        storeId: String = "default"
    ) = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()

        queries.transaction {
            // 1. Seed Categories for this businessType
            val defaultCategories = getDefaultCategoriesForBusinessType(businessType)
            val existingCats = queries.getCategoriesByBusinessType(businessType).executeAsList().map { it.name }.toSet()
            for (catName in defaultCategories) {
                if (!existingCats.contains(catName)) {
                    queries.insertCategory(
                        name = catName,
                        businessType = businessType,
                        storeId = storeId,
                        cloudId = null,
                        isDefault = 1L,
                        isSynced = 0L,
                        updatedAt = now
                    )
                }
            }

            // 2. Seed Items tailored to the businessType
            val existingItems = queries.getAllItems().executeAsList().map { it.name }.toSet()
            val seedSpecs = getSeedItemsForBusinessType(businessType)

            for (spec in seedSpecs) {
                if (!existingItems.contains(spec.name)) {
                    queries.insertItem(
                        name = spec.name,
                        quantity = spec.quantity,
                        unit = spec.unit,
                        buy_price = spec.buyPrice,
                        sell_price = spec.sellPrice,
                        low_stock_threshold = spec.lowStockThreshold,
                        category = spec.category,
                        photo_path = null,
                        barcode = null,
                        hsn_code = null,
                        tax_rate = spec.taxRate,
                        updated_at = now
                    )
                }
            }

            val allItems = queries.getAllItems().executeAsList()
            if (allItems.isNotEmpty()) {
                val sampleItems = allItems.take(5)

                // 3. Seed Suppliers
                val suppliersList = listOf(
                    Triple("Ramesh Wholesale Distributors", "+919876543210", "24AAACC1234D1Z5"),
                    Triple("Shree Ganesh Trading Co.", "+919811223344", "24AABCS5678E1Z9"),
                    Triple("Balaji Marketing Agency", "+919988776655", "24AACCB9012F1Z2")
                )
                val existingSuppliers = queries.getAllSuppliers().executeAsList().map { it.name }.toSet()
                val supplierIds = mutableListOf<Long>()

                for ((sName, sPhone, sGstin) in suppliersList) {
                    if (!existingSuppliers.contains(sName)) {
                        queries.insertSupplier(
                            name = sName,
                            phone = sPhone,
                            gstin = sGstin,
                            address = "Shop 12, APMC Market",
                            updated_at = now
                        )
                        supplierIds.add(queries.getLastInsertRowId().executeAsOne())
                    }
                }

                // 4. Seed Sales & Sale Items
                val existingSales = queries.getAllSales().executeAsList()
                if (existingSales.isEmpty()) {
                    val salesData = listOf(
                        Triple("Rahul Sharma", now - 2 * 86400000L, 0.0),
                        Triple("Priya Patel", now - 1 * 86400000L, 20.0),
                        Triple("Walk-in Customer", now - 12 * 3600000L, 0.0),
                        Triple("Vikram Singh", now - 3 * 3600000L, 10.0),
                        Triple("Walk-in Customer", now - 30 * 60000L, 0.0)
                    )

                    for ((cName, saleTime, discount) in salesData) {
                        val itemsForSale = sampleItems.shuffled().take(2.coerceAtMost(sampleItems.size))
                        var total = 0.0
                        for (it in itemsForSale) {
                            total += it.sell_price * 2.0
                        }
                        val finalTotal = (total - discount).coerceAtLeast(0.0)

                        queries.insertSale(
                            timestamp = saleTime,
                            total_amount = finalTotal,
                            discount_amount = discount,
                            customer_name = cName,
                            customer_gstin = null,
                            business_gstin = null,
                            customer_address = null,
                            business_address = null,
                            type = "SALE",
                            notes = "Sample seed transaction",
                            updated_at = saleTime
                        )
                        val saleId = queries.getLastInsertRowId().executeAsOne()

                        for (it in itemsForSale) {
                            queries.insertSaleItem(
                                sale_id = saleId,
                                item_id = it.id,
                                item_name = it.name,
                                quantity = 2.0,
                                unit = it.unit,
                                sell_price = it.sell_price,
                                buy_price = it.buy_price,
                                tax_rate = it.tax_rate,
                                hsn_code = it.hsn_code,
                                updated_at = saleTime
                            )
                        }
                    }
                }

                // 5. Seed Udhaar
                val existingUdhaar = queries.getAllUdhaar().executeAsList()
                if (existingUdhaar.isEmpty()) {
                    val udhaarData = listOf(
                        Triple("Rahul Sharma", 850.0, "GIVEN"),
                        Triple("Priya Patel", 300.0, "RECEIVED"),
                        Triple("Mohit Gupta", 1200.0, "GIVEN"),
                        Triple("Sunil Verma", 450.0, "GIVEN")
                    )
                    for ((uName, uAmount, uType) in udhaarData) {
                        queries.insertUdhaar(
                            customer_name = uName,
                            amount = uAmount,
                            type = uType,
                            timestamp = now - 86400000L,
                            notes = if (uType == "GIVEN") "Credit balance" else "Cash payment received",
                            updated_at = now
                        )
                    }
                }

                // 6. Seed Expenses
                val existingExpenses = queries.getAllExpenses().executeAsList()
                if (existingExpenses.isEmpty()) {
                    val expenseData = listOf(
                        Triple("Shop Rent", "Monthly shop premises rent", 6500.0),
                        Triple("Electricity", "Electricity bill payment", 1450.0),
                        Triple("Refreshment", "Tea, coffee & snacks for staff", 320.0),
                        Triple("Maintenance", "Tubelight & wiring repair", 450.0)
                    )
                    for ((eType, eDesc, eAmt) in expenseData) {
                        queries.insertExpense(
                            type = eType,
                            description = eDesc,
                            amount = eAmt,
                            timestamp = now - 2 * 86400000L,
                            supplier_name = null,
                            supplier_phone = null,
                            updated_at = now
                        )
                    }
                }

                // 7. Seed Purchases
                val existingPurchases = queries.getAllPurchases().executeAsList()
                val allSuppliers = queries.getAllSuppliers().executeAsList()
                if (existingPurchases.isEmpty() && allSuppliers.isNotEmpty()) {
                    val s = allSuppliers.first()
                    val pItems = sampleItems.take(2)
                    var pTotal = 0.0
                    for (it in pItems) {
                        pTotal += it.buy_price * 10.0
                    }
                    queries.insertPurchase(
                        supplier_id = s.id,
                        supplier_name = s.name,
                        total_amount = pTotal,
                        tax_amount = 0.0,
                        type = "BILL",
                        timestamp = now - 3 * 86400000L,
                        notes = "Initial stock purchase",
                        updated_at = now
                    )
                    val pId = queries.getLastInsertRowId().executeAsOne()
                    for (it in pItems) {
                        queries.insertPurchaseItem(
                            purchase_id = pId,
                            item_id = it.id,
                            item_name = it.name,
                            quantity = 10.0,
                            unit = it.unit,
                            buy_price = it.buy_price,
                            updated_at = now
                        )
                    }
                }

                // 8. Seed Stock Adjustments
                val existingAdjustments = queries.getAllStockAdjustments().executeAsList()
                if (existingAdjustments.isEmpty() && sampleItems.isNotEmpty()) {
                    val targetItem = sampleItems.first()
                    queries.insertStockAdjustment(
                        item_id = targetItem.id,
                        item_name = targetItem.name,
                        reason = "Damaged during handling",
                        delta = -2.0,
                        timestamp = now - 86400000L,
                        is_deleted = 0L,
                        cloud_id = null,
                        is_synced = 0L,
                        updated_at = now
                    )
                }
            }
        }
    }

    private data class SeedItemSpec(
        val name: String,
        val category: String,
        val unit: String,
        val buyPrice: Double,
        val sellPrice: Double,
        val quantity: Double,
        val lowStockThreshold: Double = 5.0,
        val taxRate: Double = 0.0,
    )

    private fun getSeedItemsForBusinessType(businessType: String): List<SeedItemSpec> {
        return when (businessType) {
            "grocery" -> listOf(
                SeedItemSpec("Aashirvaad Chakki Atta 10kg", "Staples, Grains & Flours", "packet", 380.0, 440.0, 20.0, 5.0, 0.0),
                SeedItemSpec("India Gate Basmati Rice 5kg", "Staples, Grains & Flours", "bag", 360.0, 425.0, 15.0, 4.0, 0.0),
                SeedItemSpec("Tata Sampann Toor Dal 1kg", "Pulses & Dals", "kg", 145.0, 175.0, 30.0, 5.0, 0.0),
                SeedItemSpec("Fortune Refined Sunflower Oil 1L", "Edible Oils & Ghee", "litre", 118.0, 140.0, 40.0, 10.0, 5.0),
                SeedItemSpec("Amul Pure Cow Ghee 1L Tin", "Edible Oils & Ghee", "tin", 540.0, 620.0, 12.0, 3.0, 12.0),
                SeedItemSpec("Tata Salt Iodized 1kg", "Spices, Masalas & Seasonings", "packet", 22.0, 28.0, 50.0, 10.0, 0.0),
                SeedItemSpec("MDH Deggi Mirch Powder 100g", "Spices, Masalas & Seasonings", "box", 68.0, 85.0, 35.0, 5.0, 5.0),
                SeedItemSpec("Parle-G Gold Biscuits 1kg", "Snacks, Biscuits & Namkeen", "packet", 110.0, 130.0, 25.0, 5.0, 18.0),
                SeedItemSpec("Haldiram Nagpur Aloo Bhujia 400g", "Snacks, Biscuits & Namkeen", "packet", 85.0, 105.0, 30.0, 5.0, 12.0),
                SeedItemSpec("Coca-Cola Cold Drink 750ml", "Beverages & Cold Drinks", "bottle", 36.0, 45.0, 40.0, 8.0, 28.0),
                SeedItemSpec("Brooke Bond Red Label Tea 500g", "Beverages & Cold Drinks", "box", 210.0, 260.0, 20.0, 5.0, 5.0),
                SeedItemSpec("Surf Excel Quick Wash Detergent 1kg", "Cleaning & Detergents", "packet", 125.0, 155.0, 20.0, 5.0, 18.0),
                SeedItemSpec("Dettol Original Bathing Soap 75g", "Personal Care & Soaps", "pcs", 34.0, 42.0, 45.0, 8.0, 18.0),
                SeedItemSpec("Amul Salted Butter 500g", "Dairy, Butter & Eggs", "box", 230.0, 275.0, 15.0, 4.0, 12.0)
            )
            "medical" -> listOf(
                SeedItemSpec("Dolo 650 Tablets (Strip of 15)", "Prescription Medicines", "strip", 24.0, 33.6, 50.0, 10.0, 12.0),
                SeedItemSpec("Augmentin 625 Duo (Strip of 10)", "Prescription Medicines", "strip", 160.0, 204.0, 25.0, 5.0, 12.0),
                SeedItemSpec("Pan-D Gastro-Resistant (Strip of 15)", "Prescription Medicines", "strip", 145.0, 199.0, 30.0, 5.0, 12.0),
                SeedItemSpec("Becadexamin Multivitamin (Bottle 30s)", "OTC & Health Supplements", "bottle", 42.0, 56.0, 40.0, 8.0, 12.0),
                SeedItemSpec("Revital H Daily Health Supplement 30s", "OTC & Health Supplements", "bottle", 240.0, 310.0, 20.0, 5.0, 18.0),
                SeedItemSpec("Dettol Antiseptic Liquid 550ml", "First Aid & Surgical", "bottle", 165.0, 214.0, 20.0, 4.0, 18.0),
                SeedItemSpec("Hansaplast Bandage Regular (100s)", "First Aid & Surgical", "box", 160.0, 220.0, 15.0, 3.0, 12.0),
                SeedItemSpec("Dr. Morepen Digital Thermometer", "Medical Equipment & Devices", "pcs", 135.0, 225.0, 15.0, 3.0, 18.0),
                SeedItemSpec("Dabur Chyawanprash Special 1kg", "Ayurvedic & Herbal", "jar", 310.0, 395.0, 15.0, 3.0, 12.0),
                SeedItemSpec("Himalaya Baby Wet Wipes 72s", "Baby & Mother Care", "packet", 130.0, 175.0, 20.0, 4.0, 12.0)
            )
            "electronics" -> listOf(
                SeedItemSpec("boAt Bassheads 100 Wired Earphones", "Earphones, Headphones & Audio", "box", 260.0, 399.0, 30.0, 5.0, 18.0),
                SeedItemSpec("Noise ColorFit Pulse 2 Smartwatch", "Smart Watches & Wearables", "box", 1150.0, 1699.0, 12.0, 3.0, 18.0),
                SeedItemSpec("Mi 10000mAh Power Bank 3i", "Chargers, Cables & Power Banks", "box", 850.0, 1299.0, 15.0, 3.0, 18.0),
                SeedItemSpec("Type-C 65W Fast Braided Cable 1m", "Chargers, Cables & Power Banks", "pcs", 140.0, 299.0, 40.0, 8.0, 18.0),
                SeedItemSpec("Logitech B170 Wireless Mouse", "Computer & Laptop Peripherals", "box", 460.0, 645.0, 20.0, 4.0, 18.0),
                SeedItemSpec("SanDisk Ultra 64GB MicroSD Card", "Computer & Laptop Peripherals", "packet", 380.0, 549.0, 25.0, 5.0, 18.0),
                SeedItemSpec("9H Tempered Glass Screen Guard", "Mobile Cases & Screen Guards", "pcs", 20.0, 99.0, 100.0, 15.0, 18.0),
                SeedItemSpec("Syska 1000W Dry Iron Box", "Small Home Appliances", "box", 490.0, 699.0, 8.0, 2.0, 18.0)
            )
            "hardware" -> listOf(
                SeedItemSpec("Taparia 8-inch Combination Pliers", "Hand Tools & Measuring", "pcs", 185.0, 260.0, 20.0, 4.0, 18.0),
                SeedItemSpec("Freemans 5m Steel Measuring Tape", "Hand Tools & Measuring", "pcs", 110.0, 165.0, 30.0, 5.0, 18.0),
                SeedItemSpec("Bosch 13mm Impact Drill Machine", "Power Tools & Machine Accessories", "box", 2400.0, 3200.0, 5.0, 1.0, 18.0),
                SeedItemSpec("Astral 1-inch CPVC Ball Valve", "Plumbing, Pipes & Fittings", "pcs", 145.0, 210.0, 25.0, 5.0, 18.0),
                SeedItemSpec("Asian Paints Tractor Emulsion White 4L", "Paints, Primers & Brushes", "can", 480.0, 640.0, 10.0, 2.0, 18.0),
                SeedItemSpec("Fevicol SH Synthetic Adhesive 1kg", "Adhesives, Tapes & Sealants", "jar", 195.0, 260.0, 20.0, 4.0, 18.0),
                SeedItemSpec("Godrej Nav-Tal 7 Levers Brass Padlock", "Locks, Hinges & Door Fittings", "box", 380.0, 495.0, 15.0, 3.0, 18.0),
                SeedItemSpec("Drywall Screws 1.5-inch (Box of 500)", "Fasteners, Screws, Nuts & Bolts", "box", 140.0, 220.0, 25.0, 5.0, 18.0)
            )
            "dairy_sweets" -> listOf(
                SeedItemSpec("Amul Gold Full Cream Milk 1L", "Fresh Milk & Buttermilk", "packet", 64.0, 68.0, 40.0, 10.0, 0.0),
                SeedItemSpec("Fresh Malai Paneer 500g", "Curd, Paneer & Cheese", "packet", 140.0, 180.0, 20.0, 5.0, 0.0),
                SeedItemSpec("Pure Desi Cow Ghee 1kg", "Pure Ghee & Butter", "jar", 550.0, 680.0, 15.0, 3.0, 5.0),
                SeedItemSpec("Kaju Katli Special 500g", "Traditional Sweets (Mithai)", "box", 420.0, 550.0, 12.0, 2.0, 5.0),
                SeedItemSpec("Gulab Jamun (1kg Tin)", "Traditional Sweets (Mithai)", "tin", 190.0, 260.0, 15.0, 3.0, 5.0),
                SeedItemSpec("Rasgulla Fresh (1kg Tin)", "Bengali & Milk Sweets", "tin", 180.0, 250.0, 15.0, 3.0, 5.0),
                SeedItemSpec("Shrikhand Elaichi 500g", "Ice Cream, Kulfi & Desserts", "cup", 95.0, 130.0, 20.0, 4.0, 5.0),
                SeedItemSpec("Ratnami Sev Mamra Namkeen 500g", "Namkeen & Farsan", "packet", 55.0, 75.0, 30.0, 5.0, 12.0)
            )
            "stationery" -> listOf(
                SeedItemSpec("Classmate Long Notebook 172 Pages", "Notebooks, Registers & Diaries", "pcs", 45.0, 60.0, 50.0, 10.0, 12.0),
                SeedItemSpec("Cello Butterflow Blue Ball Pen (Pack of 5)", "Pens, Pencils & Markers", "packet", 40.0, 50.0, 40.0, 8.0, 12.0),
                SeedItemSpec("Camlin Kokuyo Geometry Box", "School Supplies & Geometry Boxes", "box", 95.0, 130.0, 20.0, 4.0, 12.0),
                SeedItemSpec("JK Copier A4 Paper 75 GSM (500 Sheets)", "Paper Reams & Photocopy Paper", "ream", 240.0, 320.0, 15.0, 3.0, 18.0),
                SeedItemSpec("Kangaro Stapler No. 10 with Pins", "Office Stationery & Desk Accessories", "box", 65.0, 95.0, 25.0, 5.0, 18.0),
                SeedItemSpec("Fevistick Glue Stick 15g", "Art, Craft & Drawing Supplies", "pcs", 20.0, 30.0, 40.0, 8.0, 18.0),
                SeedItemSpec("Solo Executive Display File (20 Pockets)", "Files, Folders & Document Bags", "pcs", 80.0, 120.0, 20.0, 4.0, 18.0)
            )
            else -> listOf(
                SeedItemSpec("Aashirvaad Whole Wheat Atta 5kg", "Daily Essentials", "packet", 210.0, 245.0, 20.0, 5.0, 0.0),
                SeedItemSpec("Fortune Refined Sunflower Oil 1L", "Daily Essentials", "packet", 115.0, 135.0, 30.0, 5.0, 5.0),
                SeedItemSpec("Tata Tea Gold Leaf 500g", "Beverages & Drinks", "packet", 240.0, 290.0, 25.0, 5.0, 5.0),
                SeedItemSpec("Nescafe Classic Instant Coffee 50g", "Beverages & Drinks", "jar", 135.0, 170.0, 20.0, 4.0, 18.0),
                SeedItemSpec("Britannia Good Day Butter Biscuits 200g", "Snacks & Packaged Food", "packet", 28.0, 35.0, 50.0, 10.0, 18.0),
                SeedItemSpec("Maggi 2-Minute Masala Noodles 4-Pack", "Snacks & Packaged Food", "packet", 46.0, 56.0, 40.0, 8.0, 12.0),
                SeedItemSpec("Colgate Strong Teeth Toothpaste 200g", "Personal Care & Hygiene", "box", 95.0, 125.0, 30.0, 5.0, 18.0),
                SeedItemSpec("Head & Shoulders Shampoo 180ml", "Personal Care & Hygiene", "bottle", 140.0, 185.0, 20.0, 4.0, 18.0),
                SeedItemSpec("Vim Dishwash Bar 300g (Pack of 3)", "Cleaning & Household", "packet", 48.0, 60.0, 40.0, 8.0, 18.0),
                SeedItemSpec("Classmate Long Notebook 172 Pages", "Stationery & Paper", "pcs", 45.0, 60.0, 50.0, 10.0, 12.0),
                SeedItemSpec("Amul Fresh Malai Paneer 200g", "Dairy Products", "packet", 75.0, 95.0, 25.0, 5.0, 0.0),
                SeedItemSpec("Eveready 1012 AAA Batteries (Pack of 4)", "Miscellaneous", "packet", 48.0, 65.0, 30.0, 5.0, 18.0)
            )
        }
    }
}
