import { env } from "@app/common/config/env.dev";
import { IToken } from "@app/common/models/token";
import { onQueryStartedHandler } from "@app/common/utils/onQueryStartedHandler";
import { createApi, fetchBaseQuery } from "@reduxjs/toolkit/query/react";

import prepareHeaders from "../utils/prepareHeaders";

export const jobApi = createApi({
  reducerPath: "jobApi",
  tagTypes: ["FetchJobs"],
  baseQuery: fetchBaseQuery({ baseUrl: env.api + "job", prepareHeaders: prepareHeaders }),
  endpoints: (builder) => ({
    getJobs: builder.query<any, void>({
      query: () => ({
        method: "GET",
        url: "",
      }),
      providesTags: () => ["FetchJobs"],
      onQueryStarted: (_, { dispatch, queryFulfilled }) => onQueryStartedHandler(dispatch, queryFulfilled),
    }),
    createJob: builder.mutation<any, any>({
      query: (payload) => ({
        method: "POST",
        url: "",
        body: payload,
      }),
      invalidatesTags: () => ["FetchJobs"],
      onQueryStarted: (_, { dispatch, queryFulfilled }) => onQueryStartedHandler(dispatch, queryFulfilled),
    }),
  }),
});

export const { useCreateJobMutation, useGetJobsQuery } = jobApi;
