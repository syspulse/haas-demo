import { Button } from "primereact/button";
import { ComponentProps } from "react";

export default function MonitoringEmptyState({ isActive, onAction }: ComponentProps<any>) {
  if (!isActive) return <></>;
  return (
    <div className='transactionEmptyState'>
      <p className='text-20'></p>
      <p className='text-14 text--gray-600 text-w400 mb-16'>Create Interceptor Monitor</p>
      <Button
        label='Add New'
        icon='pi pi-plus'
        iconPos='right'
        // className='p-button-primary p-center'
        className='p-button-outlined p-button-secondary p-center'        
        onClick={() => onAction({ type: "new" })}
      />
    </div>
  );
}
