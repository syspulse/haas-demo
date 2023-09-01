import AppInputValidationMessage from "@app/common/components/AppInputValidationMessage/AppInputValidationMessage";
import AppLoadingError from "@app/common/components/AppLoadingError/AppLoadingError";
import { useCreateClientMutation } from "@app/common/services/client.service";
import { Button } from "primereact/button";
import { InputText } from "primereact/inputtext";
import { classNames } from "primereact/utils";
import { Controller, useForm } from "react-hook-form";

export default function CreateClient({ client }: any) {
  const [createClient, clientData] = useCreateClientMutation();
  const {
    control,
    formState: { errors },
    handleSubmit,
    setValue,
    clearErrors,
    setError,
  } = useForm({
    defaultValues: { ...client },
  });
  return (
    <>
      <AppLoadingError isError={false} isFetching={clientData.isLoading} />
      <form onSubmit={handleSubmit((payload) => createClient(payload))}>
        <div className='appFormControls'>
          <div className='inputGroup'>
            <label htmlFor='name'>Name *</label>
            <Controller
              name='name'
              control={control}
              rules={{
                required: "Name is required.",
                pattern: {
                  value: /[a-z, A-Z, 0-9]{2,}/,
                  message: "Name should be great then 2 symbols",
                },
              }}
              render={({ field, fieldState }) => (
                <InputText
                  id={field.name}
                  {...field}
                  placeholder='name'
                  className={classNames({ "p-invalid": fieldState.invalid })}
                />
              )}
            />
            <AppInputValidationMessage field={"name"} errors={errors} />
          </div>
        </div>

        <Button type='submit' className='p-button-primary p-center'>
          Create
        </Button>
      </form>
    </>
  );
}
