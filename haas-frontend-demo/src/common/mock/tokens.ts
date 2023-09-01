import { IDoughnutChart } from "../models/chart";
import { ICirculatingSupplyAnalysisMeta, ITokenMOCK } from "../models/token";

export const colors = ["#1e3d59", "#12151d", "#ff6e40", "#ffc13b", "#12151d"];
export const colors2 = ["#12151d", "#373a41", "#494d56"];
export const csCategories = ["Pools", "CEX", "Wallets", "Other smart contracts", "Burned"];
export const csCategoriesGroups = ["Destroyed", "Locked", "Purchasable", "Circulating"];

export const mockTokens: ITokenMOCK[] = new Array(100).fill({}).map(() => {
  const circulatingSupplyMax = getRandomArbitrary(10000, 10000000);

  return {
    template: "csanalysis",
    chain: "Ethereum",
    token: "Chainlink",
    alias: "LINK",
    address: `0xdac17f958d2ee523a2206206994597c13d831ec${getRandomArbitrary(1, 9).toFixed(0)}`,
    price: getRandomArbitrary(1, 10000),
    priceVector: getRandomArbitrary(-100, 100),
    marketCap: 1000000000,
    marketVector: getRandomArbitrary(-100, 100),
    circulatingSupply: circulatingSupplyMax / Number(getRandomArbitrary(1, 4).toFixed(0)),
    circulatingSupplyMax,
    holders: Number(getRandomArbitrary(0, 10000000).toFixed(0)),
    iconToken: "https://icons.iconarchive.com/icons/cjdowner/cryptocurrency-flat/256/Tether-USDT-icon.png",
    iconChain:
      "https://tl.vhv.rs/dpng/s/420-4206472_fork-cryptocurrency-ethereum-bitcoin-classic-png-download-ethereum.png",
  };
});

export const mockCirculatingSupply: IDoughnutChart<ICirculatingSupplyAnalysisMeta>[] = [
  "Investors",
  "Staking",
  "Team",
  "Hackers",
  "Fund",
].map((item, index) => {
  const _item = {
    value: getRandomArbitrary(1000, 10000),
    valueUnit: "USDT",
    label: item,
    meta: {
      vector: getRandomArbitrary(-100, 100),
    },
    colorRGB: colors[index],
  };

  return _item as IDoughnutChart<ICirculatingSupplyAnalysisMeta>;
});

export function getRandomArbitrary(min: number, max: number) {
  return Math.trunc(Math.random() * (max - min) + min);
}

export function getRandomArbitraryFloat(min: number, max: number) {
  return Math.random() * (max - min) + min;
}
