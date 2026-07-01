import { createSlice, PayloadAction } from '@reduxjs/toolkit';

interface InventoryState {
  items: any[];
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
    setInventory: (state, action: PayloadAction<any[]>) => {
      state.items = action.payload;
      state.lastSynced = Date.now();
    },
    updateInventoryItem: (state, action: PayloadAction<any>) => {
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
