import { Card } from "primereact/card";
import AnalysisRunJobRequest from "./AnalysisRunJobRequest";
import { useEffect, useState } from "react";
import { useSearchParams } from "react-router-dom";
import { useGetTokenByAddressQuery } from "@app/common/services/token.service";
import { useCreateJobMutation } from "@app/common/services/job.service";

export default function JobRunner() {
  const [searchParams, setSearchParams] = useSearchParams("");
  const [addr, setAddr] = useState<string>("");
  const { data, isSuccess, isLoading } = useGetTokenByAddressQuery(addr, { skip: addr?.length === 0 });
  const [createJob] = useCreateJobMutation();

  useEffect(() => {
    const bid = searchParams.get("bid");

    if (bid) {
      setAddr(bid);

      setSearchParams({});
    } else {
    }
  }, [searchParams]);

  const startJob = (job: any) => {
    setAddr("");
    createJob(job);
  };

  return (
    <div className='circJobRunner'>
      <h2 className='text-20 mb-14'>Create new Job</h2>

      <Card>
        {addr.length === 0 && !isLoading && (
          <div className='tokenNotSelected'>
            <h3 className='text-18 mb-14'>Token not selected</h3>
            <p>Use search to select a token for job execution.</p>
            <p>
              To activate search click on search input above or press <strong>"CTRL + /"</strong>
            </p>
          </div>
        )}

        {data && isSuccess && <AnalysisRunJobRequest token={data} onAction={(job: any) => startJob(job)} />}
      </Card>
    </div>
  );
}
