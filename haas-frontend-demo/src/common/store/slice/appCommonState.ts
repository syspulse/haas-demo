import { IAuth, IEnroll } from "@app/common/models/auth";
import { IToken } from "@app/common/models/token";
import { PayloadAction, createSlice } from "@reduxjs/toolkit";

interface ICommonState {
  isAuthorized: boolean;
  enroll?: Partial<IEnroll> | null;
  language: "en" | string;
  notifications: CommonNotification[];
  latestNotification: CommonNotification | null;
  moduleOutletHeaderText: string;
  theme: "light" | "dark";
  notebook: {
    width: string;
    height: string;
    token: string;
  };
}

export type NotificationSeverity = "success" | "error" | "warn" | "info";
export type NotificationType = "toast";

export class CommonNotification {
  constructor(
    public type: NotificationType,
    public severity: NotificationSeverity,
    public error: unknown,
    public message?: string,
    public stamp?: number
  ) {
    this.message = message ? message : "somethingWentWrong";
    this.stamp = Date.now();
  }
}

const initialState: ICommonState = {
  isAuthorized: false,
  enroll: null,
  language: "universe",
  notifications: [],
  latestNotification: null,
  moduleOutletHeaderText: "",
  theme: "light",
  notebook: {
    width: "0px",
    height: "0px",
    token: "",
  },
};

const appCommonSlice = createSlice({
  name: "common",
  initialState: { ...initialState },
  reducers: {
    logOut: () => {},
    setEnroll: (state: ICommonState, action: PayloadAction<Partial<IEnroll>>) => {
      state.enroll = state.enroll ? { ...state.enroll, ...action.payload } : action.payload;
    },
    setEnrollStep: (state: ICommonState, action: PayloadAction<string>) => {
      state.enroll = { ...state.enroll, phase: action.payload };
    },
    setUser: (state: ICommonState, action: PayloadAction<IEnroll>) => {
      state.enroll = action.payload;
      state.isAuthorized = true;
    },
    setLanguage: (state: ICommonState, action: PayloadAction<string>) => {
      state.language = action.payload;
    },
    setAccessToken: (state: ICommonState, action: PayloadAction<Partial<IEnroll>>) => {
      state.enroll && (state.enroll.accessToken = action.payload.accessToken as string);
    },
    setNotification: (state: ICommonState, action: PayloadAction<CommonNotification>) => {
      state.notifications.push(action.payload);
      state.latestNotification = action.payload;
    },
    setModuleOutletHeaderText: (state: ICommonState, action: PayloadAction<string>) => {
      state.moduleOutletHeaderText = action.payload;
    },
    setTheme: (state: ICommonState, action: PayloadAction<"light" | "dark">) => {
      state.theme = action.payload;
    },
    setNotebookToken: (state: ICommonState, action: PayloadAction<string>) => {
      state.notebook.token = action.payload;
    },
    setNotebookSize: (state: ICommonState, action: PayloadAction<{ width: string; height: string }>) => {
      state.notebook.width = action.payload.width;
      state.notebook.height = action.payload.height;
    },
  },
});

export const {
  logOut,
  setNotification,
  setModuleOutletHeaderText,
  setEnroll,
  setUser,
  setTheme,
  setAccessToken,
  setLanguage,
  setNotebookToken,
  setNotebookSize,
  setEnrollStep,
} = appCommonSlice.actions;

export default appCommonSlice.reducer;
