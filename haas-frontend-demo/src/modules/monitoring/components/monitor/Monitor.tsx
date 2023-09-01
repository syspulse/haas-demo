import { ComponentOnAction, ComponentProps } from "@app/common/models/general";
import { IMonitor, monitorNetworksPreset } from "@app/common/models/monitor";
import {
  useChangeStatusMonitorMutation,
  useDeleteMonitorMutation,
  useDownloadCSVMutation,
  useGetAlarmsQuery,
  useGetMonitorHistoryQuery,
} from "@app/common/services/transaction-monitoring";
import parseStream from "@app/common/utils/parseStream";
import moment from "moment";
import { Card } from "primereact/card";
import { Chart } from "primereact/chart";
import { confirmDialog } from "primereact/confirmdialog";
import { ProgressSpinner } from "primereact/progressspinner";
import { Tag } from "primereact/tag";
import { classNames } from "primereact/utils";
import { VirtualScroller } from "primereact/virtualscroller";
import { memo, useEffect, useState } from "react";

import MonitorControls from "./MonitorControls";
import MonitorInProgressLoader from "./MonitorInProgressLoader";

type MonitorProps = ComponentProps<any> & {
  item: IMonitor;
};

const Monitor = ({ isActive, onAction, item }: MonitorProps) => {
  const chartStep = 1000;
  const isEmail = isActive ? !item.alarm[0].includes("ws:") : false;
  const isWS = isActive ? item.alarm[0].includes("ws:") : false;
  const [isSocketOpen, setIsSocketOpen] = useState(false);
  const [collectedAlarms, setCollectedAlarms] = useState<any[]>([]);
  const [alarmsCount, setAlarmsCount] = useState(0);
  const [updateStatusMonitor] = useChangeStatusMonitorMutation();
  const [deleteMonitor] = useDeleteMonitorMutation();
  const [downloadCSV] = useDownloadCSVMutation();
  const history = useGetMonitorHistoryQuery(item?.id || "", {
    skip: !Boolean(item?.id),
  });

  const alarms = useGetAlarmsQuery(item.alarm[0], {
    skip: !isSocketOpen,
  });

  const [chart, setChart] = useState<any>({
    labels: [],
    datasets: [
      {
        label: "",
        data: [],
        fill: false,
        borderColor: "#71cab5",
        tension: 0.1,
      },
    ],
  });

  useEffect(() => {
    if (history.data && !history.isFetching) {
      setCollectedAlarms([...history.data.history]);
      setAlarmsCount(history.data.count);
      setIsSocketOpen(isWS);

      if (history.isSuccess && isEmail && item.status === "started") {
        setTimeout(() => {
          history.refetch();
        }, 10000);
      }
    }
  }, [history]);

  useEffect(() => {
    if (alarms && alarms.data && alarms.data.length > 0) {
      if (!alarms.data) {
        alarms.data = [];
      }

      const latestAlarm = alarms.data[alarms.data.length - 1];
      const parsed: any[] = parseStream(latestAlarm);

      setAlarmsCount((prev: number) => (prev += parsed.length));

      setCollectedAlarms((state: any) => {
        const currentTS = parsed[0].ts;
        const latestTS = state.length > 1 ? state[state.length - 1].ts : Date.now();
        const diffTS = currentTS - latestTS;
        const range = 60 * 1000 * 10; // 10 minutes

        if (diffTS > range) {
          const index = state.findIndex((item: any) => chartStep + latestTS > item.ts);
          state.splice(index);
        }

        return [...parsed, ...state];
      });
    }
  }, [alarms]);

  useEffect(() => {
    const grouped = [...collectedAlarms].reverse().reduce((acc: any[], tx: any) => {
      if (acc.length > 0) {
        const group: any = acc[acc.length - 1];

        if (group.ts + chartStep > tx.ts) {
          group.items += 1;
        } else {
          acc.push({
            ts: tx.ts,
            items: 1,
          });
        }
      } else {
        acc.push({
          ts: tx.ts,
          items: 1,
        });
      }

      return acc;
    }, []);

    const chartColors: any = {
      tx: ["#0094ff", "#33aaff"],
      token: ["#e5bf00", "#ffd600"],
      block: ["#d5712a", "#dd8e55"],
      event: ["#0094ff", "#33aaff"],
      function: ["#e5bf00", "#ffd600"],
      mempool: ["#d5712a", "#dd8e55"],
    };
    const entity = item.entity;

    setChart((prev: any) => ({
      ...prev,
      labels: grouped.map((item) => moment(item.ts).format("H:mm:ss")),
      datasets: [
        {
          ...prev.datasets[0],
          data: grouped.map((item) => item.items),
          backgroundColor: grouped.map((item) => (item.items > 3 ? chartColors[entity][0] : chartColors[entity][0])),
        },
      ],
    }));
  }, [collectedAlarms, chartStep]);

  useEffect(() => {
    if (item.status === "started") {
      history.refetch();
    }
  }, [item.status]);

  const reject = () => {};
  const accept = () => {
    onAction({ type: "delete", data: item });
    deleteMonitor(item);
  };

  const chartOptions = {
    plugins: {
      datalabels: false,
      legend: {
        display: false,
      },
    },
    filled: false,
    animation: false,
    responsive: true,
    maintainAspectRatio: false,
    scales: {
      y: {
        grid: {
          display: false,
        },
      },
      x: {
        ticks: {
          stepSize: chartStep,
        },
        grid: {
          display: false,
        },
      },
    },
  };

  const confirmDelete = () => {
    confirmDialog({
      message: `Do you want to delete ${item.name}`,
      header: "Delete Confirmation",
      icon: "pi pi-info-circle",
      acceptClassName: "p-button-danger",
      accept,
      reject,
      style: {
        width: "350px",
      },
    });
  };

  const monitorControlsActions = (action: ComponentOnAction) => {
    if (action.type === "update") {
      updateStatusMonitor(action.data);
    }

    if (action.type === "delete") {
      confirmDelete();
    }
    if (action.type === "edit") {
      onAction({ type: "edit", data: action.data });
    }
    if (action.type === "csv") {
      downloadCSV(action.data.id as string);
    }
  };

  if (!isActive) {
    return <></>;
  }

  return (
    <Card key={item?.id} className={classNames({ monitorItemRoot: true, email: isEmail })}>
      <div className='grid'>
        <MonitorControls isActive={isActive} item={item} onAction={monitorControlsActions} />

        <p className='text-24 color--gray-700'>
          <span>Alerts: </span> <span>{alarmsCount}</span>
        </p>
      </div>

      {(collectedAlarms.length > 0 || isWS) && !isEmail && (
        <div className='grid'>
          <div className='monitorAlerts'>
            {collectedAlarms.length === 0 && (
              <div className='alertsPending'>
                <p className='text-16'>{item.status === "started" ? "Waiting for alerts..." : "Not started"}</p>
                {item.status === "started" && <ProgressSpinner />}
              </div>
            )}
            {collectedAlarms.length > 0 && (
              <VirtualScroller
                style={{ height: "340px" }}
                className='monitorAlertsScroll'
                items={collectedAlarms}
                itemSize={70}
                itemTemplate={(itemTx) => (
                  <div className='monitorAlertsScrollItem '>
                    <div className='itemKV'>
                      <span className='itemKey'>Time:</span>
                      <span className='itemValue'>{moment(itemTx.ts).format("H:mm:ss")}</span>
                    </div>
                    <div className='itemKV'>
                      <span className='itemKey'>Block:</span>
                      <Tag className='itemValue'>{itemTx?.block}</Tag>
                    </div>
                    <div className='itemKV'>
                      <span className='itemKey'>Tx:</span>
                      <span
                        className='itemValue link'
                        onClick={() => {
                          const network = item?.bid || "ethereum";

                          window.open(`${monitorNetworksPreset[network]?.explorer}${itemTx?.hash}`, "blank");
                        }}
                      >
                        {itemTx?.hash}
                      </span>
                    </div>
                    <span className='itemData'>{itemTx?.output}</span>
                  </div>
                )}
              />
            )}
          </div>
          <div className='monitorChart'>
            <Chart type='bar' data={chart} options={chartOptions} height='100%' />
          </div>
        </div>
      )}
      <MonitorInProgressLoader
        isActive={true}
        isWS={isWS}
        isEmail={isEmail}
        alarmsCount={alarmsCount}
        item={item}
        onAction={monitorControlsActions}
      />
    </Card>
  );
};

export default memo(Monitor);
