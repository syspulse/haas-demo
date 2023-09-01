import { env } from "@app/common/config/env.dev";
import { stateSingletTone } from "@app/index";
import { createApi, fetchBaseQuery } from "@reduxjs/toolkit/query/react";
import axios from "axios";

import { THash } from "../models/general";
import { onQueryStartedHandler } from "../utils/onQueryStartedHandler";
import prepareHeaders from "../utils/prepareHeaders";

export const circApi = createApi({
  reducerPath: "circApi",
  baseQuery: fetchBaseQuery({ baseUrl: env.api + "circ/", prepareHeaders: prepareHeaders }),
  endpoints: (builder) => ({
    getCircIdByTokenId: builder.query<any, any>({
      keepUnusedDataFor: 0,
      query: (props) => ({
        method: "GET",
        url: `token/${props.id}?ts0=${props.ts0}&ts1=${props.ts1}`,
      }),
      onQueryStarted: (_, { dispatch, queryFulfilled }) => onQueryStartedHandler(dispatch, queryFulfilled),
    }),
    getCircById: builder.query<any, string>({
      keepUnusedDataFor: 0,
      query: (tokenId) => ({
        method: "GET",
        url: `${tokenId}`,
      }),
      onQueryStarted: (_, { dispatch, queryFulfilled }) => onQueryStartedHandler(dispatch, queryFulfilled),
    }),
    getCircLast: builder.query<any, void>({
      keepUnusedDataFor: 0,
      query: () => ({
        method: "GET",
        url: `last`,
      }),
      onQueryStarted: (_, { dispatch, queryFulfilled }) => onQueryStartedHandler(dispatch, queryFulfilled),
      transformResponse: (response: any) => {
        return response.circulations.reduce((acc: THash<any>, item: any) => {
          acc[item.tokenId] = item;
          return acc;
        }, {});
      },
    }),
  }),
});

export const { useGetCircByIdQuery, useGetCircIdByTokenIdQuery, useGetCircLastQuery } = circApi;

export async function getBulkTokens(arr: string[]) {
  const tokenId: string[] = [];
  const tokenAddr: string[] = [];

  arr.forEach((item) => {
    item.includes("0x") ? tokenAddr.push(item) : tokenId.push(item);
  });

  const bearer = `Bearer ${await stateSingletTone.getToken()}`;
  const tokensByAddrResponse = await axios({
    url: env.api + "token/address/" + tokenAddr.join(","),
    method: "GET",
    headers: { Authorization: bearer },
  });
  const tokensByAddr = tokensByAddrResponse.data.tokens;
  const tokensByIdResponse = await axios({
    url: env.api + "token/" + tokenId.join(","),
    method: "GET",
    headers: { Authorization: bearer },
  });
  const tokensById = tokensByIdResponse.data.tokens;

  return [...tokensByAddr, ...tokensById];
}
