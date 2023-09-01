import { env } from "@app/common/config/env.dev";
import { onQueryStartedHandler } from "@app/common/utils/onQueryStartedHandler";
import { createApi, fetchBaseQuery } from "@reduxjs/toolkit/query/react";

export const etherscanApi = createApi({
  reducerPath: "etherscanApi",
  baseQuery: fetchBaseQuery({
    baseUrl: env.etherscanApi,
  }),
  endpoints: (builder) => ({
    getAbi: builder.query<any, string>({
      query: (address) => ({
        method: "GET",
        url: `?module=contract&action=getabi&apikey=6BTG2MASKXTQWUG4XGKT3263FKZYDD3CWK&address=${address}`,
      }),
      keepUnusedDataFor: 0,
      onQueryStarted: (_, { dispatch, queryFulfilled }) => onQueryStartedHandler(dispatch, queryFulfilled),
      transformResponse: (response: any) => {
        return response.result;
      },
    }),
  }),
});

export const { useGetAbiQuery } = etherscanApi;
