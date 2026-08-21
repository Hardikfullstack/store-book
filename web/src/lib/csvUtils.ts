/**
 * CSV Utility functions for Inventory Import and Export
 * Supports column schema:
 * Item Name, Stock Quan, Unit, Buy Price, Sell Price, Alert Thres, Category, HSN Code, Tax Rate
 */

export interface InventoryCsvRow {
  name: string;
  quantity: number;
  unit: string;
  buyPrice: number;
  sellPrice: number;
  lowStockThreshold: number;
  category: string;
  hsnCode: string;
  taxRate: number;
}

export interface CsvValidationError {
  row: number;
  data: { name: string; quantity: number | string; buyPrice: number | string; sellPrice: number | string };
  errors: string[];
}

export interface CsvValidationResult {
  validItems: InventoryCsvRow[];
  invalidItems: CsvValidationError[];
  totalRows: number;
}

export const INVENTORY_CSV_HEADERS = [
  'Item Name',
  'Stock Quan',
  'Unit',
  'Buy Price',
  'Sell Price',
  'Alert Thres',
  'Category',
  'HSN Code',
  'Tax Rate'
] as const;

/**
 * Escapes a field according to RFC 4180 CSV standard.
 */
function escapeCsvValue(val: unknown): string {
  if (val === null || val === undefined) return '';
  const str = String(val);
  if (str.includes(',') || str.includes('"') || str.includes('\n') || str.includes('\r')) {
    return `"${str.replace(/"/g, '""')}"`;
  }
  return str;
}

/**
 * Generates CSV string for full store inventory items (without ID column).
 */
interface CsvItemShape {
  name?: string;
  itemName?: string;
  quantity?: number;
  stock_quan?: number;
  unit?: string;
  buyPrice?: number;
  buy_price?: number;
  sellPrice?: number;
  sell_price?: number;
  lowStockThreshold?: number;
  low_stock_threshold?: number;
  category?: string;
  hsnCode?: string | number;
  hsn_code?: string | number;
  taxRate?: number;
  tax_rate?: number;
}

export function generateInventoryCsv(items: CsvItemShape[]): string {
  const headerLine = INVENTORY_CSV_HEADERS.join(',');
  const rowLines = items.map((item) => {
    const name = item.name || item.itemName || '';
    const quantity = item.quantity ?? item.stock_quan ?? 0;
    const unit = item.unit || 'pcs';
    const buyPrice = item.buyPrice ?? item.buy_price ?? 0;
    const sellPrice = item.sellPrice ?? item.sell_price ?? 0;
    const alertThres = item.lowStockThreshold ?? item.low_stock_threshold ?? 5;
    const category = item.category || '';
    const hsnCode = item.hsnCode ?? item.hsn_code ?? '';
    const taxRate = item.taxRate ?? item.tax_rate ?? 0;

    return [
      escapeCsvValue(name),
      escapeCsvValue(quantity),
      escapeCsvValue(unit),
      escapeCsvValue(buyPrice),
      escapeCsvValue(sellPrice),
      escapeCsvValue(alertThres),
      escapeCsvValue(category),
      escapeCsvValue(hsnCode),
      escapeCsvValue(taxRate)
    ].join(',');
  });

  return [headerLine, ...rowLines].join('\r\n');
}

/**
 * Generates sample inventory template CSV content without ID column.
 */
export function generateSampleInventoryTemplateCsv(): string {
  const sampleRows = [
    {
      name: 'Bike',
      quantity: 10,
      unit: 'litre',
      buyPrice: 80,
      sellPrice: 100,
      lowStockThreshold: 5,
      category: 'Automotive',
      hsnCode: '8714',
      taxRate: 0
    },
    {
      name: 'Engine Oil',
      quantity: 25,
      unit: 'litre',
      buyPrice: 250,
      sellPrice: 300,
      lowStockThreshold: 5,
      category: 'Automotive',
      hsnCode: '2710',
      taxRate: 18
    },
    {
      name: 'Brake Pads',
      quantity: 50,
      unit: 'pcs',
      buyPrice: 120,
      sellPrice: 180,
      lowStockThreshold: 10,
      category: 'Spares',
      hsnCode: '8708',
      taxRate: 18
    }
  ];

  return generateInventoryCsv(sampleRows);
}

/**
 * Downloads a CSV file to the browser.
 */
export function downloadCsvFile(csvContent: string, fileName: string) {
  const blob = new Blob(['\uFEFF' + csvContent], { type: 'text/csv;charset=utf-8;' });
  const url = URL.createObjectURL(blob);
  const link = document.createElement('a');
  link.href = url;
  link.setAttribute('download', fileName);
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
  URL.revokeObjectURL(url);
}

/**
 * Parses raw CSV line taking quotes into account.
 */
