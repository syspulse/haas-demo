import { env } from "@app/common/config/env.dev";
import { IMonitor, IMonitorScript } from "@app/common/models/monitor";
import { onQueryStartedHandler } from "@app/common/utils/onQueryStartedHandler";
import prepareHeaders from "@app/common/utils/prepareHeaders";
import { stateSingletTone } from "@app/index";
import { createApi, fetchBaseQuery } from "@reduxjs/toolkit/query/react";
import { abiApi } from "./abi.service";

export const transactionMonitoringApi = createApi({
  reducerPath: "transactionMonitoringApi",
  tagTypes: ["invalidateScript"],
  baseQuery: fetchBaseQuery({
    baseUrl: env.api,
    prepareHeaders: prepareHeaders,
  }),
  endpoints: (builder) => ({
    getMonitors: builder.query<IMonitor[], void>({
      query: () => ({
        method: "GET",
        url: `intercept/user/${stateSingletTone.getUserId()}`,
      }),
      keepUnusedDataFor: 0,
      onQueryStarted: (_, { dispatch, queryFulfilled }) => onQueryStartedHandler(dispatch, queryFulfilled),
      transformResponse: (response: any) => response.interceptions,
    }),
    getMonitorHistory: builder.query<any, string>({
      query: (id) => ({
        method: "GET",
        url: `intercept/${id}`,
      }),
      keepUnusedDataFor: 0,
      onQueryStarted: (_, { dispatch, queryFulfilled }) => onQueryStartedHandler(dispatch, queryFulfilled),
      transformResponse: (response: any) => response,
    }),
    getMonitorScript: builder.query<IMonitorScript, string>({
      query: (id) => ({
        method: "GET",
        url: `intercept/script/${id}`,
      }),
      providesTags: ["invalidateScript"],
      onQueryStarted: (_, { dispatch, queryFulfilled }) => onQueryStartedHandler(dispatch, queryFulfilled),
    }),
    getMonitorABI: builder.query<IMonitorScript, any>({
      query: (query) => ({
        method: "GET",
        url: `intercept/${query.id}/abi/${query.aid}`,
      }),
      onQueryStarted: (_, { dispatch, queryFulfilled }) => onQueryStartedHandler(dispatch, queryFulfilled),
    }),
    getScripts: builder.query<{ scripts: IMonitorScript[] }, string>({
      query: () => ({
        method: "GET",
        url: `intercept/script`,
      }),
      onQueryStarted: (_, { dispatch, queryFulfilled }) => onQueryStartedHandler(dispatch, queryFulfilled),
    }),
    updateScript: builder.mutation<any, any>({
      query: (payload) => ({
        method: "PUT",
        url: `intercept/script/${payload.id}`,
        body: payload.body,
      }),
      onQueryStarted: (arg, { dispatch, queryFulfilled }) =>
        onQueryStartedHandler(dispatch, queryFulfilled, updateScriptCb, arg),
    }),
    deleteScript: builder.mutation<any, string>({
      query: (id) => ({
        method: "DELETE",
        url: `intercept/script/${id}`,
      }),
      onQueryStarted: (arg, { dispatch, queryFulfilled }) =>
        onQueryStartedHandler(dispatch, queryFulfilled, deleteScriptCb, arg),
    }),
    getAlarms: builder.query<string[], string>({
      queryFn: () => ({ data: [] }),
      keepUnusedDataFor: 0,
      async onCacheEntryAdded(url, { cacheDataLoaded, cacheEntryRemoved, updateCachedData }) {
        let pingInterval: any;

        try {
          await cacheDataLoaded;
          const path = env.interceptWS + url.split("ws://")[1];
          const socket = new WebSocket(path);

          socket.addEventListener("message", (message) => {
            updateCachedData((state) => {
              state.push(message.data);
            });
          });

          socket.addEventListener("open", () => {
            pingInterval = setInterval(() => {
              if (socket.readyState === socket.OPEN) {
                socket.send(Date.now().toString());
              } else {
                clearInterval(pingInterval);
              }
            }, 10000);
          });

          await cacheEntryRemoved;
          clearInterval(pingInterval);
          socket.close();
        } catch {
          clearInterval(pingInterval);
        }
      },
    }),
    createMonitor: builder.mutation<Partial<IMonitor>, IMonitor>({
      query: (payload) => {
        return {
          method: "POST",
          url: "intercept",
          body: payload,
        };
      },
      onQueryStarted: (arg, { dispatch, queryFulfilled }) =>
        onQueryStartedHandler(dispatch, queryFulfilled, createMonitorCb, arg),
    }),
    updateMonitor: builder.mutation<IMonitor, Partial<IMonitor>>({
      query: (payload) => {
        return {
          method: "PUT",
          url: `intercept/${payload.id}`,
          body: payload,
        };
      },
      invalidatesTags: ["invalidateScript"],
      onQueryStarted: (arg, { dispatch, queryFulfilled }) =>
        onQueryStartedHandler(dispatch, queryFulfilled, updateMonitorCb, arg),
    }),
    changeStatusMonitor: builder.mutation<Partial<IMonitor>, IMonitor>({
      query: (payload) => {
        return {
          method: "POST",
          url: `intercept/${payload.id}`,
          body: { id: payload.id, command: payload.status === "stopped" ? "stop" : "start" },
        };
      },
      onQueryStarted: (arg, { dispatch, queryFulfilled }) =>
        onQueryStartedHandler(dispatch, queryFulfilled, updateMonitorCb, arg),
    }),
    deleteMonitor: builder.mutation<void, any>({
      query: (payload) => {
        return {
          method: "DELETE",
          url: `intercept/${payload.id}`,
        };
      },
      onQueryStarted: (arg, { dispatch, queryFulfilled }) =>
        onQueryStartedHandler(dispatch, queryFulfilled, deleteMonitorCb, arg),
    }),
    downloadCSV: builder.mutation<void, string>({
      query: (id) => ({
        method: "GET",
        url: `intercept/${id}/history`,
        responseHandler: (response) => response.text(),
      }),
      onQueryStarted: (arg, { dispatch, queryFulfilled }) =>
        onQueryStartedHandler(dispatch, queryFulfilled, downloadCSVcb, arg),
    }),
  }),
});

