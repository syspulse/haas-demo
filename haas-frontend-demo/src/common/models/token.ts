export interface ISearch {
  tokens: IToken[];
}

export interface IToken extends ITokenAction {
  id: string;
  symbol: string;
  name: string;
  addr: string;
  cat: string[];
  icon: string;
  key: string;
  circ?: ICircToken;
  chain: ITokenChain[];
  dcml: number;
  locks: any[];
}

export interface ITokenChain {
  addr: string;
  bid: string;
}

export interface ITokenAction {
  isSelected: boolean;
}

export interface ITokenMOCK {
  template: string; // Should be defined Enum, ex: csanalysis | etc...
  chain: string;
  token: string;
  alias: string;
  address: string;
  price: number;
  priceVector: number; // negative or positive number in %
  marketCap: number;
  marketVector: number; // negative or positive number in %
  circulatingSupply: number;
  circulatingSupplyMax: number;
  holders: number;
  iconToken: string; // link to CDN ?
  iconChain: string; // link to CDN ?
}

export interface ICirculatingSupplyAnalysisMeta {
  vector: number;
}

export interface ICircToken {
  history: ICircTokenHistory[];
  id: string;
  name: string;
  tokenId: string;
}

export interface ICircTokenHistory {
  buckets: ICircTokenBucket[];
  category: ICircCategory[];
  holders: ICircTokenHolder[];
  holdersDown: number;
  holdersTotal: number;
  holdersUp: number;
  inflation: number;
  supply: number;
  totalSupply: number;
  ts: number;
}

export interface ICircCategory {
  label: string;
  value: number;
}

export interface ICircTokenBucket {
  change: number;
  label: string;
  ratio: number;
  value: number;
}

export interface ICircTokenHolder {
  addr: string;
  lb: unknown[];
  r: number;
  v: number;
}
