import { PayloadAction, createSlice } from "@reduxjs/toolkit";

const initialState: any = {
  createUpdateLabels: {
    state: {},
    active: false,
  },
  findLabels: "",
  createUpdateTokens: {
    state: {},
    active: false,
  },
};

const appControlSlice = createSlice({
  name: "control",
  initialState: { ...initialState },
  reducers: {
    createUpdateLabel: (state: any, action: PayloadAction<Partial<any>>) => {
      state.createUpdateLabels = {
        state: action.payload,
        active: true,
      };
    },
    hideLabelForm: (state: any) => {
      state.createUpdateLabels = {
        state: {},
        active: false,
      };
    },
    searchLabels: (state: any, action: PayloadAction<string>) => {
      state.findLabels = action.payload;
    },
    createUpdateToken: (state: any, action: PayloadAction<Partial<any>>) => {
      state.createUpdateTokens = {
        state: action.payload,
        active: true,
      };
    },
    hideTokenForm: (state: any) => {
      state.createUpdateTokens = {
        state: {},
        active: false,
      };
    },
  },
});

export const { createUpdateLabel, hideLabelForm, searchLabels, createUpdateToken, hideTokenForm } =
  appControlSlice.actions;

export default appControlSlice.reducer;
