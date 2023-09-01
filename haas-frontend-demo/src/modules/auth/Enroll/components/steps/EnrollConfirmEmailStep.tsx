import AppInputValidationMessage from "@app/common/components/AppInputValidationMessage/AppInputValidationMessage";
import { ComponentProps } from "@app/common/models/general";
import { RootState } from "@app/common/store";
import { useAppSelector } from "@app/common/store/hooks";
import { setEnrollStep } from "@app/common/store/slice/appCommonState";
import { Button } from "primereact/button";
import { Card } from "primereact/card";
import { InputText } from "primereact/inputtext";
import { classNames } from "primereact/utils";
import { useEffect } from "react";
import { Controller, useForm } from "react-hook-form";
import { useDispatch } from "react-redux";

export default function EnrollConfirmEmailStep({ isActive, onAction }: ComponentProps<any>) {
  const dispatch = useDispatch();
  const { name, email } = useAppSelector((state: RootState) => state.common.enroll);

  const {
    control,
    formState: { errors },
    handleSubmit,
  } = useForm({
    defaultValues: {
      code: "",
    },
  });

  useEffect(() => {
    // isActive && getEmail({ id, email, name });
  }, [isActive]);

  if (!isActive) {
    return <></>;
  }

  return (
    <form className='appFormControls' onSubmit={handleSubmit(onAction)}>
      <Card>
        <p className='text-16 mb-16'>Confirms email address</p>
        <p className='text-14 text-w400 mb-8'>
          <strong className='text-w500 color-primary--600'>{name}</strong>, please check your email:
          <strong className='text-w500'> {email}</strong>
        </p>
        <div className='inputGroup'>
          <Controller
            name='code'
            control={control}
            rules={{
              required: "Code required",
            }}
            render={({ field, fieldState }) => (
              <InputText
                id={field.name}
                {...field}
                placeholder='Code'
                className={classNames({ "p-invalid": fieldState.invalid })}
              />
            )}
          />
          <AppInputValidationMessage field={"code"} errors={errors} />
        </div>
      </Card>
      <div className='controlGroup'>
        <Button
          onClick={(event) => {
            event.preventDefault();
            dispatch(setEnrollStep("START"));
          }}
          label='Back'
          className='p-button-secondary'
        />
        <Button type='submit' label='Next' className='p-button-primary' />
      </div>
    </form>
  );
}