function deleteMonitorCb({ dispatch, arg }: any) {
  dispatch(
    transactionMonitoringApi.util.updateQueryData("getMonitors", undefined, (state: any) => {
      Object.values(state).forEach((item: any, index) => {
        if (item.id === arg.id) {
          state.splice(index, 1);
        }
      });
    })
  );
}

function updateMonitorCb({ dispatch, arg }: any) {
  dispatch(
    transactionMonitoringApi.util.updateQueryData("getMonitors", undefined, (state: any) => {
      Object.values(state).forEach((item: any, index) => {
        if (item.id === arg.id) {
          state[index] = { ...state[index], ...arg };
        }
      });
    })
  );
  dispatch({
    type: `${abiApi.reducerPath}/invalidateTags`,
    payload: ["invalidateAbiAll"],
  });
}

function downloadCSVcb({ data, dispatch, arg }: any) {
  const file = new File([data.data], arg, { type: "text/csv;charset=utf-8;" });
  const exportUrl = URL.createObjectURL(file);
  const fileLink = document.createElement("a");
  fileLink.href = exportUrl;
  fileLink.download = `history_intercept-id_${arg}`;
  fileLink.click();
  fileLink.remove();
}

function createMonitorCb({ data, dispatch, arg }: any) {
  dispatch(
    transactionMonitoringApi.util.updateQueryData("getMonitors", undefined, (state: any) => {
      state.push({ ...data.data, status: "started" });
    })
  );
}

function updateScriptCb({ dispatch, arg }: any) {
  dispatch(
    transactionMonitoringApi.util.updateQueryData("getScripts", '', (state: any) => {
      state.scripts = [...state.scripts].map((script: any) => {
        if (script.id === arg.id) {
          return { ...script, ...arg.body };
        }

        return script;
      });
    })
  );
}

function deleteScriptCb({ dispatch, arg }: any) {
  dispatch(
    transactionMonitoringApi.util.updateQueryData("getScripts", '', (state: any) => {
      state.scripts = [...state.scripts].filter((script: any) => script.id !== arg);
    })
  );
}

export const {
  useGetMonitorsQuery,
  useCreateMonitorMutation,
  useUpdateMonitorMutation,
  useDeleteMonitorMutation,
  useChangeStatusMonitorMutation,
  useGetMonitorScriptQuery,
  useGetAlarmsQuery,
  useGetMonitorHistoryQuery,
  useDownloadCSVMutation,
  useGetScriptsQuery,
  useGetMonitorABIQuery,
  useUpdateScriptMutation,
  useDeleteScriptMutation,
} = transactionMonitoringApi;
