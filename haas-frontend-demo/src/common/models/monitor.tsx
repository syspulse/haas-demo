import { ReactComponent as JsIcon } from "@assets/icons/js.svg";
import { ReactComponent as ScalaIcon } from "@assets/icons/scala.svg";

import { THash } from "./general";

export interface IMonitor {
  alarm: Array<string>;
  count: number;
  id?: string;
  name: string;
  scriptId?: string;
  script?: string;
  status: MonitorStatusTypes;
  uid?: string;
  entity: MonitorScriptEntityTypes;
  bid?: string;
  abi?: string;
  contract?: string;
  scriptDesc?: string;
  scriptTyp?: string;
  scriptName?: string;
}

export interface IMonitorScript {
  id: string;
  name: string;
  src: string;
  typ: MonitorScriptTypes;
  ts0?: number;
  desc?: string;
}

export type MonitorStatusTypes = "started" | "stopped" | string;
export type MonitorScriptTypes = "js" | string;
export type MonitorScriptEntityTypes = "tx" | "block" | "token" | "event" | "function" | "mempool";
export type monitorTypesPresetTypes = {
  script: string;
  value: MonitorScriptEntityTypes;
  title: string;
};

export const monitorAlarms = [
  {
    value: "Websocket",
    key: "ws://",
  },
  { value: "Email", key: "email://" },
  { value: "Syslog", key: "syslog://" },
  { value: "Kafka ", key: "kafka://" },
  { value: "HTTP", key: "http://" },
];

export const monitorAlarmsPatterns: any = {
  "email://": {
    title: "Email Address",
    placeholder: "Email address",
    validationRules: {
      title: "Email",
      required: "Email is required.",
      pattern: {
        value: /^[A-Z0-9._%+-]+@[A-Z0-9.-]+\.[A-Z]{2,4}$/i,
        message: "Invalid email address. E.g. example@email.com",
      },
    },
    value: "email",
  },
  "syslog://": {
    title: "Syslog",
    placeholder: "syslog",
    validationRules: {
      title: "Syslog",
      required: "Syslog is required.",
      pattern: {
        value: /[a-z, A-Z, 0-9]{2,}/,
        message: "Log name should be longer then 2 charters and match pattern [a-z, A-Z, 0-9]",
      },
    },
    value: "alarm",
  },
  "kafka://": {
    title: "Kafka",
    placeholder: "Kafka",
    validationRules: {
      title: "Kafka",
      required: "Kafka is required.",
      pattern: {
        value: /[a-z, A-Z, 0-9, /]{2,}/,
        message: "URL should be longer then 2 charters and match pattern [a-z, A-Z, 0-9, /]",
      },
    },
    value: "broker/alarms",
  },
  "http://": {
    title: "HTTP",
    placeholder: "HTTP",
    validationRules: {
      title: "HTTP",
      required: "HTTP url is required.",
      pattern: {
        value: /[a-z, A-Z, 0-9, /]{2,}/,
        message: "URL should be longer then 2 charters and match pattern [a-z, A-Z, 0-9, /]",
      },
    },
    value: "service/webhook",
  },
};

export const monitorTypesPreset: THash<monitorTypesPresetTypes> = {
  tx: {
    title: "Transaction",
    value: "tx",
    script: 'if(value >= 10e18) value / 1e18 + " ETH: " + from_address + " -> " + to_address; else null',
  },
  block: {
    title: "Block",
    value: "block",
    script: '"Block: " + block_number + ": gas=" + gas_used;',
  },
  token: {
    title: "TokenTransfer",
    value: "token",
    script:
      'let t = null;\nswitch(token_address) {\n    case "0x1f9840a85d5af5bf1d1762f925bdaddc4201f984": t = "UNI"; break;\n    case "0xdac17f958d2ee523a2206206994597c13d831ec7": t = "USDT"; break;\n    case "0xb7277a6e95992041568d9391d09d0122023778a2": t = "USDC"; break;\n}\nif(t != null ) {\n  t + " : " + value / 10e9 + ": " + from_address + " -> " + to_address; \n} else null',
  },
  event: {
    title: "Event",
    value: "event",
    script: `switch (event_sig) { case 'Transfer(address,address,uint256)': if(event_param_2 > 10e16) "ERC20: "+contract+": Transfer: "+event_param_from+" -> "+event_param_to+" : "+event_param_2 break; case 'Approval(address,address,uint256)': if(event_param_2 > 10e16) "ERC20: "+contract+": Approve: "+event_param_0+" -> "+event_param_1+" : "+event_param_2 break; default: null }`,
  },
  function: {
    title: "Function",
    value: "function",
    script: `if( func_sig.startsWith('execute')) {
      "UNISWAP-3: "+contract+": "+func_param_0
    } else 
      null`,
  },
  mempool: {
    title: "Mempool",
    value: "mempool",
    script: `if( func_sig.startsWith('execute')) {
      "UNISWAP-3: "+contract+": "+func_param_0
    } else 
      null`,
  },
};

export const monitorNetworksPreset: THash<{ title: string; key: string; icon: string; explorer: string }> = {
  ethereum: {
    title: "Ethereum",
    key: "ethereum",
    icon: "//assets.coingecko.com/coins/images/279/large/ethereum.png?1595348880",
    explorer: "//etherscan.io/tx/",
  },
  zksync: {
    title: "zkSync",
    key: "zksync",
    icon: "//zksync.io/safari-pinned-tab.svg",
    explorer: "//explorer.zksync.io/tx/",
  },
  arbitrum: {
    title: "Arbitrum",
    key: "arbitrum",
    icon: "//arbitrum.io/wp-content/uploads/2021/01/cropped-Arbitrum_Symbol-Full-color-White-background-32x32.png",
    explorer: "//arbiscan.io/tx/",
  },
  optimism: {
    title: "Optimism",
    key: "optimism",
    icon: "//assets-global.website-files.com/611dbb3c82ba72fbc285d4e2/612d2f8f988b5f801bd0cf1e_favicon.png",
    explorer: "//optimistic.etherscan.io/tx/",
  },
  bsc: {
    title: "Binance Smart Chain",
    key: "bsc",
    icon: "//assets.coingecko.com/coins/images/825/small/bnb-icon2_2x.png?1644979850",
    explorer: "//bscscan.com/tx/",
  },
  polygon: {
    icon: "//assets.coingecko.com/coins/images/4713/small/matic-token-icon.png?1624446912",
    title: "Polygon",
    key: "polygon",
    explorer: "//polygonscan.com/tx/",
  },
  avalanche: {
    icon: "//assets.coingecko.com/coins/images/12559/small/Avalanche_Circle_RedWhite_Trans.png?1670992574",
    title: "Avalanche",
    key: "avalanche",
    explorer: "//avascan.info/blockchain/dfk/tx/",
  }
};

export const typIcons: any = {
  js: <JsIcon />,
  scala: <ScalaIcon />,
};
