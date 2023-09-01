import { Button } from "primereact/button";
import { ProgressSpinner } from "primereact/progressspinner";

import "./style.scss";

type AppLoadingErrorType = {
  refetch?: () => void;
  isError: boolean;
  isFetching?: boolean;
  text?: string;
  buttonText?: string;
};

export default function AppLoadingError({ refetch, isError, isFetching, text, buttonText }: AppLoadingErrorType) {
  if (isError) {
    return (
      <div className='loadingError'>
        {text && <p className='text-14'>{text}</p>}
        {buttonText && refetch &&
          (<Button
            label={buttonText}
            onClick={() => refetch()}
            className='p-button-secondary p-button-outlined'
            icon='pi pi-sync'
          />
        )}
      </div>
    );
  }

  if (isFetching) {
    return (
      <div className='loadingError'>
        <ProgressSpinner />
      </div>
    );
  }
  return <></>;
}
