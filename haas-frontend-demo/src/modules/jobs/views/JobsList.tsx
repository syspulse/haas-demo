import AppLoadingError from "@app/common/components/AppLoadingError/AppLoadingError";
import JobsHistory from "@app/modules/jobs/components/JobsHistory";
import JobRunner from "@app/modules/jobs/components/JobRunner";
import { useGetJobsQuery } from "@app/common/services/job.service";

export default function JobsList() {
  const { data, isError, isFetching, isSuccess } = useGetJobsQuery();
  return (
    <>
      <h1 className='text-24 mb-16'>Job monitor</h1>
      <AppLoadingError isError={isError} isFetching={isFetching} text='Error...' />
      <JobRunner />
      {isSuccess && data && data.jobs && <JobsHistory data={data.jobs} />}
    </>
  );
}
