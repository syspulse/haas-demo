import App from "@app/App";
// Multi language
import "@app/common/locales/locales";
import { persistor, store } from "@app/common/store";
import "@app/index.scss";
import ReactDOM from "react-dom/client";
import { Provider } from "react-redux";
import { BrowserRouter } from "react-router-dom";
import { PersistGate } from "redux-persist/integration/react";

import { StateSingletTone } from "./common/utils/prepareHeaders";

const deploys = require("@app/common/config/deploys.json");
const latestVersion = deploys && deploys.length > 0 ? deploys[deploys.length - 1] : null;
const currentVersion = window.localStorage.getItem("releaseVersion");

!currentVersion && window.localStorage.setItem("releaseVersion", latestVersion);

if (currentVersion && currentVersion !== latestVersion) {
  setTimeout(() => {
    window.localStorage.clear();
    window.location.reload();
  }, 1000);
} else {
  const root = ReactDOM.createRoot(document.getElementById("root") as HTMLElement);
  root.render(
    <Provider store={store}>
      <PersistGate loading={<>Loading...</>} persistor={persistor}>
        <BrowserRouter>
          <App />
        </BrowserRouter>
      </PersistGate>
    </Provider>
  );
}

export const stateSingletTone: any = new StateSingletTone(store);

// If you want to start measuring performance in your app, pass a function
// to log results (for example: reportWebVitals(console.log))
// or send to an analytics endpoint. Learn more: https://bit.ly/CRA-vitals