function parseCsvLine(line: string): string[] {
  const result: string[] = [];
  let current = '';
  let inQuotes = false;

  for (let i = 0; i < line.length; i++) {
    const char = line[i];
    if (char === '"') {
      if (inQuotes && line[i + 1] === '"') {
        current += '"';
        i++;
      } else {
        inQuotes = !inQuotes;
      }
    } else if (char === ',' && !inQuotes) {
      result.push(current.trim());
      current = '';
    } else {
      current += char;
    }
  }
  result.push(current.trim());
  return result;
}

/**
 * Parses and validates CSV string against the inventory schema without ID column.
 */
export function parseInventoryCsv(csvText: string): CsvValidationResult {
  const lines = csvText.split(/\r?\n/).map(l => l.trim()).filter(l => l.length > 0);
  if (lines.length === 0) {
    return { validItems: [], invalidItems: [], totalRows: 0 };
  }

  const rawHeaders = parseCsvLine(lines[0]);
  const headerMap: { [key: string]: number } = {};

  rawHeaders.forEach((h, idx) => {
    const cleanHeader = h.toLowerCase().replace(/[^a-z0-9]/g, '');
    headerMap[cleanHeader] = idx;
  });

  const getColValue = (rowArr: string[], ...aliases: string[]): string => {
    for (const alias of aliases) {
      const cleanAlias = alias.toLowerCase().replace(/[^a-z0-9]/g, '');
      if (headerMap[cleanAlias] !== undefined && headerMap[cleanAlias] < rowArr.length) {
        return rowArr[headerMap[cleanAlias]];
      }
    }
    return '';
  };

  const validItems: InventoryCsvRow[] = [];
  const invalidItems: { row: number; data: CsvValidationError['data']; errors: string[] }[] = [];

  for (let i = 1; i < lines.length; i++) {
    const rowNumber = i + 1;
    const rowArr = parseCsvLine(lines[i]);
    if (rowArr.length === 1 && !rowArr[0]) continue; // Skip blank line

    const errors: string[] = [];

    const name = getColValue(rowArr, 'item name', 'name', 'itemname', 'product name');
    const rawQuantity = getColValue(rowArr, 'stock quan', 'stock quantity', 'quantity', 'stock', 'qty');
    const unit = getColValue(rowArr, 'unit') || 'pcs';
    const rawBuyPrice = getColValue(rowArr, 'buy price', 'buy_price', 'purchase price', 'cost price', 'buyprice');
    const rawSellPrice = getColValue(rowArr, 'sell price', 'sell_price', 'sale price', 'selling price', 'sellprice');
    const rawAlertThres = getColValue(rowArr, 'alert thres', 'alert threshold', 'low stock threshold', 'threshold', 'lowstockthreshold');
    const category = getColValue(rowArr, 'category');
    const hsnCode = getColValue(rowArr, 'hsn code', 'hsn', 'hsncode');
    const rawTaxRate = getColValue(rowArr, 'tax rate', 'tax', 'taxrate', 'gst rate');

    if (!name) {
      errors.push('Item Name is required');
    }

    const quantity = rawQuantity !== '' ? Number(rawQuantity) : 0;
    if (isNaN(quantity) || quantity < 0) {
      errors.push('Stock Quantity must be a non-negative number');
    }

    const buyPrice = rawBuyPrice !== '' ? Number(rawBuyPrice) : NaN;
    if (isNaN(buyPrice) || buyPrice < 0) {
      errors.push('Buy Price must be a non-negative number');
    }

    const sellPrice = rawSellPrice !== '' ? Number(rawSellPrice) : NaN;
    if (isNaN(sellPrice) || sellPrice < 0) {
      errors.push('Sell Price must be a non-negative number');
    }

    const lowStockThreshold = rawAlertThres !== '' ? Number(rawAlertThres) : 5;
    if (isNaN(lowStockThreshold) || lowStockThreshold < 0) {
      errors.push('Alert Threshold must be a non-negative number');
    }

    const taxRate = rawTaxRate !== '' ? Number(rawTaxRate) : 0;
    if (isNaN(taxRate) || taxRate < 0) {
      errors.push('Tax Rate must be a non-negative number');
    }

    if (errors.length > 0) {
      invalidItems.push({
        row: rowNumber,
        data: { name: name || '(empty)', quantity: rawQuantity, buyPrice: rawBuyPrice, sellPrice: rawSellPrice },
        errors
      });
    } else {
      validItems.push({
        name,
        quantity,
        unit: unit || 'pcs',
        buyPrice,
        sellPrice,
        lowStockThreshold,
        category,
        hsnCode,
        taxRate
      });
    }
  }

  return {
    validItems,
    invalidItems,
    totalRows: lines.length - 1
  };
}
