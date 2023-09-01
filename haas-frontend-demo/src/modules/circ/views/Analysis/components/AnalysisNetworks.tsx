import { getDate } from "@app/common/utils/getDate";
import parseBigInt from "@app/common/utils/parseBigInt";
import { Card } from "primereact/card";
import { Chart } from "primereact/chart";
import { Tag } from "primereact/tag";
import { useEffect, useState } from "react";

export default function AnalysisNetworks({ activeHistory, token }: any) {
  const [networks, setNetworks] = useState<any[]>([]);

  useEffect(() => {
    setNetworks([
      {
        value: activeHistory.current.supply,
        percentage: 60,
        color: "#0784c3",
        label: "Ethereum",
        icon: "//assets.coingecko.com/coins/images/279/large/ethereum.png?1595348880",
        change: 1,
      },
      {
        value: activeHistory.current.supply,
        percentage: 10,
        color: "#2e384c",
        label: "Arbitrum",
        icon: "//arbitrum.io/wp-content/uploads/2021/01/cropped-Arbitrum_Symbol-Full-color-White-background-32x32.png",
        change: -2,
      },
      {
        value: activeHistory.current.supply,
        percentage: 5,
        color: "#4e529a",
        label: "zkSync",
        icon: "//zksync.io/safari-pinned-tab.svg",
        change: 0.1,
      },
      {
        value: activeHistory.current.supply,
        percentage: 5,
        color: "#f0b90b",
        label: "BNB Chain",
        icon: "//dex-bin.bnbstatic.com/new/static/images/favicon.ico",
        change: 5,
      },
      {
        value: activeHistory.current.supply,
        percentage: 15,
        color: "#ff0420",
        label: "Optimism",
        icon: "//assets-global.website-files.com/611dbb3c82ba72fbc285d4e2/612d2f8f988b5f801bd0cf1e_favicon.png",
        change: -4.21,
      },
      {
        value: activeHistory.current.supply,
        percentage: 5,
        color: "#803bdf",
        label: "Polygon",
        icon: "//assets-global.website-files.com/637359c81e22b715cec245ad/63f775c741010796d62770fd_polygon-favicon.png",
        change: 0.012,
      },
    ]);
  }, [activeHistory]);

  return (
    <Card>
      <p className='text-20 mb-24'>Blockchains ({getDate(activeHistory.current.ts)})</p>
      <div className='analysisNetworks'>
        <div className='doughnut'>
          <Chart
            type='doughnut'
            data={{
              datasets: [
                {
                  data: networks.map((item: any) => item.percentage),
                  backgroundColor: networks.map((item: any) => item.color),
                  borderWidth: 0,
                  hoverOffset: 0,
                  offset: 0,
                },
              ],
            }}
            options={{
              borderWidth: 1,
              hoverOffset: 5,
              offset: 0,
              layout: {
                padding: 40,
              },
              plugins: {
                tooltip: {
                  callbacks: {
                    label: "",
                  },
                  enabled: false,
                  external: () => {},
                },
                datalabels: {
                  align: "end",
                  anchor: "end",
                  rotation: 0,
                  offset: 0,
                  font: {
                    weight: "bold",
                  },
                  formatter: (item: number) => {
                    return item < 3 ? "" : item + "%";
                  },
                  color: (item: any) => item.dataset.backgroundColor[item.dataIndex],
                },
              },
              animation: {
                duration: 1,
              },
            }}
            style={{ width: "100%" }}
          />
        </div>
        <div className='legend'>
          {networks.map((item: any, index: number) => (
            <div className='legendItem' key={index}>
              <div className='title'>
                <div className='marker'>
                  <img src={item.icon} alt={item.label} />
                </div>
                <p>{item.label}</p>
              </div>
              <div className='percentage'>
                <p>{item.percentage}%</p>

                <Tag
                  severity={item.change === 0 ? "info" : item.change > 0 ? "success" : "danger"}
                  icon={"pi pi-caret-" + (item.change === 0 ? "" : item.change > 0 ? "up" : "down")}
                  value={item.change + "%"}
                ></Tag>
              </div>
              <p className='amount'>
                {token.symbol.toUpperCase()} {parseBigInt(item.value, token.dcml, "en-US")}
              </p>
            </div>
          ))}
        </div>
      </div>
    </Card>
  );
}
