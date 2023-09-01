import { env } from "@app/common/config/env.dev";
import { onQueryStartedHandler } from "@app/common/utils/onQueryStartedHandler";
import { createApi, fetchBaseQuery } from "@reduxjs/toolkit/query/react";

import prepareHeaders from "../utils/prepareHeaders";

export const clientApi = createApi({
  reducerPath: "clientApi",
  tagTypes: ["GetClients"],
  baseQuery: fetchBaseQuery({
    baseUrl: env.api,
    prepareHeaders: prepareHeaders,
  }),
  endpoints: (builder) => ({
    getClients: builder.query<any, void>({
      query: () => ({
        method: "GET",
        url: `auth/cred`,
      }),
      onQueryStarted: (_, { dispatch, queryFulfilled }) => onQueryStartedHandler(dispatch, queryFulfilled),
      providesTags: () => ["GetClients"],
      transformResponse: (response: any) => response.creds,
    }),
    deleteClient: builder.mutation<any, string>({
      query: (cid) => ({
        method: "DELETE",
        url: `auth/cred/${cid}`,
      }),
      onQueryStarted: (_, { dispatch, queryFulfilled }) => onQueryStartedHandler(dispatch, queryFulfilled),
      invalidatesTags: () => ["GetClients"],
    }),
    createClient: builder.mutation<any, any>({
      query: (body: any) => ({
        method: "POST",
        url: `auth/cred`,
        body,
      }),
      onQueryStarted: (_, { dispatch, queryFulfilled }) => onQueryStartedHandler(dispatch, queryFulfilled),
      invalidatesTags: () => ["GetClients"],
    }),
  }),
});

export const { useCreateClientMutation, useGetClientsQuery, useDeleteClientMutation } = clientApi;
