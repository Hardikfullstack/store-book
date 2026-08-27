import { configureStore, combineReducers } from '@reduxjs/toolkit';
import {
  persistStore,
  persistReducer,
  FLUSH,
  REHYDRATE,
  PAUSE,
  PERSIST,
  PURGE,
  REGISTER,
} from 'redux-persist';
import storage from 'redux-persist/lib/storage'; // defaults to localStorage for web
import cartReducer from './cartSlice';
import inventoryReducer from './inventorySlice';
import udhaarReducer from './udhaarSlice';

const persistConfig = {
  key: 'storebook-root',
  storage,
  whitelist: ['cart', 'inventory', 'udhaar'],
};

const rootReducer = combineReducers({
  cart: cartReducer,
  inventory: inventoryReducer,
  udhaar: udhaarReducer,
});

const persistedReducer = persistReducer(persistConfig, rootReducer);

export const store = configureStore({
  reducer: persistedReducer,
  middleware: (getDefaultMiddleware) =>
    getDefaultMiddleware({
      serializableCheck: {
        // Ignore all redux-persist actions from serialization check
        ignoredActions: [FLUSH, REHYDRATE, PAUSE, PERSIST, PURGE, REGISTER],
      },
    }),
});

export const persistor = persistStore(store);

export type RootState = ReturnType<typeof store.getState>;
export type AppDispatch = typeof store.dispatch;
