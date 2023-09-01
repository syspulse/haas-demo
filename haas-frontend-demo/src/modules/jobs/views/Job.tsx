import AppLoadingError from "@app/common/components/AppLoadingError/AppLoadingError";
import { useGetJobsQuery } from "@app/common/services/job.service";
import parseJson from "@app/common/utils/parseJson";
import ReactCodeMirror from "@uiw/react-codemirror";
import moment from "moment";
import { classNames } from "primereact/utils";
import { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import { json } from "@codemirror/lang-json";
import { markdown } from "@codemirror/lang-markdown";
import { Card } from "primereact/card";
const beautify = require("simply-beautiful");
export default function Job() {
  const { data, isError, isFetching, isSuccess } = useGetJobsQuery();
  const { jobId }: { jobId: string } = useParams() as { jobId: string };
  const [job, setJob] = useState<any>(null);

  useEffect(() => {
    if (data && data.jobs) {
      const _job = data.jobs.find((job: any) => job.id === jobId);

      setJob(
        _job
          ? {
              ..._job,
              inputs: beautify.json(
                JSON.stringify({
                  ..._job.inputs,
                  custom_locks: parseJson(_job.inputs.custom_locks).output,
                  token_addresses: parseJson(_job.inputs.token_addresses).output,
                }),
                {
                  indent_size: 2,
                }
              ),
            }
          : null
      );
    }
  }, [data]);
  return (
    <>
      <AppLoadingError isError={isError} isFetching={isFetching} text='Error...' />

      {isSuccess && data && data.jobs && (
        <>
          {job && job.xid ? (
            <>
              <div className='jobMainInfo'>
                <Card className='section'>
                  <p className='title'>{job.name}</p>
                  <p className='subTitle'>
                    Xid: <strong>{job.xid}</strong>
                  </p>
                  <p className='subTitle'>
                    ID: <strong>{job.id}</strong>
                  </p>
                  <p className='subTitle'>
                    Result:
                    <span
                      className={classNames({
                        jobStatus: true,
                        done: job.result === "ok",
                        fail: job.result === "error",
                      })}
                    >
                      {job.result ? job.result : <i className='pi pi-spin pi-spinner' />}
                    </span>
                    State:
                    <span className={classNames({ jobState: true, finished: job.state === "finished" })}>
                      {job.state}
                    </span>
                  </p>
                  <p className='subTitle'>
                    Duration:{" "}
                    <strong>
                      {job.tsEnd && job.tsStart ? (
                        <>
                          {(() => {
                            const duration = moment.duration(job.tsEnd - job.tsStart);
                            const hours = duration.hours();
                            const minutes = duration.minutes();
                            const seconds = duration.seconds();
                            const milliseconds = duration.milliseconds();

                            return (
                              <>
                                {hours ? (
                                  <> {hours} hours</>
                                ) : (
                                  <>
                                    {minutes ? (
                                      <> {minutes} minutes</>
                                    ) : (
                                      <>{seconds ? <> {seconds} seconds</> : <>{milliseconds} milliseconds</>}</>
                                    )}
                                  </>
                                )}
                              </>
                            );
                          })()}
                        </>
                      ) : (
                        <i className='pi pi-spin pi-spinner' />
                      )}
                    </strong>
                  </p>
                  <p className='subTitle'>
                    Created: <strong>{moment(job.ts0).format("YYYY MMM DD HH:mm:ss")}</strong>
                  </p>
                  <p className='subTitle'>
                    Started:{" "}
                    <strong>
                      {job.tsStart ? (
                        moment(job.tsStart).format("YYYY MMM DD HH:mm:ss")
                      ) : (
                        <i className='pi pi-spin pi-spinner' />
                      )}
                    </strong>
                  </p>
                  <p className='subTitle'>
                    Complete:{" "}
                    <strong>
                      <>
                        {job.tsEnd ? (
                          moment(job.tsEnd).format("YYYY MMM DD HH:mm:ss")
                        ) : (
                          <i className='pi pi-spin pi-spinner' />
                        )}
                      </>
                    </strong>
                  </p>
                </Card>
                <Card className='section'>
                  <p className='title '>Inputs</p>
                  <ReactCodeMirror
                    className='codeInput'
                    value={job.inputs}
                    height='250px'
                    readOnly={true}
                    extensions={[json()]}
                  />
                </Card>
                <Card className='basis section'>
                  <p className='title '>Output</p>
                  {job.output ? (
                    <ReactCodeMirror
                      className='codeInput'
                      value={job.output}
                      height='200px'
                      readOnly={true}
                      extensions={[markdown()]}
                    />
                  ) : (
                    <>Job in progress...</>
                  )}
                </Card>
                <Card className='basis section'>
                  <p className='title '>Logs</p>
                  {job.log ? (
                    <ReactCodeMirror
                      className='codeInput'
                      value={job.log.join("\n")}
                      height='200px'
                      readOnly={true}
                      extensions={[markdown()]}
                    />
                  ) : (
                    <>Job in progress...</>
                  )}
                </Card>
              </div>
            </>
          ) : (
            <>
              <h1 className='text-24 mb-16'>Job: {jobId} not found</h1>
            </>
          )}
        </>
      )}
    </>
  );
}
