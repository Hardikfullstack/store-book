import { createSlice, PayloadAction } from '@reduxjs/toolkit';

export interface InventoryItem {
  id: string;
  name: string;
  quantity: number;
  unit: string;
  buyPrice?: number;
  sellPrice?: number;
  lowStockThreshold?: number;
  category: string;
  photoPath?: string | null;
  hsnCode?: string | null;
  barcode?: string | null;
  taxRate?: number | null;
  batchLotNumber?: string | null;
  expiryDate?: string | null;
  isDeleted?: boolean | number;
  updatedAt?: number;
  // Legacy snake_case fields
  buy_price?: number;
  sell_price?: number;
  low_stock_threshold?: number;
  is_deleted?: number;
  updated_at?: number;
}
interface InventoryState {
  items: InventoryItem[];
  lastSynced: number;
}

const initialState: InventoryState = {
  items: [],
  lastSynced: 0,
};

export const inventorySlice = createSlice({
  name: 'inventory',
  initialState,
  reducers: {
    setInventory: (state, action: PayloadAction<InventoryItem[]>) => {
      state.items = action.payload;
      state.lastSynced = Date.now();
    },
    updateInventoryItem: (state, action: PayloadAction<Partial<InventoryItem> & { id: string }>) => {
      const index = state.items.findIndex(i => i.id === action.payload.id);
      if (index !== -1) {
        state.items[index] = { ...state.items[index], ...action.payload } as InventoryItem;
      } else {
        state.items.push(action.payload as InventoryItem);
      }
      state.lastSynced = Date.now();
    },
    clearInventory: (state) => {
      state.items = [];
      state.lastSynced = 0;
    }
  },
});

export const { setInventory, updateInventoryItem, clearInventory } = inventorySlice.actions;
export default inventorySlice.reducer;
