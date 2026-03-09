import { configureStore, createSlice, PayloadAction } from '@reduxjs/toolkit';

interface BillingState {
  outstanding: number;
  lastUpdated: string | null;
}

const initialState: BillingState = {
  outstanding: 0,
  lastUpdated: null
};

const billingSlice = createSlice({
  name: 'billing',
  initialState,
  reducers: {
    setOutstanding(state, action: PayloadAction<number>) {
      state.outstanding = action.payload;
      state.lastUpdated = new Date().toISOString();
    }
  }
});

export const { setOutstanding } = billingSlice.actions;

export const legacyStore = configureStore({
  reducer: {
    billing: billingSlice.reducer
  }
});

export type LegacyStore = typeof legacyStore;
export type LegacyState = ReturnType<LegacyStore['getState']>;
