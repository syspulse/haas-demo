import { ComponentProps } from "@app/common/models/general";
import { IMonitor } from "@app/common/models/monitor";
import { Button } from "primereact/button";

type MonitorInProgressLoaderProps = ComponentProps<any> & {
  isEmail: boolean;
  isWS: boolean;
  alarmsCount: number;
  item: IMonitor;
};

export default function MonitorInProgressLoader({
  isEmail,
  isWS,
  alarmsCount,
  item,
  onAction,
}: MonitorInProgressLoaderProps) {
  const email = isEmail ? item.alarm[0].split("//")[1] : "";

  return (
    <>
      {isEmail && (
        <div className='monitorProgressLoader'>
          <p className='text-16 text--gray-800 mb-8'>
            <>
              {item.status !== "started" ? "Paused: " : "Active: "}
              <strong className='color--primary-600 text-w600 mx-4'>{alarmsCount}</strong> alert
              {alarmsCount > 1 ? "s" : ""} has been sent to <span className='color--blue-500'>{email}</span>
            </>
          </p>
        </div>
      )}
    </>
  );
}
