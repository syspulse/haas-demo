import AppLoadingError from "@app/common/components/AppLoadingError/AppLoadingError";
import { ComponentOnAction } from "@app/common/models/general";
import { IMonitor } from "@app/common/models/monitor";
import { useGetMonitorsQuery } from "@app/common/services/transaction-monitoring";
import { ConfirmDialog } from "primereact/confirmdialog";
import { useEffect, useState } from "react";

import MonitorEdit from "./components/MonitorEdit";
import MonitoringEmptyFiltering from "./components/MonitoringEmptyFiltering";
import MonitoringEmptyState from "./components/MonitoringEmptyState";
import MonitoringFilters from "./components/MonitoringFilters";
import Monitor from "./components/monitor/Monitor";
import "./styles.scss";

type MonitoringState = {
  isEditActive: boolean;
  activeMonitor: IMonitor | null;
  monitors: IMonitor[];
};

type MonitorFilterState = {
  filter: string[];
  sort: string;
};

const Monitoring = () => {
  const [monitoringState, setMonitoringState] = useState<MonitoringState>({
    isEditActive: false,
    activeMonitor: null,
    monitors: [],
  });
  const [monitoringFilterState, setMonitoringFilterState] = useState<MonitorFilterState>({
    filter: ["all"],
    sort: "alerts",
  });
  const { data, isFetching, isError, refetch } = useGetMonitorsQuery();

  const monitoringActions = (action: ComponentOnAction) => {
    if (action.type === "new") {
      setMonitoringState((state) => ({ ...state, isEditActive: true }));
    }

    if (action.type === "edit") {
      setMonitoringState((state) => ({ ...state, activeMonitor: { ...action.data }, isEditActive: true }));
    }

    if (action.type === "hide") {
      setMonitoringState((state) => ({ ...state, activeMonitor: { ...action.data }, isEditActive: false }));
    }

    if (action.type === "filter" || action.type === "sort") {
      setMonitoringFilterState((state: any) => {
        state[action.type] = action.data;

        return { ...state };
      });
    }
  };

  useEffect(() => {
    if (data) {
      const monitors = [...(data as IMonitor[])].reduce((acc: IMonitor[], item: IMonitor) => {
        const isNotification = item.alarm.some((alarm: string) => {
          const alarmType = alarm.includes("ws://") ? "websocket" : alarm.includes("email://") ? "email" : "";
          return monitoringFilterState.filter.includes(alarmType);
        });
        const isEntity = monitoringFilterState.filter.includes(item.entity);
        const isStatus = monitoringFilterState.filter.includes(item.status);
        const isAll = monitoringFilterState.filter.includes("all");

        (isAll || (isStatus && isEntity && isNotification)) && acc.push(item);

        return acc;
      }, []);

      setMonitoringState((state) => ({
        ...state,
        monitors: monitors.sort((a, b) => {
          if (monitoringFilterState.sort === "name") {
            return a.name.localeCompare(b.name);
          }

          if (monitoringFilterState.sort === "alerts") {
            return a.count > b.count ? -1 : 1;
          }

          return 0;
        }) as IMonitor[],
      }));
    }
  }, [data, monitoringFilterState]);

  return (
    <div className='monitoringRoot'>
      <AppLoadingError
        isError={isError}
        isFetching={isFetching}
        text='Error...'
        buttonText='Retry'
        refetch={refetch}
      />
      <h1 className='text-24 mb-16'>Monitors {data && data?.length > 0 && <>({data.length})</>}</h1>
      <MonitoringEmptyState
        isActive={data?.length === 0 && !isFetching}
        onAction={() => setMonitoringState((state) => ({ ...state, isEditActive: true }))}
      />
      {Boolean(data && data?.length > 0) && <MonitoringFilters isActive={true} onAction={monitoringActions} />}

      {Boolean(data && data?.length > 0) && (
        <div className='monitoringList'>
          {monitoringState.monitors?.map((item: IMonitor) => (
            <Monitor key={item.id} isActive={Boolean(item.id)} item={item} onAction={monitoringActions} />
          ))}

          <MonitoringEmptyFiltering
            isActive={monitoringState.monitors.length === 0}
            filter={monitoringFilterState.filter}
          />
        </div>
      )}

      {monitoringState.isEditActive && (
        <MonitorEdit
          isActive={monitoringState.isEditActive}
          onAction={monitoringActions}
          monitor={monitoringState.activeMonitor}
        />
      )}
      <ConfirmDialog />
    </div>
  );
};

export default Monitoring;
