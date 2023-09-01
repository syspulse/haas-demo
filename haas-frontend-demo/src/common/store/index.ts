import { authApi } from "@app/common/services/auth";
import { circApi } from "@app/common/services/circ.service";
import { enrollApi } from "@app/common/services/enroll";
import { tokenApi } from "@app/common/services/token.service";
import { combineReducers, configureStore } from "@reduxjs/toolkit";
import { FLUSH, PAUSE, PERSIST, PURGE, REGISTER, REHYDRATE, persistReducer, persistStore } from "redux-persist";
import storage from "redux-persist/lib/storage";

import { abiApi } from "../services/abi.service";
import { clientApi } from "../services/client.service";
import { etherscanApi } from "../services/etherscan";
import { labelsApi } from "../services/labels.service";
import { transactionMonitoringApi } from "../services/transaction-monitoring";
import { userApi } from "../services/user.service";
import listenerMiddleware from "./listenerMiddleware";
import appCommonSlice from "./slice/appCommonState";
import appControlSlice from "./slice/appControlState";
import { jobApi } from "../services/job.service";
import { notificationsApi } from "../services/nofications.service";
import { holdersApi } from "../services/holders.service";

const persistConfig = () => ({
  key: "persistState",
  version: 1,
  storage: storage,
  blacklist: [
    tokenApi.reducerPath,
    authApi.reducerPath,
    enrollApi.reducerPath,
    transactionMonitoringApi.reducerPath,
    circApi.reducerPath,
    etherscanApi.reducerPath,
    userApi.reducerPath,
    labelsApi.reducerPath,
    abiApi.reducerPath,
    clientApi.reducerPath,
    jobApi.reducerPath,
    notificationsApi.reducerPath,
    holdersApi.reducerPath,
    "control",
  ],
});

const rootReducer = combineReducers({
  common: appCommonSlice,
  control: appControlSlice,
  [authApi.reducerPath]: authApi.reducer,
  [tokenApi.reducerPath]: tokenApi.reducer,
  [enrollApi.reducerPath]: enrollApi.reducer,
  [transactionMonitoringApi.reducerPath]: transactionMonitoringApi.reducer,
  [etherscanApi.reducerPath]: etherscanApi.reducer,
  [circApi.reducerPath]: circApi.reducer,
  [userApi.reducerPath]: userApi.reducer,
  [labelsApi.reducerPath]: labelsApi.reducer,
  [abiApi.reducerPath]: abiApi.reducer,
  [clientApi.reducerPath]: clientApi.reducer,
  [jobApi.reducerPath]: jobApi.reducer,
  [notificationsApi.reducerPath]: notificationsApi.reducer,
  [holdersApi.reducerPath]: holdersApi.reducer,
});

export const store = configureStore({
  reducer: persistReducer(persistConfig(), rootReducer),
  middleware: (getDefaultMiddleware) =>
    getDefaultMiddleware({
      serializableCheck: {
        ignoredActions: [FLUSH, REHYDRATE, PAUSE, PERSIST, PURGE, REGISTER],
      },
    })
      .prepend(listenerMiddleware.middleware)
      .concat(tokenApi.middleware)
      .concat(authApi.middleware)
      .concat(enrollApi.middleware)
      .concat(transactionMonitoringApi.middleware)
      .concat(etherscanApi.middleware)
      .concat(circApi.middleware)
      .concat(userApi.middleware)
      .concat(labelsApi.middleware)
      .concat(abiApi.middleware)
      .concat(clientApi.middleware)
      .concat(jobApi.middleware)
      .concat(notificationsApi.middleware)
      .concat(holdersApi.middleware),
});

export type RootState = ReturnType<typeof store.getState>;
export type AppDispatch = typeof store.dispatch;
export const persistor = persistStore(store);
