import AppInputValidationMessage from "@app/common/components/AppInputValidationMessage/AppInputValidationMessage";
import AppLoadingError from "@app/common/components/AppLoadingError/AppLoadingError";
import {
  IMonitor,
  monitorAlarms,
  monitorAlarmsPatterns,
  monitorNetworksPreset,
  monitorTypesPreset,
  monitorTypesPresetTypes,
  typIcons,
} from "@app/common/models/monitor";
import { useGetAbiByIdQuery } from "@app/common/services/abi.service";
import { useGetAbiQuery } from "@app/common/services/etherscan";
import {
  useCreateMonitorMutation,
  useGetMonitorScriptQuery,
  useGetScriptsQuery,
  useUpdateMonitorMutation,
  useUpdateScriptMutation,
} from "@app/common/services/transaction-monitoring";
import { RootState } from "@app/common/store";
import { useAppSelector } from "@app/common/store/hooks";
import Clipboard from "@app/common/utils/Clipboard";
import { java } from "@codemirror/lang-java";
import { javascript } from "@codemirror/lang-javascript";
import { json } from "@codemirror/lang-json";
import { markdown } from "@codemirror/lang-markdown";
import CodeMirror from "@uiw/react-codemirror";
import { Button } from "primereact/button";
import { Card } from "primereact/card";
import { Dialog } from "primereact/dialog";
import { Dropdown } from "primereact/dropdown";
import { InputText } from "primereact/inputtext";
import { classNames } from "primereact/utils";
import { ComponentProps, useEffect, useState } from "react";
import { Controller, useForm } from "react-hook-form";

const beautify = require("simply-beautiful");

type MonitorEditProps = ComponentProps<any> & {
  monitor: IMonitor;
};

const defaultMonitor = {
  alarm: ["ws://"],
  count: 0,
  id: "",
  name: "",
  scriptId: "",
  script: "",
  status: "",
  uid: null,
  alert: "",
  entity: "",
  abi: "",
  contract: "",
  bid: "",
  scriptDesc: "",
  scriptTyp: "js",
  scriptName: "",
};

