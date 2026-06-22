const express = require('express');
const sqlite3 = require('sqlite3').verbose();
const cors = require('cors');
const path = require('path');

const app = express();
app.use(cors());
app.use(express.json());

// Connect to existing SQLite DB
const dbPath = path.resolve(__dirname, '../storebook.db');
const db = new sqlite3.Database(dbPath, (err) => {
    if (err) {
        console.error('Error opening database', err.message);
    } else {
        console.log('Connected to the SQLite database.');
    }
});

// --- API ENDPOINTS ---

// GET Items
app.get('/api/items', (req, res) => {
    db.all('SELECT * FROM items WHERE is_deleted = 0', [], (err, rows) => {
        if (err) return res.status(500).json({ error: err.message });
        res.json({ data: rows });
    });
});

// POST Item
app.post('/api/items', (req, res) => {
    const { name, quantity, unit, buy_price, sell_price, category } = req.body;
    db.run(
        `INSERT INTO items (name, quantity, unit, buy_price, sell_price, category) VALUES (?, ?, ?, ?, ?, ?)`,
        [name, quantity, unit, buy_price, sell_price, category],
        function (err) {
            if (err) return res.status(500).json({ error: err.message });
            res.json({ id: this.lastID });
        }
    );
});

// GET Sales
app.get('/api/sales', (req, res) => {
    db.all('SELECT * FROM sales WHERE is_deleted = 0 ORDER BY timestamp DESC', [], (err, rows) => {
        if (err) return res.status(500).json({ error: err.message });
        res.json({ data: rows });
    });
});

// GET Udhaar (Credit)
app.get('/api/udhaar', (req, res) => {
    db.all('SELECT * FROM udhaar WHERE is_deleted = 0 ORDER BY timestamp DESC', [], (err, rows) => {
        if (err) return res.status(500).json({ error: err.message });
        res.json({ data: rows });
    });
});

// GET Expenses
app.get('/api/expenses', (req, res) => {
    db.all('SELECT * FROM expenses WHERE is_deleted = 0 ORDER BY timestamp DESC', [], (err, rows) => {
        if (err) return res.status(500).json({ error: err.message });
        res.json({ data: rows });
    });
});

// Dashboard Stats Summary
app.get('/api/stats', (req, res) => {
    const stats = {};
    
    db.get('SELECT COUNT(*) as totalItems FROM items WHERE is_deleted = 0', (err, row) => {
        stats.totalItems = row ? row.totalItems : 0;
        
        db.get('SELECT SUM(total_amount) as totalSales FROM sales WHERE is_deleted = 0', (err, row) => {
            stats.totalSales = row ? (row.totalSales || 0) : 0;
            
            db.get('SELECT SUM(amount) as totalUdhaar FROM udhaar WHERE is_deleted = 0', (err, row) => {
                stats.totalUdhaar = row ? (row.totalUdhaar || 0) : 0;
                
                db.get('SELECT SUM(amount) as totalExpenses FROM expenses WHERE is_deleted = 0', (err, row) => {
                    stats.totalExpenses = row ? (row.totalExpenses || 0) : 0;
                    res.json({ data: stats });
                });
            });
        });
    });
});

const PORT = process.env.PORT || 5000;
app.listen(PORT, () => {
    console.log(`Server running on port ${PORT}`);
});
