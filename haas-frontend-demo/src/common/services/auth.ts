import { env } from "@app/common/config/env.dev";
import { IEnroll } from "@app/common/models/auth";
import { authProviders, authProvidersCbType } from "@app/common/services/auth.providers";
import { setEnroll, setUser } from "@app/common/store/slice/appCommonState";
import { onQueryStartedHandler } from "@app/common/utils/onQueryStartedHandler";
import { BaseQueryFn, createApi } from "@reduxjs/toolkit/query/react";
import axios, { AxiosError, AxiosRequestConfig } from "axios";

const axiosBaseQuery =
  (
    { baseUrl }: { baseUrl: string } = { baseUrl: "" }
  ): BaseQueryFn<{
    url: string;
    method: AxiosRequestConfig["method"];
    data?: AxiosRequestConfig["data"];
  }> =>
  async ({ url, method, data }) => {
    try {
      if (!authProviders[data]) throw new Error("noProvider");
      const { path, error } = (await authProviders[data].call()) as authProvidersCbType;

      if (Boolean(error)) throw new Error(error);
      const request = await axios({
        url: baseUrl + url + path,
        method,
      });

      return { data: request.data };
    } catch (axiosError) {
      let err = axiosError as AxiosError;
      return {
        error: {
          status: err.response?.status,
          data: err.response?.data || err.message,
        },
      };
    }
  };

export const authApi = createApi({
  reducerPath: "authApi",
  baseQuery: axiosBaseQuery({
    baseUrl: env.api,
  }),
  endpoints: (builder) => ({
    auth: builder.mutation({
      query: (provider) => ({
        method: "GET",
        url: "auth",
        data: provider,
      }),
      onQueryStarted: (arg, { dispatch, queryFulfilled }) =>
        onQueryStartedHandler(dispatch, queryFulfilled, handleAuth, arg),
    }),
  }),
});

export function handleAuth({ data, dispatch, arg }: any) {
  const authData = data.data as IEnroll;

  if (!authData.uid) {
    dispatch(setEnroll({ ...authData, phase: "", id: null, provider: arg }));
  } else {
    dispatch(setUser({ ...authData, phase: "LOGGED", provider: arg }));
  }

  return;
}

export const { useAuthMutation } = authApi;
