import { ComponentProps } from "@app/common/models/general";
import { IMonitor, monitorAlarmsPatterns, monitorNetworksPreset, monitorTypesPreset } from "@app/common/models/monitor";
import { classNames } from "primereact/utils";

type MonitorControlsProps = ComponentProps<any> & {
  item: IMonitor;
};

export default function MonitorControls({ isActive, onAction, item }: MonitorControlsProps) {
  return (
    <div className='flex-1'>
      <p className='text-20'>
        <span className={classNames({ "text--gray-600": item.status === "stopped" })}>{item.name}</span>
        <span className='monitorControls'>
          {item.status === "started" && (
            <i
              className='pi pi-pause color--yellow-500 tooltip'
              data-pr-tooltip='Stop'
              data-pr-position='top'
              onClick={() => onAction({ type: "update", data: { ...item, status: "stopped" } })}
            ></i>
          )}
          {item.status === "stopped" && (
            <i
              className='pi pi-play color--primary-500 tooltip'
              data-pr-tooltip='Start'
              data-pr-position='top'
              onClick={() => onAction({ type: "update", data: { ...item, status: "started" } })}
            ></i>
          )}
          <i
            className='pi pi-cog tooltip'
            data-pr-tooltip='Details'
            data-pr-position='top'
            onClick={() => onAction({ type: "edit", data: item })}
          ></i>
          <i
            className='pi pi-download tooltip'
            data-pr-tooltip='Download CSV'
            data-pr-position='top'
            onClick={() => onAction({ type: "csv", data: item })}
          ></i>
          <i
            className='pi pi-trash color--red-300 tooltip'
            data-pr-tooltip='Delete'
            data-pr-position='top'
            onClick={() => onAction({ type: "delete" })}
          ></i>
        </span>
      </p>
      <p className='text--gray-800 monitorControlsDetails'>
        <span className='networkName'>
          <span className='text--gray-600'>Network:</span>
          <img
            src={item?.bid ? monitorNetworksPreset[item.bid].icon : Object.values(monitorNetworksPreset)[0].icon}
            alt={item?.bid ? monitorNetworksPreset[item.bid].title : Object.values(monitorNetworksPreset)[0].title}
          />
          <span className='text--gray-800'>
            {item?.bid ? monitorNetworksPreset[item.bid].title : Object.values(monitorNetworksPreset)[0].title}
          </span>
        </span>
        <span className='text--gray-600'>
          <span>Entity: </span>
          <span className='text--gray-800'>{item?.entity && monitorTypesPreset[item?.entity].title} </span>
        </span>

        <span className='text--gray-800'>
          <span className='text--gray-600'>Notify in: </span>
          {item?.alarm.map((item: string, index) => (
            <span key={index}>
              {item.includes("ws://") ? "Websocket" : monitorAlarmsPatterns[item.split("://")[0] + "://"].title}
            </span>
          ))}
        </span>
      </p>
    </div>
  );
}
