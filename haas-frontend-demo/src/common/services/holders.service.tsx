import { env } from "@app/common/config/env.dev";
import { onQueryStartedHandler } from "@app/common/utils/onQueryStartedHandler";
import { createApi, fetchBaseQuery } from "@reduxjs/toolkit/query/react";

import prepareHeaders from "../utils/prepareHeaders";

export const holdersApi = createApi({
  reducerPath: "holdersApi",
  baseQuery: fetchBaseQuery({ baseUrl: env.api + "holders", prepareHeaders: prepareHeaders }),
  tagTypes: ["invalidateHolders"],
  endpoints: (builder) => ({
    getHolders: builder.query<any, string>({
      query: (params) => ({
        method: "GET",
        url: `/${params}`,
      }),
      providesTags: ["invalidateHolders"],
      onQueryStarted: (_, { dispatch, queryFulfilled }) => onQueryStartedHandler(dispatch, queryFulfilled),
    }),
  }),
});

export const { useGetHoldersQuery } = holdersApi;
