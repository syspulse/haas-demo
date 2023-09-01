import { env } from "@app/common/config/env.dev";
import { IMonitor } from "@app/common/models/monitor";
import { onQueryStartedHandler } from "@app/common/utils/onQueryStartedHandler";
import prepareHeaders from "@app/common/utils/prepareHeaders";
import { stateSingletTone } from "@app/index";
import { createApi, fetchBaseQuery } from "@reduxjs/toolkit/query/react";

export const notificationsApi = createApi({
  reducerPath: "notificationsApi",
  baseQuery: fetchBaseQuery({
    baseUrl: env.api,
    prepareHeaders: prepareHeaders,
  }),
  tagTypes: ["FetchNotifications"],
  endpoints: (builder) => ({
    getUnreadNotifications: builder.query<any, void>({
      query: () => ({
        method: "GET",
        url: `notify/users/${stateSingletTone.getUserId()}`,
      }),
      keepUnusedDataFor: 0,
      providesTags: () => ["FetchNotifications"],
      onQueryStarted: (_, { dispatch, queryFulfilled }) => onQueryStartedHandler(dispatch, queryFulfilled),
    }),
    listenNotifications: builder.query<any, void>({
      queryFn: () => ({ data: [] }),
      keepUnusedDataFor: 0,
      async onCacheEntryAdded(url, { cacheDataLoaded, cacheEntryRemoved, updateCachedData }) {
        let pingInterval: any;

        try {
          await cacheDataLoaded;

          const path = `${env.ws}notify/user/${stateSingletTone.getUserId()}`;
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
    markAsRead: builder.mutation<any, string>({
      query: (id: string) => ({
        method: "PUT",
        url: `notify/users/${stateSingletTone.getUserId()}`,
        body: {
          id,
        },
      }),
      invalidatesTags: () => ["FetchNotifications"],
      onQueryStarted: (_, { dispatch, queryFulfilled }) => onQueryStartedHandler(dispatch, queryFulfilled),
    }),
  }),
});

export const { useListenNotificationsQuery, useGetUnreadNotificationsQuery, useMarkAsReadMutation } = notificationsApi;
