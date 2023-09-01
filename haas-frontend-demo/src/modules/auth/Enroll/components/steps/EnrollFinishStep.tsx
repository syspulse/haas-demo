import done from "@app/common/lottie-animations/done.json";
import { ComponentProps } from "@app/common/models/general";
import Lottie from "lottie-react";
import { Button } from "primereact/button";

export default function EnrollFinishStep({ isActive, onAction }: ComponentProps<any>) {
  if (!isActive) {
    return <></>;
  }

  return (
    <div className='enrollFinishStep appFormControls'>
      <p className='text-16 text-center'>Welcome on board!</p>
      <div className='enrollFinishAnimation'>
        <Lottie animationData={done} loop={false} />
      </div>
      <div className='controlGroup controlGroup--center'>
        <Button onClick={() => onAction()} label='Login' className='p-button-primary' />
      </div>
    </div>
  );
}
