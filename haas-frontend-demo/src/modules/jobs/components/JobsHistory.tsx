import Clipboard from "@app/common/utils/Clipboard";
import moment from "moment";
import { Column } from "primereact/column";
import { DataTable } from "primereact/datatable";
import { classNames } from "primereact/utils";
import { memo, useState } from "react";
import { useNavigate } from "react-router-dom";

const JobsHistory = ({ data }: any) => {
  const navigate = useNavigate();
  return (
    <>
      <h2 className='text-20 mb-14'>History</h2>

      <DataTable
        editMode='row'
        dataKey='id'
        className='jobsTable'
        value={[...data].sort((a: any, b: any) => b.ts0 - a.ts0)}
        responsiveLayout='scroll'
        autoLayout
        scrollHeight='calc(100vh - 450px)'
        paginator
        rows={10}
        onRowClick={(event: any) => navigate(event.data.id)}
      >
        <Column header='XID' body={(job) => <>{job.xid}</>} />
        <Column
          header='ID'
          body={(job) => (
            <>
              <Clipboard text={job.id} maxLength={5} />
            </>
          )}
        />
        <Column header='Name' body={(job) => <>{job.name}</>} />
        <Column
          header='Result'
          body={(job) => (
            <span
              className={classNames({
                jobStatus: true,
                done: job.result === "ok",
                fail: job.result === "error",
              })}
            >
              {job.result ? job.result : <i className='pi pi-spin pi-spinner' />}
            </span>
          )}
        />
        <Column
          header='State'
          body={(job) => (
            <span className={classNames({ jobState: true, finished: job.state === "finished" })}>{job.state}</span>
          )}
        />
        <Column header='Created' body={(job) => <>{moment(job.ts0).format("YYYY MMM DD HH:mm:ss")}</>} />
        <Column
          header='Started'
          body={(job) => (
            <>
              {job.tsStart ? (
                moment(job.tsStart).format("YYYY MMM DD HH:mm:ss")
              ) : (
                <i className='pi pi-spin pi-spinner' />
              )}
            </>
          )}
        />
        <Column
          header='Complete'
          body={(job) => (
            <>
              {job.tsEnd ? moment(job.tsEnd).format("YYYY MMM DD HH:mm:ss") : <i className='pi pi-spin pi-spinner' />}
            </>
          )}
        />
      </DataTable>
    </>
  );
};

export default memo(JobsHistory);
