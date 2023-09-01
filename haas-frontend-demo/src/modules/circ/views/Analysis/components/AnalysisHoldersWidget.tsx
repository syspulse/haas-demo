import { getDate } from "@app/common/utils/getDate";
import getPercentage from "@app/common/utils/getPercentage";
import { Card } from "primereact/card";
import { ProgressBar } from "primereact/progressbar";
import { Tag } from "primereact/tag";

export default function AnalysisHoldersWidget({ activeHistory }: any) {
  const historyItem = activeHistory.current;
  const change = getPercentage(
    activeHistory.current.holdersTotal - activeHistory.prev.holdersTotal,
    activeHistory.current.holdersTotal,
    2
  );

  return (
    <Card className='p-card--large flex-basis--100'>
      <p className='text-20  mb-24'>Holders Statistics ({getDate(activeHistory.current.ts)})</p>
      <div className='holdersStatistic'>
        <div>
          <p className='text-14 text-w500 color--gray-700'>Total Holders</p>
          <p className='text-20 color--black-700 mb-14'>{historyItem?.holdersTotal.toLocaleString("en-US")}</p>
          <Tag
            severity={change === 0 ? "info" : change > 0 ? "success" : "danger"}
            icon={"pi pi-caret-up"}
            value={change + " %"}
          ></Tag>
        </div>
        <div className='ratio'>
          <p className='text-14 text-w500 color--gray-700'>Holders/Flippers Ratio</p>
          <p className='text-20 mb-14'>
            <span className='color--primary-500'>{historyItem?.holdersUp}</span> /{" "}
            <span className='color--red-500'>{historyItem?.holdersDown}</span>
          </p>
          <ProgressBar
            value={getPercentage(historyItem?.holdersUp, historyItem?.holdersUp + historyItem?.holdersDown)}
            style={{ height: "16px", width: "100%", background: "var(--red-500)" }}
            showValue={false}
          ></ProgressBar>
        </div>
      </div>
    </Card>
  );
}
