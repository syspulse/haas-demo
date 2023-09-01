import { createListenerMiddleware } from "@reduxjs/toolkit";

import { logOut } from "./slice/appCommonState";

const listenerMiddleware = createListenerMiddleware();

listenerMiddleware.startListening({
  actionCreator: logOut,
  effect: () => {
    localStorage.clear();
    window.location.reload();
  },
});

export default listenerMiddleware;
