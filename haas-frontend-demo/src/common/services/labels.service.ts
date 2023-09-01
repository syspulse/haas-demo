import { env } from "@app/common/config/env.dev";
import { onQueryStartedHandler } from "@app/common/utils/onQueryStartedHandler";
import { stateSingletTone } from "@app/index";
import { createApi, fetchBaseQuery } from "@reduxjs/toolkit/query/react";
import axios from "axios";
import { useDispatch } from "react-redux";

import { CommonNotification, setNotification } from "../store/slice/appCommonState";
import prepareHeaders from "../utils/prepareHeaders";
import { holdersApi } from "./holders.service";

export const labelsApi = createApi({
  reducerPath: "labelsApi",
  baseQuery: fetchBaseQuery({
    baseUrl: env.api,
    prepareHeaders: prepareHeaders,
  }),
  tagTypes: ["GetLabels"],
  endpoints: (builder) => ({
    getLabels: builder.query<any, string>({
      query: (query: string) => {
        return {
          method: "GET",
          url: `/tag${query}`,
        };
      },
      providesTags: () => ["GetLabels"],
      onQueryStarted: (_, { dispatch, queryFulfilled }) => onQueryStartedHandler(dispatch, queryFulfilled),
    }),
    getLabelsByAddress: builder.query<any, string[]>({
      query: (list: string[]) => {
        return {
          method: "GET",
          url: `/tag/${list.join(",")}`,
        };
      },
      providesTags: () => ["GetLabels"],
      onQueryStarted: (_, { dispatch, queryFulfilled }) => onQueryStartedHandler(dispatch, queryFulfilled),
    }),
    updateTag: builder.mutation<any, any>({
      query: (payload: any) => ({
        method: "PUT",
        url: `/tag/${payload.body.id}`,
        body: payload.body,
      }),
      invalidatesTags: () => ["GetLabels"],
      onQueryStarted: (arg, { dispatch, queryFulfilled }) =>
        onQueryStartedHandler(dispatch, queryFulfilled, updateTagCb, arg),
    }),
    createTag: builder.mutation<any, any>({
      query: (payload: any) => ({
        method: "POST",
        url: `/tag`,
        body: payload.body,
      }),
      invalidatesTags: () => ["GetLabels"],
      onQueryStarted: (arg, { dispatch, queryFulfilled }) =>
        onQueryStartedHandler(dispatch, queryFulfilled, createTagCb),
    }),
  }),
});

function updateTagCb({ data, dispatch, arg }: any) {
  dispatch(
    labelsApi.util.updateQueryData("getLabels", arg.query, (state: any) => {
      const index = state.tags.findIndex((item: any) => item.id === arg.body.id);
      state.tags[index] = data.data;
      state = { ...state, tags: state.tags };
    })
  );
  dispatch({
    type: `${holdersApi.reducerPath}/invalidateTags`,
    payload: ["invalidateHolders"],
  });
}

function createTagCb({ data, dispatch, arg }: any) {
  const successNotification: CommonNotification = new CommonNotification(
    "toast",
    "success",
    "",
    `${data.data.cat} has been created!`
  );

  dispatch(setNotification(JSON.parse(JSON.stringify(successNotification))));
  dispatch({
    type: `${holdersApi.reducerPath}/invalidateTags`,
    payload: ["invalidateHolders"],
  });
}

export const { useGetLabelsQuery, useUpdateTagMutation, useCreateTagMutation, useGetLabelsByAddressQuery } = labelsApi;

export async function bulkLabelsDelete(arr: any[]) {
  const bearer = `Bearer ${await stateSingletTone.getToken()}`;

  await Promise.all(
    arr.map((item) =>
      axios({
        url: env.api + "tag/" + item.id,
        method: "DELETE",
        headers: { Authorization: bearer },
      })
    )
  );
  return;
}