export default function MonitorEdit({ isActive, monitor, onAction }: MonitorEditProps) {
  const [createMonitor, createMonitorData] = useCreateMonitorMutation();
  const [updateMonitor] = useUpdateMonitorMutation();
  const [updateScript, updateScriptData] = useUpdateScriptMutation();
  const { uid, email } = useAppSelector((state: RootState) => state.common.enroll);
  const scriptData = useGetMonitorScriptQuery(monitor?.scriptId, {
    skip: !isActive || !monitor?.scriptId,
  });
  const abiData = useGetAbiByIdQuery(
    { aid: monitor?.aid, entity: "contract" },
    {
      skip: !isActive || !monitor?.scriptId || !monitor?.aid,
    }
  );
  const [contract, setContact] = useState<string>("");
  const esAbi = useGetAbiQuery(contract, { skip: contract.length !== 42 });
  const [updateMonitorPayload, setUpdateMonitorPayload] = useState<null | IMonitor>(null);
  const scriptsData = useGetScriptsQuery("", { skip: !monitor?.id });

  const {
    control,
    formState: { errors },
    handleSubmit,
    setValue,
    clearErrors,
    getValues,
  } = useForm({
    defaultValues: { ...defaultMonitor },
  });

  useEffect(() => {
    if (monitor?.id && scriptData.isSuccess) {
      monitor?.alarm.forEach((item: string) => {
        if (item.includes("email://")) {
          setValue("alert", item.split("email://")[1]);
          setValue("alarm", monitor?.alarm || ["email://"]);
        } else {
          setValue("alarm", monitor?.alarm || ["ws://"]);
        }
      });

      setValue("name", monitor?.name || "");
      setValue("script", scriptData.data?.src || "");
      setValue("entity", monitor?.entity || "");
      setValue("abi", monitor?.abi || "");
      setValue("contract", monitor?.aid || "");
      setValue("bid", monitor?.bid || "");
      setValue("scriptId", scriptData.data?.id || "");
      setValue("scriptName", scriptData.data?.name || monitor?.name);
      setValue("scriptDesc", scriptData.data?.desc || "");
      setValue("scriptTyp", monitor?.scriptTyp || "js");
      if (abiData.isSuccess) {
        setValue(
          "abi",
          beautify.json(abiData.data.json, {
            indent_size: 2,
          })
        );
      }

      clearErrors();
    } else {
      const _monitorTypesPreset: monitorTypesPresetTypes[] = Object.values(monitorTypesPreset);
      setValue("entity", _monitorTypesPreset[0].value);
      setValue("script", _monitorTypesPreset[0].script);
    }
  }, [scriptData, abiData, setValue, clearErrors, monitor]);

  useEffect(() => {
    if (esAbi.data && esAbi.data.includes("[{")) {
      setValue(
        "abi",
        beautify.json(esAbi.data, {
          indent_size: 2,
        })
      );
    }
  }, [esAbi]);

  useEffect(() => {
    if (updateMonitorPayload !== null && updateScriptData.isSuccess && !updateScriptData.isLoading && scriptData.data) {
      setUpdateMonitorPayload(null);
      const scriptId = updateMonitorPayload.scriptId;

      delete updateMonitorPayload.scriptDesc;
      delete updateMonitorPayload.scriptTyp;

      updateMonitor({ ...updateMonitorPayload, id: monitor.id });
      onAction({ type: "hide", data: { ...defaultMonitor } });
    }
  }, [updateScriptData, updateMonitorPayload, monitor, scriptData]);

  const saveMonitor = (payload: any) => {
    const newMonitorPayload: IMonitor = {
      name: payload.name,
      script: payload.script,
      entity: payload.entity,
      alarm: payload.alarm.map((item: string) => {
        if (Object.keys(monitorAlarmsPatterns).includes(item)) {
          return item + payload.alert;
        }

        if (item.includes("ws://")) {
          return "ws://" + uid + Date.now();
        }

        return item;
      }),
      uid,
      bid: payload.bid,
      scriptDesc: payload.scriptDesc,
      scriptTyp: payload.scriptTyp,
    } as IMonitor;

    if (payload.contract.length === 42 && payload.abi.length > 10) {
      newMonitorPayload["contract"] = payload.contract;
      newMonitorPayload["abi"] = payload.abi;
    }

    if (monitor?.id && scriptData?.data) {
      const updateBody = { ...newMonitorPayload };

      delete updateBody.script;
      delete updateBody.scriptDesc;

      setUpdateMonitorPayload({ ...newMonitorPayload, script: `ref://${payload.scriptId}`, scriptId: payload.scriptId });

      updateScript({
        id: payload.scriptId,
        body: {
          desc: newMonitorPayload.scriptDesc,
          name: payload.scriptName,
          src: newMonitorPayload.script,
          typ: newMonitorPayload.scriptTyp,
        },
      });
    } else {
      createMonitor(newMonitorPayload);
      onAction({ type: "hide", data: { ...defaultMonitor } });
    }
  };

  return (
    <Dialog
      header={
        monitor?.id ? (
          <>
            Monitor <Clipboard text={monitor?.id} maxLength={50} />
          </>
        ) : (
          "Create Monitor"
        )
      }
      appendTo={document.body}
      visible={isActive}
      closeOnEscape={true}
      style={{ maxWidth: "800px" }}
      onHide={() => onAction({ type: "hide", data: { ...defaultMonitor } })}
    >
      <AppLoadingError isError={false} isFetching={createMonitorData.isLoading} />
      <form className='monitorEditFormWrapper' onSubmit={handleSubmit(saveMonitor)}>
        <Card className='monitoringEditForm'>
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
            {scriptsData?.data?.scripts && (
              <div className='inputGroup'>
                <label htmlFor=''>Script</label>
                <Controller
                  name='scriptTyp'
                  control={control}
                  render={({ field }) => (
                    <Dropdown
                      onChange={(e) => {
                        const script: any = scriptsData?.data?.scripts.find((item: any) => item.id === e.value);

                        setValue("scriptId", script.id);
                        setValue("scriptName", script.name);
                        setValue("script", script.src);
                        setValue("scriptTyp", script.typ);
                        setValue("scriptDesc", script.desc || "");

                        clearErrors();
                      }}
                      value={getValues("scriptId")}
                      options={scriptsData?.data?.scripts}
                      optionLabel='name'
                      optionValue='id'
                      placeholder='Select script'
                      filter
                      disabled={!monitor?.id}
                    />
                  )}
                />
                <AppInputValidationMessage field={"scriptTyp"} errors={errors} />
              </div>
            )}
            <div className='inputGroup'>
              <label htmlFor=''>Script language</label>
              <Controller
                name='scriptTyp'
                control={control}
                render={({ field }) => (
                  <Dropdown
                    {...field}
                    onChange={(e) => field.onChange(e.value)}
                    valueTemplate={(item: string) => (
                      <span className='dropDownItemTemplate'>
                        {item}
                        {typIcons[item]}
                      </span>
                    )}
                    itemTemplate={(item: string) => (
                      <span className='dropDownItemTemplate'>
                        {item}
                        {typIcons[item]}
                      </span>
                    )}
                    options={Object.keys(typIcons)}
                    placeholder='Select script language'
                    disabled={monitor?.id}
                  />
                )}
              />
              <AppInputValidationMessage field={"scriptTyp"} errors={errors} />
            </div>
            <div className='appFormControls2Col'>
              <div className='inputGroup'>
                <label htmlFor=''>Alarms Trigger</label>
                <Controller
                  name='alarm'
                  control={control}
                  render={({ field }) => (
                    <Dropdown
                      value={`${field.value.length > 0 ? (field.value[0] as any).split("://")[0] : ""}://`}
                      optionLabel='value'
                      optionValue='key'
                      options={monitorAlarms}
                      onChange={(e) => {
                        setValue(field.name, [e.value]);

                        if (Object.keys(monitorAlarmsPatterns).includes(e.value)) {
                          setValue(
                            "alert",
                            monitorAlarmsPatterns[e.value].value === "email"
                              ? email
                              : monitorAlarmsPatterns[e.value].value
                          );
                        } else {
                          setValue("alert", "");
                        }

                        clearErrors(field.name);
                      }}
                      placeholder='Select notification type'
                    />
                  )}
                />
              </div>
              {getValues("alarm").some((item: string) => Object.keys(monitorAlarmsPatterns).includes(item)) && (
                <div className='inputGroup'>
                  <label htmlFor='alert'>{monitorAlarmsPatterns[getValues("alarm") as any].title}*</label>
                  <Controller
                    name='alert'
                    control={control}
                    rules={monitorAlarmsPatterns[getValues("alarm") as any].validationRules}
                    render={({ field, fieldState }) => (
                      <InputText
                        id={field.name}
                        {...field}
                        placeholder={monitorAlarmsPatterns[getValues("alarm") as any].placeholder}
                        className={classNames({ "p-invalid": fieldState.invalid })}
                      />
                    )}
                  />
                  <AppInputValidationMessage field={"alert"} errors={errors} />
                </div>
              )}
            </div>
            <div className='appFormControls2Col'>
              <div className='inputGroup'>
                <label htmlFor=''>Network</label>
                <Controller
                  name='bid'
                  control={control}
                  rules={{
                    required: "Network is required.",
                  }}
                  render={({ field }) => (
                    <Dropdown
                      value={field.value}
                      valueTemplate={(item: string) => {
                        if (!item) {
                          return <>Select network</>;
                        }

                        return (
                          <span className='dropDownItemTemplate'>
                            <img src={monitorNetworksPreset[item].icon} alt={monitorNetworksPreset[item].title} />{" "}
                            {monitorNetworksPreset[item].title}
                          </span>
                        );
                      }}
                      itemTemplate={(item: string) => (
                        <span className='dropDownItemTemplate'>
                          <img src={monitorNetworksPreset[item].icon} alt={monitorNetworksPreset[item].title} />{" "}
                          {monitorNetworksPreset[item].title}
                        </span>
                      )}
                      options={Object.keys(monitorNetworksPreset)}
                      placeholder='Select network'
                      onChange={(e) => {
                        setValue(field.name, e.value);
                        clearErrors();
                      }}
                    />
                  )}
                />
                <AppInputValidationMessage field={"bid"} errors={errors} />
              </div>
              <div className='inputGroup'>
                <label htmlFor=''>Monitored entity</label>
                <Controller
                  name='entity'
                  control={control}
                  render={({ field }) => (
                    <Dropdown
                      value={field.value}
                      options={Object.values(monitorTypesPreset)}
                      placeholder='Select entity'
                      optionLabel='title'
                      optionValue='value'
                      onChange={(e) => {
                        setValue(field.name, e.value);
                        setValue(
                          "script",
                          beautify.json(monitorTypesPreset[e.value].script, {
                            indent_size: 2,
                          })
                        );
                        clearErrors();
                      }}
                    />
                  )}
                />
              </div>
            </div>

            {(getValues("entity") === "event" || getValues("entity") === "function") && (
              <>
                <div className='inputGroup inputGroup--split'>
                  <label htmlFor='contract'>Contract *</label>
                  <Controller
                    name='contract'
                    control={control}
                    rules={{
                      required: "Contract is required.",
                      pattern: {
                        value: /[a-z, A-Z, 0-9]{42,}/,
                        message: "Contract should be 42 symbols",
                      },
                    }}
                    render={({ field, fieldState }) => (
                      <>
                        <InputText
                          id={field.name}
                          {...field}
                          placeholder='Contract'
                          className={classNames({ "p-invalid": fieldState.invalid })}
                          onChange={(value) => {
                            setValue(field.name, value.target.value);
                          }}
                        />
                        <Button
                          disabled={getValues("contract").length !== 42 || esAbi.isLoading}
                          className='tooltip'
                          data-pr-position='right'
                          data-pr-tooltip='Fetch ABI from Etherscan'
                          onClick={(event) => {
                            event.preventDefault();
                            event.stopPropagation();
                            clearErrors("contract");
                            setContact(getValues("contract"));
                          }}
                        >
                          Get ABI
                        </Button>
                      </>
                    )}
                  />
                  <AppInputValidationMessage field={"contract"} errors={errors} />
                </div>

                <div className='inputGroup inputGroup--full'>
                  <label htmlFor=''>ABI *</label>
                  <Controller
                    name='abi'
                    control={control}
                    rules={{
                      required: getValues("entity") === "mempool" ? false : "ABI is required",
                    }}
                    render={({ field }) => (
                      <CodeMirror
                        className='codeInput'
                        value={field.value}
                        height='200px'
                        readOnly={esAbi.isLoading}
                        extensions={[json()]}
                        onChange={(change) => {
                          setValue(field.name, change);
                          clearErrors(field.name);
                        }}
                      />
                    )}
                  />
                  <AppInputValidationMessage field={"abi"} errors={errors} />
                </div>
              </>
            )}

            <div className='inputGroup inputGroup--full'>
              <label htmlFor=''>Script</label>
              <Controller
                name='script'
                control={control}
                rules={{
                  required: "Script is required.",
                }}
                render={({ field }) => (
                  <CodeMirror
                    className='codeInput'
                    value={field.value}
                    height='200px'
                    extensions={[javascript({ jsx: true }), java()]}
                    onChange={(change) => {
                      setValue(field.name, change);
                      clearErrors(field.name);
                    }}
                  />
                )}
              />
              <AppInputValidationMessage field={"script"} errors={errors} />
            </div>
            <div className='inputGroup inputGroup--full'>
              <label htmlFor=''>Script Description</label>
              <Controller
                name='scriptDesc'
                control={control}
                render={({ field }) => (
                  <CodeMirror
                    className='codeInput'
                    value={field.value}
                    height='200px'
                    extensions={[markdown()]}
                    onChange={(change) => {
                      setValue(field.name, change);
                      clearErrors(field.name);
                    }}
                  />
                )}
              />
            </div>
          </div>
        </Card>
        <div className='monitoringEditFormFooter'>
          <Button type='submit' className='p-button-primary p-center'>
            {!monitor?.id ? "Save" : "Update"}
          </Button>
        </div>
      </form>
    </Dialog>
  );
}
