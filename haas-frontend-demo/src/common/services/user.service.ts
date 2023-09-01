import { env } from "@app/common/config/env.dev";
import { IUser } from "@app/common/models/auth";
import { onQueryStartedHandler } from "@app/common/utils/onQueryStartedHandler";
import { stateSingletTone } from "@app/index";
import { createApi, fetchBaseQuery } from "@reduxjs/toolkit/query/react";

import prepareHeaders from "../utils/prepareHeaders";

export const userApi = createApi({
  reducerPath: "userApi",
  baseQuery: fetchBaseQuery({
    baseUrl: env.api,
    prepareHeaders: prepareHeaders,
  }),
  endpoints: (builder) => ({
    getUser: builder.query<Partial<IUser>, string>({
      query: () => ({
        method: "GET",
        url: `/user/${stateSingletTone.getUserId()}`,
      }),
      onQueryStarted: (_, { dispatch, queryFulfilled }) => onQueryStartedHandler(dispatch, queryFulfilled),
    }),
    putUser: builder.mutation<Partial<IUser>, Partial<IUser>>({
      query: (body) => ({
        method: "PUT",
        url: `/user/${stateSingletTone.getUserId()}`,
        body,
      }),
      onQueryStarted: (arg, { dispatch, queryFulfilled }) =>
        onQueryStartedHandler(dispatch, queryFulfilled, updateUserCb, arg),
    }),
  }),
});

function updateUserCb({ dispatch, arg }: any) {
  dispatch(
    userApi.util.updateQueryData("getUser", '', (state: any) => {
      Object.keys(state).forEach((item: any) => {
        state[item] = arg[item] ? arg[item] : state[item];
      });
    })
  );
}

export const { usePutUserMutation, useGetUserQuery } = userApi;
