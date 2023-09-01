import AppInputValidationMessage from "@app/common/components/AppInputValidationMessage/AppInputValidationMessage";
import { appLocales } from "@app/common/locales/locales";
import { IEnroll } from "@app/common/models/auth";
import { useGetUserQuery } from "@app/common/services/user.service";
import { RootState } from "@app/common/store";
import { useAppSelector } from "@app/common/store/hooks";
import { logOut, setNotebookToken } from "@app/common/store/slice/appCommonState";
import { Button } from "primereact/button";
import { Card } from "primereact/card";
import { Dropdown } from "primereact/dropdown";
import { InputText } from "primereact/inputtext";
import { classNames } from "primereact/utils";
import { ReactNode, useEffect } from "react";
import { Controller, SubmitHandler, useForm } from "react-hook-form";
import { useDispatch } from "react-redux";

import "./style.scss";

const profileActionsSet = ["general", "interface"];
const defaultAvatar = "https://avatars.githubusercontent.com/u/1753933?v=4";
const AppEditProfile = ({
  onSave,
  formsSet,
  hideFormName,
  acceptButtonText,
}: {
  onSave: SubmitHandler<Partial<IEnroll>>;
  formsSet?: string[];
  hideFormName?: boolean;
  acceptButtonText?: string;
}) => {
  const dispatch = useDispatch();
  const cancelButton = !acceptButtonText ? "" : "Cancel";

  !formsSet && (formsSet = profileActionsSet);
  !acceptButtonText && (acceptButtonText = "Save Changes");

  const common = useAppSelector((state: RootState) => state.common);

  const { data, isSuccess } = useGetUserQuery("", { skip: !common.isAuthorized });
  const provider = common.enroll.provider;
  const language = common.language;
  const notebook = common.notebook;

  const defaultValues: Partial<IEnroll> = {
    name: "",
    xid: "",
    email: "",
    avatar: defaultAvatar,
    locale: language,
  };
  const {
    control,
    formState: { errors },
    handleSubmit,
    setValue,
    clearErrors,
    getValues,
  } = useForm({ defaultValues });

  useEffect(() => {
    if (!common.isAuthorized) {
      setValue("name", common.enroll.name);
      setValue("email", common.enroll.email);
      setValue("xid", common.enroll.xid);
      setValue("avatar", common.enroll.avatar);
      clearErrors();
    }
  }, []);

  useEffect(() => {
    if (data && isSuccess) {
      const availableKeys = Object.keys(defaultValues);

      Object.entries(data).forEach((item) => {
        const key = item[0] as any;
        availableKeys.includes(item[0]) && setValue(key, item[1]);
      });
      clearErrors();
    }
  }, [data]);

  const forms = {
    general: (
      <Card key='general'>
        {!hideFormName && <p className='text-20 mb-20 no-select'>General</p>}

        <div className='inputGroup'>
          <label htmlFor='name'>Name *</label>
          <Controller
            name='name'
            control={control}
            rules={{
              required: "Name is required.",
              pattern: {
                value: /[a-z, A-Z]{2,}/,
                message: "Name should be at least 2 letters",
              },
            }}
            render={({ field, fieldState }) => (
              <InputText
                id={field.name}
                {...field}
                placeholder='Name'
                className={classNames({ "p-invalid": fieldState.invalid })}
              />
            )}
          />
          <AppInputValidationMessage field={"name"} errors={errors} />
        </div>
        <div className='inputGroup'>
          <label htmlFor='email'>Email Address *</label>
          <Controller
            name='email'
            control={control}
            rules={{
              required: "Email is required.",
              pattern: {
                value: /^[A-Z0-9._%+-]+@[A-Z0-9.-]+\.[A-Z]{2,4}$/i,
                message: "Invalid email address. E.g. example@email.com",
              },
            }}
            render={({ field, fieldState }) => (
              <InputText
                id={field.name}
                {...field}
                disabled={provider ? provider === "google" : false}
                placeholder='Email address'
                className={classNames({ "p-invalid": fieldState.invalid })}
              />
            )}
          />
          <AppInputValidationMessage field={"email"} errors={errors} />
        </div>

        <div className={classNames("inputGroup", { hidden: provider !== "metamask" })}>
          <label htmlFor='xid'>Ethereum address</label>
          <Controller
            name='xid'
            control={control}
            render={({ field, fieldState }) => (
              <InputText
                id={field.name}
                {...field}
                disabled={provider ? provider === "metamask" : false}
                placeholder='Ethereum address'
                className={classNames({ "p-invalid": fieldState.invalid })}
              />
            )}
          />
          <AppInputValidationMessage field={"email"} errors={errors} />
        </div>
      </Card>
    ),
    interface: (
      <Card className='my-16' key='interface'>
        {!hideFormName && <p className='text-20 mb-20 no-select'>Interface</p>}

        <div className='inputGroup'>
          <label htmlFor='language'>Language</label>
          <Dropdown
            optionLabel='name'
            optionValue='key'
            value={getValues("locale")}
            options={Object.values(appLocales)}
            onChange={(event) => {
              setValue("locale", event.value);
              clearErrors("locale");
            }}
            placeholder='Select Language'
          />
        </div>

        <div className='inputGroup'>
          <label htmlFor='language'>Jupyter token</label>

          <InputText
            value={notebook.token}
            placeholder='Jupyter token'
            onChange={(e) => dispatch(setNotebookToken(e.target.value))}
          />
        </div>
      </Card>
    ),
  };

  return (
    <div className='rootEditProfile'>
      <form className='appFormControls' onSubmit={handleSubmit(onSave)}>
        {Object.entries(forms).reduce((acc: ReactNode[], item) => {
          if (formsSet?.includes(item[0])) {
            acc.push(item[1]);
          }
          return acc;
        }, [])}
        <div className='controlGroup'>
          {cancelButton.length > 0 && (
            <Button
              label={cancelButton}
              className='p-button-outlined p-button-danger'
              onClick={(event) => {
                event.preventDefault();
                event.stopPropagation();

                dispatch(logOut());
              }}
            />
          )}
          <Button type='submit' label={acceptButtonText} className='p-button-primary' />
        </div>
      </form>
    </div>
  );
};

export default AppEditProfile;
