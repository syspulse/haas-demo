import { env } from "@app/common/config/env.dev";
import { onQueryStartedHandler } from "@app/common/utils/onQueryStartedHandler";
import { createApi, fetchBaseQuery } from "@reduxjs/toolkit/query/react";

import prepareHeaders from "../utils/prepareHeaders";

export const abiApi = createApi({
  reducerPath: "abiApi",
  baseQuery: fetchBaseQuery({
    baseUrl: env.api,
    prepareHeaders: prepareHeaders,
  }),
  tagTypes: ["invalidateAbiAll"],
  endpoints: (builder) => ({
    getAbi: builder.query<any, any>({
      query: (query: any) => ({
        method: "GET",
        url: `/abi?entity=${query.entity}&from=${query.index}&size=${query.size}`,
      }),
      providesTags: ["invalidateAbiAll"],
      onQueryStarted: (_, { dispatch, queryFulfilled }) => onQueryStartedHandler(dispatch, queryFulfilled),
    }),
    getAbiById: builder.query<any, any>({
      query: (query: any) => ({
        method: "GET",
        url: `/abi/${query.aid}?entity=${query.entity}`,
      }),
      providesTags: ["invalidateAbiAll"],
      onQueryStarted: (_, { dispatch, queryFulfilled }) => onQueryStartedHandler(dispatch, queryFulfilled),
    }),
    getAbiSearch: builder.query<any, any>({
      query: (query: any) => ({
        method: "GET",
        url: `/abi/search/${query.txt}?entity=${query.entity}&from=${query.index}&size=${query.size}`,
      }),
      onQueryStarted: (_, { dispatch, queryFulfilled }) => onQueryStartedHandler(dispatch, queryFulfilled),
    }),
  }),
});

export const { useGetAbiQuery, useGetAbiSearchQuery, useGetAbiByIdQuery } = abiApi;
