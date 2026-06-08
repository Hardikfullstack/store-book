package com.pdfscanner.editorapp.ui.viewmodels

import android.app.Application
import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pdfscanner.editorapp.data.repository.CartItem
import com.pdfscanner.editorapp.data.repository.CustomerBalance
import com.pdfscanner.editorapp.data.repository.ExpenseEntry
import com.pdfscanner.editorapp.data.repository.Item
import com.pdfscanner.editorapp.data.repository.Sale
import com.pdfscanner.editorapp.data.repository.StoreBookRepository
import com.pdfscanner.editorapp.data.repository.UdhaarEntry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.File
import java.io.FileOutputStream
import java.io.InputStreamReader

class StoreBookViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = StoreBookRepository(application.applicationContext)

    // Data Flows
    private val _allItems = MutableStateFlow<List<Item>>(emptyList())
    val allItems: StateFlow<List<Item>> = _allItems

    private val _lowStockItems = MutableStateFlow<List<Item>>(emptyList())
    val lowStockItems: StateFlow<List<Item>> = _lowStockItems

    private val _salesList = MutableStateFlow<List<Sale>>(emptyList())
    val salesList: StateFlow<List<Sale>> = _salesList

    private val _udhaarBalances = MutableStateFlow<List<CustomerBalance>>(emptyList())
    val udhaarBalances: StateFlow<List<CustomerBalance>> = _udhaarBalances

    private val _expensesList = MutableStateFlow<List<ExpenseEntry>>(emptyList())
    val expensesList: StateFlow<List<ExpenseEntry>> = _expensesList

    // Cart / Checkout State
    var cartItems by mutableStateOf<List<CartItem>>(emptyList())
        private set

    var cartDiscount by mutableStateOf(0.0)
    var cartCustomerName by mutableStateOf("")
    var cartNotes by mutableStateOf("")

    // Premium status demo (defaults to true for demo UX, toggleable in settings)
    var isPremiumUser by mutableStateOf(true)

    // Undo mechanism
    var lastSaleId by mutableStateOf<Long?>(null)
        private set
    var lastSaleTime by mutableStateOf(0L)
        private set

    init {
        loadAllData()
    }

    fun loadAllData() {
        viewModelScope.launch {
            val items = repository.getActiveItems()
            _allItems.value = items
            _lowStockItems.value = items.filter { it.quantity <= it.lowStockThreshold }
            _salesList.value = repository.getSales()
            _udhaarBalances.value = repository.getUdhaarBalances()
            _expensesList.value = repository.getExpenses()
        }
    }

    // --- Inventory Actions ---

    fun addItem(
        name: String,
        quantity: Double,
        unit: String,
        buyPrice: Double,
        sellPrice: Double,
        threshold: Double,
        category: String
    ) {
        viewModelScope.launch {
            val item = Item(
                name = name.trim(),
                quantity = quantity,
                unit = unit,
                buyPrice = buyPrice,
                sellPrice = sellPrice,
                lowStockThreshold = threshold,
                category = category
            )
            repository.insertItem(item)
            loadAllData()
        }
    }

    fun updateItem(
        id: Long,
        name: String,
        quantity: Double,
        unit: String,
        buyPrice: Double,
        sellPrice: Double,
        threshold: Double,
        category: String
    ) {
        viewModelScope.launch {
            val item = Item(
                id = id,
                name = name.trim(),
                quantity = quantity,
                unit = unit,
                buyPrice = buyPrice,
                sellPrice = sellPrice,
                lowStockThreshold = threshold,
                category = category
            )
            repository.updateItem(item)
            loadAllData()
        }
    }

    fun deleteItem(id: Long) {
        viewModelScope.launch {
            repository.softDeleteItem(id)
            loadAllData()
        }
    }

    // --- Cart Actions ---

    fun addToCart(item: Item, qty: Double) {
        val existing = cartItems.find { it.item.id == item.id }
        if (existing != null) {
            existing.quantity += qty
            // Trigger Compose recomposition
            cartItems = cartItems.toList()
        } else {
            cartItems = cartItems + CartItem(item, qty)
        }
    }

    fun updateCartQty(item: Item, qty: Double) {
        val existing = cartItems.find { it.item.id == item.id }
        if (existing != null) {
            if (qty <= 0.0) {
                removeFromCart(item)
            } else {
                existing.quantity = qty
                cartItems = cartItems.toList()
            }
        }
    }

    fun removeFromCart(item: Item) {
        cartItems = cartItems.filterNot { it.item.id == item.id }
    }

    fun clearCart() {
        cartItems = emptyList()
        cartDiscount = 0.0
        cartCustomerName = ""
        cartNotes = ""
    }

    // --- Sales Actions ---

    fun checkout(onSuccess: (Long, Double) -> Unit) {
        if (cartItems.isEmpty()) return
        viewModelScope.launch {
            val saleId = repository.recordSale(
                itemsInCart = cartItems,
                discount = cartDiscount,
                customerName = cartCustomerName.trim().takeIf { it.isNotBlank() },
                notes = cartNotes.trim().takeIf { it.isNotBlank() }
            )
            if (saleId != -1L) {
                lastSaleId = saleId
                lastSaleTime = System.currentTimeMillis()
                
                // Calculate checkout grand total
                var subtotal = 0.0
                for (c in cartItems) {
                    subtotal += c.item.sellPrice * c.quantity
                }
                val total = subtotal - cartDiscount

                clearCart()
                loadAllData()
                onSuccess(saleId, total)
            }
        }
    }

    fun undoLastSale(onSuccess: () -> Unit) {
        val saleId = lastSaleId ?: return
        // Check if 30 seconds have passed
        if (System.currentTimeMillis() - lastSaleTime > 30000) {
            lastSaleId = null
            return
        }
        viewModelScope.launch {
            val success = repository.undoSale(saleId)
            if (success) {
                lastSaleId = null
                loadAllData()
                onSuccess()
            }
        }
    }

    // --- Udhaar Credit Ledger Actions ---

    fun recordUdhaarEntry(customerName: String, amount: Double, type: String, notes: String?) {
        viewModelScope.launch {
            val entry = UdhaarEntry(
                customerName = customerName.trim(),
                amount = amount,
                type = type,
                timestamp = System.currentTimeMillis(),
                notes = notes?.trim()
            )
            repository.insertUdhaarEntry(entry)
            loadAllData()
        }
    }

    // --- Expenses Logger Actions ---

    fun logOverheadExpense(desc: String, amount: Double) {
        viewModelScope.launch {
            val entry = ExpenseEntry(
                type = "OVERHEAD",
                description = desc.trim(),
                amount = amount,
                timestamp = System.currentTimeMillis()
            )
            repository.insertExpense(entry)
            loadAllData()
        }
    }

    fun logRestockItem(itemId: Long, quantity: Double, costPrice: Double, supplier: String?, phone: String?) {
        viewModelScope.launch {
            repository.restockItem(
                itemId = itemId,
                quantityToAdd = quantity,
                costPrice = costPrice,
                supplierName = supplier?.trim()?.takeIf { it.isNotBlank() },
                supplierPhone = phone?.trim()?.takeIf { it.isNotBlank() }
            )
            loadAllData()
        }
    }

    // --- CSV Operations ---

    fun exportInventoryToCSV(context: Context, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val items = repository.getActiveItems()
                val csvContent = StringBuilder()
                // Header
                csvContent.append("ID,Item Name,Stock Quantity,Unit,Buy Price,Sell Price,Alert Threshold,Category\n")
                for (item in items) {
                    csvContent.append("${item.id},\"${item.name}\",${item.quantity},${item.unit},${item.buyPrice},${item.sellPrice},${item.lowStockThreshold},\"${item.category}\"\n")
                }

                val dir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) ?: context.filesDir
                val file = File(dir, "StoreBook_Inventory_${System.currentTimeMillis() / 1000}.csv")
                FileOutputStream(file).use { out ->
                    out.write(csvContent.toString().toByteArray())
                }
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "Unknown Error")
            }
        }
    }

    fun importInventoryFromCSV(context: Context, fileUri: Uri, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val inputStream = context.contentResolver.openInputStream(fileUri)
                    ?: throw Exception("Could not open CSV file")
                val reader = BufferedReader(InputStreamReader(inputStream))
                var line: String? = reader.readLine() // Read header
                var importedCount = 0

                while (reader.readLine().also { line = it } != null) {
                    val tokens = line!!.split(",")
                    if (tokens.size >= 8) {
                        val name = tokens[1].replace("\"", "").trim()
                        val qty = tokens[2].toDoubleOrNull() ?: 0.0
                        val unit = tokens[3].trim()
                        val buyPrice = tokens[4].toDoubleOrNull() ?: 0.0
                        val sellPrice = tokens[5].toDoubleOrNull() ?: 0.0
                        val threshold = tokens[6].toDoubleOrNull() ?: 0.0
                        val category = tokens[7].replace("\"", "").trim()

                        val item = Item(
                            name = name,
                            quantity = qty,
                            unit = unit,
                            buyPrice = buyPrice,
                            sellPrice = sellPrice,
                            lowStockThreshold = threshold,
                            category = category
                        )
                        repository.insertItem(item)
                        importedCount++
                    }
                }
                reader.close()
                if (importedCount > 0) {
                    loadAllData()
                    onSuccess()
                } else {
                    onError("No valid records found in CSV file")
                }
            } catch (e: Exception) {
                onError(e.message ?: "CSV Import failed")
            }
        }
    }
}
