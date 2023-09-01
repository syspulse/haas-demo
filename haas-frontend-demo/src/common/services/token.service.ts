import { env } from "@app/common/config/env.dev";
import { ISearch, IToken } from "@app/common/models/token";
import { onQueryStartedHandler } from "@app/common/utils/onQueryStartedHandler";
import { createApi, fetchBaseQuery } from "@reduxjs/toolkit/query/react";

import { tokenCategoriesHash } from "../config/tokekCategories";
import prepareHeaders from "../utils/prepareHeaders";

export const tokenApi = createApi({
  reducerPath: "tokenApi",
  tagTypes: ["GetTokens"],
  baseQuery: fetchBaseQuery({ baseUrl: env.api + "token", prepareHeaders: prepareHeaders }),
  endpoints: (builder) => ({
    getTokenAll: builder.query<any, string>({
      query: (getParams) => (getParams ? `${getParams}` : ""),
      providesTags: () => ["GetTokens"],
      onQueryStarted: (_, { dispatch, queryFulfilled }) => onQueryStartedHandler(dispatch, queryFulfilled),
    }),
    getTokenAllCount: builder.query<number, void>({
      query: () => "?from=0&size=0",
      providesTags: () => ["GetTokens"],
      onQueryStarted: (_, { dispatch, queryFulfilled }) => onQueryStartedHandler(dispatch, queryFulfilled),
      transformResponse: (response: any) => response.total,
    }),
    getTokenSearch: builder.query<IToken[], string>({
      query: (keyword) => `/typing/${encodeURIComponent(keyword)}`,
      providesTags: () => ["GetTokens"],
      onQueryStarted: (_, { dispatch, queryFulfilled }) => onQueryStartedHandler(dispatch, queryFulfilled),
      transformResponse: (response: ISearch, _, arg) => response.tokens,
    }),
    getTokenByName: builder.query<IToken, string>({
      query: (name) => `/${encodeURIComponent(name)}`,
      providesTags: () => ["GetTokens"],
      onQueryStarted: (_, { dispatch, queryFulfilled }) => onQueryStartedHandler(dispatch, queryFulfilled),
    }),
    getTokenByAddress: builder.query<IToken, string>({
      query: (address) => `/address/${encodeURIComponent(address)}`,
      providesTags: () => ["GetTokens"],
      onQueryStarted: (_, { dispatch, queryFulfilled }) => onQueryStartedHandler(dispatch, queryFulfilled),
      transformResponse: (response: any) => {
        return response.tokens[0];
      },
    }),
    createToken: builder.mutation<IToken, any>({
      query: (payload) => ({
        method: "POST",
        url: "",
        body: payload,
      }),
      invalidatesTags: () => ["GetTokens"],
      onQueryStarted: (_, { dispatch, queryFulfilled }) => onQueryStartedHandler(dispatch, queryFulfilled),
    }),
    updateToken: builder.mutation<IToken, any>({
      query: (payload) => ({
        method: "PUT",
        url: `/${payload.id}`,
        body: payload,
      }),
      invalidatesTags: () => ["GetTokens"],
      onQueryStarted: (_, { dispatch, queryFulfilled }) => onQueryStartedHandler(dispatch, queryFulfilled),
    }),
    deleteToken: builder.mutation<IToken, any>({
      query: (id) => ({
        method: "DELETE",
        url: `/${id}`,
      }),
      invalidatesTags: () => ["GetTokens"],
      onQueryStarted: (_, { dispatch, queryFulfilled }) => onQueryStartedHandler(dispatch, queryFulfilled),
    }),
  }),
});

export const {
  useGetTokenSearchQuery,
  useGetTokenAllQuery,
  useGetTokenAllCountQuery,
  useGetTokenByNameQuery,
  useGetTokenByAddressQuery,
  useCreateTokenMutation,
  useUpdateTokenMutation,
  useDeleteTokenMutation,
} = tokenApi;
