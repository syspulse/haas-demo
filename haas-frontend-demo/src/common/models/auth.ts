import { authProvidersTypes } from "../services/auth.providers";

export interface Ijwt {
  exp: number;
  iat: number;
  uid: string;
}

export interface IAuth extends IAuthRefresh {
  accessToken: string | null;
}

export interface IAuthRefresh {
  refreshToken: string | null;
}

export interface IUser {
  email: string;
  id: string;
  name: string;
  ethereumAddress: string;
  avatar: string;
}

export interface IEnroll {
  avatar: string;
  email: string;
  idToken: string;
  locale: string;
  name: string;
  xid: string;
  accessToken: string;
  phase: EnrollPhaseTypes;
  id: string | null;
  uid: string;
  code?: string;
  provider?: authProvidersTypes;
  refreshToken?: string;
}

type EnrollPhaseTypes = "EMAIL_ACK" | "START_ACK" | "CONFIRM_EMAIL_ACK" | "LOGGED" | string;
