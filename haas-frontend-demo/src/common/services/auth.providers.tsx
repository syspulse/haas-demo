import { env } from "@app/common/config/env.dev";
import { ReactComponent as GoogleLogo } from "@assets/icons/googleLogo.svg";
import { ReactComponent as MetamaskLogo } from "@assets/icons/metamaskLogo.svg";
import { ReactComponent as TwitterLogo } from "@assets/icons/twitterLogo.svg";
import { BaseQueryFn, MutationDefinition } from "@reduxjs/toolkit/dist/query";
import { MutationTrigger } from "@reduxjs/toolkit/dist/query/react/buildHooks";
import { ethers } from "ethers";
import { gapi } from "gapi-script";
import { ReactNode } from "react";

declare global {
  interface Window {
    ethereum: any;
    gapi: any;
    metamask: string;
  }
}

export const providersLogos: authProvidersType<ReactNode> = {
  metamask: <MetamaskLogo />,
  google: <GoogleLogo />,
  twitter: <TwitterLogo />,
};

export const authProviders: authProvidersType<any> = {
  metamask: async () => {
    try {
      if (!window.ethereum) throw new Error("noWallet");

      await window.ethereum.send("eth_requestAccounts");
      const provider = new ethers.providers.Web3Provider(window.ethereum);
      const signer = provider.getSigner();
      const address = await signer.getAddress();
      const msg = `timestamp: ${Date.now()},address: ${address}`;
      const sig = await signer.signMessage(msg);

      return {
        path: `/eth/auth?client_id=eaf9642f76195dca7529c0589e6d6259&msg=${btoa(
          msg
        )}&sig=${sig}&addr=${address}&redirect_uri=${env.api}auth/eth/callback`,
        error: null,
      };
    } catch (error: unknown) {
      let _error = "";

      if (error instanceof Error) {
        _error = error.message as string;
      } else {
        _error = String(error);
      }

      return {
        path: "",
        error: _error,
      };
    }
  },
  google: async () => {
    try {
      const code = await new Promise((resolve, reject) => {
        const clientId = "1084039747276-3na59kcrc8ab5k65louvg5jv8ulnmv54.apps.googleusercontent.com";
        const initClient = () => {
          gapi.auth2
            .init({
              clientId: clientId,
              scope: "openid",
            })
            .then((GoogleAuth: any) => {
              GoogleAuth.grantOfflineAccess().then((result: { code: string }, error: any) => {
                result && result.code && resolve(result.code);

                error && reject(error);
              });
            });
        };

        gapi.load("client:auth2", initClient);
      });

      return {
        path: `/token/google?code=${code}&redirect_uri=${window.location.origin}`,
        error: null,
      };
    } catch (error: unknown) {
      let _error = "";

      if (error instanceof Error) {
        _error = error.message as string;
      } else {
        _error = String(error);
      }

      return {
        path: "",
        error: _error,
      };
    }
  },
  twitter: async () => {
    try {
      const clientId = "VExtd2FtdGlBYU5NbWdIemNjWFM6MTpjaQ";
      const redirectUri = `${window.location.origin}/twitter`;
      const scope = "users.read tweet.read";

      const challenge = "challenge";
      const loginUrl = `https://twitter.com/i/oauth2/authorize?response_type=code&client_id=${clientId}&redirect_uri=${redirectUri}&scope=${scope}&state=state&code_challenge=${challenge}&code_challenge_method=plain`;

      const code = await new Promise((resolve) => {
        window.open(loginUrl);

        const interval = setInterval(() => {
          const twitterCode = localStorage.getItem("twitterCode");
          if (twitterCode) {
            localStorage.removeItem("twitterCode");
            clearInterval(interval);

            resolve(twitterCode);
          }
        }, 1000);
      });

      return {
        path: `/token/twitter?code=${code}&redirect_uri=${redirectUri}&challenge=${challenge}`,
        error: null,
      };
    } catch (error: unknown) {
      let _error = "";

      if (error instanceof Error) {
        _error = error.message as string;
      } else {
        _error = String(error);
      }

      return {
        path: "",
        error: _error,
      };
    }
  },
};

export const getAuthProviders = (auth: getAuthProvidersParamType) => {
  return Object.keys(authProviders).reduce((acc: authProviderItem[], item: authProvidersTypes) => {
    acc.push(new authProviderItem(item, auth));
    return acc;
  }, []);
};

export type authProvidersTypes = "metamask" | "google" | "twitter" | string;
export type authProvidersType<T> = { [key: authProvidersTypes]: Promise<authProvidersCbType> | T };
export type authProvidersCbType = { path: string; error: string };
export type getAuthProvidersParamType = MutationTrigger<
  MutationDefinition<
    any,
    BaseQueryFn<
      {
        url: string;
        method: string | undefined;
        data?: any;
      },
      unknown,
      unknown,
      {},
      {}
    >,
    never,
    any,
    "authApi"
  >
>;

export class authProviderItem {
  constructor(public key: authProvidersTypes, public action: (key: string) => void, public logo?: ReactNode) {
    this.logo = providersLogos[key] as ReactNode;
  }
}
