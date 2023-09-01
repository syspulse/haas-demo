import { env } from "@app/common/config/env.dev";
import { Ijwt } from "@app/common/models/auth";
import { stateSingletTone } from "@app/index";
import axios from "axios";
import jwt_decode from "jwt-decode";

import { setAccessToken } from "../store/slice/appCommonState";

export default async function prepareHeaders(headers: Headers, { getState }: { getState: () => any }) {
  const isLogged = getState().common.enroll.phase === "LOGGED";

  if (isLogged) {
    headers.set("Authorization", `Bearer ${await stateSingletTone.getToken()}`);
  }

  return headers;
}

export class StateSingletTone {
  constructor(store: any) {
    this.sync(store);
  }

  store: any = null;

  accessToken = null;
  refreshToken = null;
  fetching = false;

  sync = (store: any) => {
    this.store = store;
    this.fetching = false;

    const unsubscribe = this.store.subscribe(() => {
      const enroll = this.store.getState().common.enroll;
      if (enroll) {
        this.store = store;
        this.accessToken = enroll.accessToken;
        this.refreshToken = enroll.refreshToken;
        unsubscribe();
      }
    });
  };

  getUserId = (): string => {
    return this.accessToken && this.store ? this.store.getState().common.enroll.uid : this.getUserId();
  };

  getToken = () => {
    if (!this.accessToken && this.store) {
      this.sync(this.store);
    }

    return new Promise(async (resolve) => {
      if (this.accessToken && !this.fetching) {
        this.fetching = true;
        if (this.isTokenValid(this.accessToken)) {
          this.fetching = false;
          resolve(this.accessToken);
        } else {         
          this.accessToken = await this.getNewToken();
          this.store && this.accessToken && this.store.dispatch(setAccessToken({ accessToken: this.accessToken }));
          this.fetching = false;

          resolve(this.accessToken);
        }
      } else {
        setTimeout(async () => resolve(await this.getToken()), 500);
      }
    });
  };

  getNewToken = async () => {
    try {
      const request = await axios({
        url: `${env.api}auth/${this.accessToken}/refresh/${this.refreshToken}`,
        method: "PUT",
        headers: {
          Authorization: `Bearer ${this.accessToken}`,
        },
      });

      return request.data.accessToken;
    } catch {
      localStorage.clear();
      window.location.reload();
    }
  };

  isTokenValid = (accessToken: string) => {
    const jwt: Ijwt = jwt_decode(accessToken);
    const exp = jwt.exp * 1000;
    const now = Date.now();

    return now < exp;
  };
}
