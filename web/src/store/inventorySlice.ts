import { createSlice, PayloadAction } from '@reduxjs/toolkit';

/** Wide duck-typed item shape held in Redux — covers both LocalItem (snake_case)
    and Item (camelCase) used by different consumers. */
type InventoryItem = { id: string } & Record<string, unknown>;

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
    updateInventoryItem: (state, action: PayloadAction<InventoryItem>) => {
      const index = state.items.findIndex(i => i.id === action.payload.id);
      if (index !== -1) {
        state.items[index] = { ...state.items[index], ...action.payload };
      } else {
        state.items.push(action.payload);
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
