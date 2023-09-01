import { CommonNotification, setAccessToken, setNotification } from "@app/common/store/slice/appCommonState";
import { AnyAction, ThunkDispatch } from "@reduxjs/toolkit";

export const WarningCodes = ["noWallet", "noProvider"];

export const onQueryStartedHandler = async (
  dispatch: ThunkDispatch<any, any, AnyAction>,
  queryFulfilled: Promise<any>,
  cb?: any,
  arg?: any
) => {
  try {
    const data = await queryFulfilled;

    if (cb) cb({ data, dispatch, arg });
  } catch (error: any) {
    let errorNotification: CommonNotification;

    if (error.error && error.error.data && WarningCodes.includes(error.error.data)) {
      errorNotification = new CommonNotification("toast", "warn", error, error.error.data);
    } else {
      errorNotification = new CommonNotification("toast", "error", error);
    }

    dispatch(setNotification(JSON.parse(JSON.stringify(errorNotification)))); // Ugly way to serialize Class...
  }
};
