import { createSlice, PayloadAction } from '@reduxjs/toolkit';
import type { DcUdhaarEntry } from '@/types/dataconnect';

interface UdhaarState {
  records: DcUdhaarEntry[];
  lastSynced: number;
}

const initialState: UdhaarState = {
  records: [],
  lastSynced: 0,
};

export const udhaarSlice = createSlice({
  name: 'udhaar',
  initialState,
  reducers: {
    setUdhaars: (state, action: PayloadAction<DcUdhaarEntry[]>) => {
      state.records = action.payload;
      state.lastSynced = Date.now();
    },
    updateUdhaarRecord: (state, action: PayloadAction<Partial<DcUdhaarEntry> & { id: string }>) => {
      const index = state.records.findIndex(i => i.id === action.payload.id);
      if (index !== -1) {
        state.records[index] = { ...state.records[index], ...action.payload };
      } else {
        state.records.push(action.payload as DcUdhaarEntry);
      }
      state.lastSynced = Date.now();
    },
    removeUdhaarRecord: (state, action: PayloadAction<string>) => {
      state.records = state.records.filter(i => i.id !== action.payload);
      state.lastSynced = Date.now();
    },
    clearUdhaars: (state) => {
      state.records = [];
      state.lastSynced = 0;
    }
  },
});

export const { setUdhaars, updateUdhaarRecord, removeUdhaarRecord, clearUdhaars } = udhaarSlice.actions;
export default udhaarSlice.reducer;