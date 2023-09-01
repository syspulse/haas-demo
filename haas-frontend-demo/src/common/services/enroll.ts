import { env } from "@app/common/config/env.dev";
import { onQueryStartedHandler } from "@app/common/utils/onQueryStartedHandler";
import prepareHeaders from "@app/common/utils/prepareHeaders";
import { createApi, fetchBaseQuery } from "@reduxjs/toolkit/query/react";

import { IEnroll } from "../models/auth";
import { setEnroll } from "../store/slice/appCommonState";

export const enrollApi = createApi({
  reducerPath: "enrollApi",
  baseQuery: fetchBaseQuery({
    baseUrl: env.api,
    prepareHeaders: prepareHeaders,
  }),
  endpoints: (builder) => ({
    createUser: builder.mutation({
      query: (payload: Partial<IEnroll>) => ({
        method: "POST",
        url: "enroll",
        body: payload,
      }),
      onQueryStarted: (_, { dispatch, queryFulfilled }) =>
        onQueryStartedHandler(dispatch, queryFulfilled, createUserCb),
    }),
    getEmailCode: builder.mutation({
      query: (payload: Partial<IEnroll>) => ({
        method: "POST",
        url: `enroll/${payload.id}/email`,
        body: { id: payload.id, data: { email: payload.email, name: payload.name } },
      }),
      onQueryStarted: (_, { dispatch, queryFulfilled }) =>
        onQueryStartedHandler(dispatch, queryFulfilled, createUserCb),
    }),
    verifyEmailCode: builder.mutation({
      query: (payload: Partial<IEnroll>) => ({
        method: "POST",
        url: `enroll/${payload.id}/confirm`,
        body: { id: payload.id, data: { code: payload.code } },
      }),
      onQueryStarted: (_, { dispatch, queryFulfilled }) =>
        onQueryStartedHandler(dispatch, queryFulfilled, createUserCb),
    }),
  }),
});

export const { useCreateUserMutation, useGetEmailCodeMutation, useVerifyEmailCodeMutation } = enrollApi;

export function createUserCb({ data, dispatch }: any) {
  const authData = data.data as Partial<IEnroll>;

  dispatch(setEnroll({ ...authData }));

  return;
}
