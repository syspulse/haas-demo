import AppInputValidationMessage from "@app/common/components/AppInputValidationMessage/AppInputValidationMessage";
import AppLoadingError from "@app/common/components/AppLoadingError/AppLoadingError";
import { tokenCategories } from "@app/common/config/tokekCategories";
import { useCreateTokenMutation, useUpdateTokenMutation } from "@app/common/services/token.service";
import { useAppSelector } from "@app/common/store/hooks";
import { hideTokenForm } from "@app/common/store/slice/appControlState";
import { Button } from "primereact/button";
import { Card } from "primereact/card";
import { Dialog } from "primereact/dialog";
import { MultiSelect } from "primereact/multiselect";
import { InputText } from "primereact/inputtext";
import { classNames } from "primereact/utils";
import { useEffect, useState } from "react";
import { Controller, useForm } from "react-hook-form";
import { useDispatch } from "react-redux";

import "./style.scss";
import TokenContractForm from "./TokenConractForm";
import AnalysisRunJobRequestCustomLocks from "@app/modules/jobs/components/AnalysisRunJobRequestCustomLocks";

const defaultToken = {
  id: "",
  symbol: "",
  name: "",
  cat: [],
  icon: "",
  decimals: 18,
  contracts: {},
  locks: [],
};

export default function TokensCreateUpdate() {
  const { createUpdateTokens } = useAppSelector((state: any) => state.control);
  const dispatch = useDispatch();
  const [createToken, createTokenData] = useCreateTokenMutation();
  const [updateToken, updateTokenData] = useUpdateTokenMutation();
  const {
    control,
    formState: { errors },
    handleSubmit,
    setValue,
    clearErrors,
    reset,
  } = useForm({
    defaultValues: { ...defaultToken } as any,
  });
  const [customCategory, setCustomCategory] = useState("");
  const [categories, setCategories] = useState(
    [...tokenCategories].splice(1, tokenCategories.length).map((item) => item.value)
  );
  const [locks, setLocks] = useState<any>({});
  const [customLocksForm, setCustomLocksForm] = useState<any>({ content: <></>, visible: false });

  useEffect(() => {
    reset();
    createUpdateTokens.state.id && setValue("id", createUpdateTokens.state.id);
    createUpdateTokens.state.symbol && setValue("symbol", createUpdateTokens.state.symbol);
    createUpdateTokens.state.cat && setValue("cat", createUpdateTokens.state.cat);
    createUpdateTokens.state.name && setValue("name", createUpdateTokens.state.name);
    createUpdateTokens.state.dcml && setValue("decimals", createUpdateTokens.state.dcml);
    createUpdateTokens.state.icon && setValue("icon", createUpdateTokens.state.icon);
    createUpdateTokens.state.locks &&
      setLocks(
        createUpdateTokens.state.locks.reduce((acc: any, item: any) => {
          acc[item.bid] = item.lock.map((lock: any) => ({ ...lock, removable: true }));

          return acc;
        }, {})
      );

    if (createUpdateTokens.state.chain && createUpdateTokens.state.chain.length > 0) {
      createUpdateTokens.state.cat.forEach((cat: string) => {
        if (!categories.includes(cat)) {
          setCategories((prev) => [cat, ...prev]);
        }
      });

      const contracts: any = {};
      createUpdateTokens.state.chain.forEach((item: any) => {
        contracts[item.bid] = item.addr;
      });

      setValue("contracts", contracts);
    }

    clearErrors();
  }, [createUpdateTokens]);

  useEffect(() => {
    createTokenData.isSuccess && dispatch(hideTokenForm());
  }, [createTokenData]);

  useEffect(() => {
    updateTokenData.isSuccess && dispatch(hideTokenForm());
  }, [updateTokenData]);

  useEffect(() => {
    const updatedCustomLocks: any[] = [];
    Object.keys(locks).forEach((key: string) => {
      updatedCustomLocks.push({
        bid: key,
        lock: locks[key].map((item: any) => {
          const _item = { ...item };

          delete _item["removable"];

          return _item;
        }),
      });
    });

    setValue("locks", updatedCustomLocks);
  }, [locks]);

  return (
    <>
      <Dialog
        header={createUpdateTokens.state.cat ? "Edit Token" : "Add new Token"}
        visible={createUpdateTokens.active}
        style={{ width: "50vw", maxWidth: "500px" }}
        onHide={() => dispatch(hideTokenForm())}
      >
        <form
          className='monitorEditFormWrapper'
          onSubmit={handleSubmit((payload) => {
            if (createUpdateTokens && createUpdateTokens.state.id) {
              updateToken({ ...payload });
            } else {
              createToken({ ...payload });
            }

            reset();
          })}
        >
          <Card>
            <div className='appFormControls appFormControls--full'>
              <AppLoadingError isError={false} isFetching={createTokenData.isLoading || updateTokenData.isLoading} />
              <div className='inputGroup'>
                <label htmlFor='name'>ID *</label>
                <Controller
                  name='id'
                  control={control}
                  rules={{
                    required: "ID is required.",
                    pattern: {
                      value: /[a-z, A-Z, 0-9]{2,}/,
                      message: "ID should greater 2 symbols",
                    },
                  }}
                  render={({ field, fieldState }) => (
                    <InputText
                      id={field.name}
                      {...field}
                      placeholder='Token ID'
                      className={classNames({ "p-invalid": fieldState.invalid })}
                      disabled={Boolean(createUpdateTokens.state.id)}
                    />
                  )}
                />
                <AppInputValidationMessage field={"id"} errors={errors} />
              </div>

              <div className='inputGroup'>
                <label htmlFor='name'>Symbol *</label>
                <Controller
                  name='symbol'
                  control={control}
                  rules={{
                    required: "symbol is required.",
                    pattern: {
                      value: /[a-z, A-Z, 0-9]{2,}/,
                      message: "symbol should greater 2 symbols",
                    },
                  }}
                  render={({ field, fieldState }) => (
                    <InputText
                      id={field.name}
                      {...field}
                      placeholder='Token symbol'
                      className={classNames({ "p-invalid": fieldState.invalid })}
                    />
                  )}
                />
                <AppInputValidationMessage field={"symbol"} errors={errors} />
              </div>

              <div className='inputGroup'>
                <label htmlFor='name'>Name *</label>
                <Controller
                  name='name'
                  control={control}
                  rules={{
                    required: "name is required.",
                    pattern: {
                      value: /[a-z, A-Z, 0-9]{2,}/,
                      message: "name should greater 2 symbols",
                    },
                  }}
                  render={({ field, fieldState }) => (
                    <InputText
                      id={field.name}
                      {...field}
                      placeholder='Token name'
                      className={classNames({ "p-invalid": fieldState.invalid })}
                    />
                  )}
                />
                <AppInputValidationMessage field={"name"} errors={errors} />
              </div>

              <div className='inputGroup'>
                <label htmlFor='name'>Category *</label>
                <Controller
                  name='cat'
                  control={control}
                  rules={{
                    required: "Category is required.",
                    pattern: {
                      value: /[a-z, A-Z, 0-9]{2,}/,
                      message: "Category should be at least 2 letters",
                    },
                  }}
                  render={({ field, fieldState }) => (
                    <div className='multiselectCategory'>
                      <MultiSelect
                        id={field.name}
                        {...field}
                        onChange={(e) => field.onChange(e.value)}
                        options={categories.filter((item: string) =>
                          item.toLocaleLowerCase().includes(customCategory.toLocaleLowerCase())
                        )}
                        placeholder='Select category'
                        maxSelectedLabels={2}
                        className={classNames({ "p-invalid": fieldState.invalid })}
                        panelHeaderTemplate={
                          <div className='customCategoryForm'>
                            <span className='input p-input-icon-right'>
                              {customCategory.length > 0 && (
                                <i className='pi pi-times' onClick={() => setCustomCategory("")} />
                              )}
                              <InputText
                                name='customCategory'
                                placeholder='Find category or create own one'
                                value={customCategory}
                                onChange={(event) => setCustomCategory(event.target.value)}
                              />
                            </span>

                            <Button
                              className='p-button-outlined p-button-secondary'
                              onClick={() => {
                                setCategories((prev: any) => [customCategory, ...prev]);
                                setValue(field.name, [...field.value, customCategory]);
                                setCustomCategory("");
                                clearErrors();
                              }}
                              disabled={customCategory.length === 0}
                              label='Add'
                            />
                          </div>
                        }
                      />
                      {field.value.length > 0 && <i className='pi pi-times' onClick={() => setValue(field.name, [])} />}
                    </div>
                  )}
                />
                <AppInputValidationMessage field={"cat"} errors={errors} />
              </div>

              <div className='inputGroup'>
                <label htmlFor='name'>Icon url</label>
                <Controller
                  name='icon'
                  control={control}
                  render={({ field, fieldState }) => (
                    <InputText
                      id={field.name}
                      {...field}
                      placeholder='Icon url'
                      className={classNames({ "p-invalid": fieldState.invalid })}
                    />
                  )}
                />
              </div>

              <div className='inputGroup'>
                <label htmlFor='decimals'>Decimals</label>
                <Controller
                  name='decimals'
                  control={control}
                  render={({ field, fieldState }) => (
                    <InputText
                      id={field.name}
                      {...field}
                      placeholder='Decimals'
                      className={classNames({ "p-invalid": fieldState.invalid })}
                    />
                  )}
                />
              </div>

              <div className='inputGroup'>
                <label htmlFor='decimals'>Contracts</label>
                <Controller
                  name='contracts'
                  control={control}
                  render={({ field }) => (
                    <>
                      {Object.entries(field.value).map((item: any) => (
                        <div key={item[0] + "general"} className='contactsGroup'>
                          <TokenContractForm
                            key={item[0] + "contact"}
                            _key={item[0]}
                            value={item[1]}
                            removable={true}
                            onAction={(value: any) => {
                              if (value.type === "remove") {
                                const obj: any = { ...field.value };
                                delete obj[value.obj.prevKey];
                                setValue(field.name, { ...obj });
                              } else {
                                const obj: any = { ...field.value, ...value.obj };

                                if (value.currentKey !== value.prevKey) {
                                  delete obj[value.prevKey];
                                }

                                setValue(field.name, { ...obj });
                              }
                            }}
                          >
                            <i
                              key={item[0] + "_trigger"}
                              className='pi pi-lock tooltip'
                              data-pr-tooltip={`${
                                locks[item[0]] && locks[item[0]].length > 0 ? "Add" : "Edit"
                              } custom locks`}
                              data-pr-position='top'
                              onClick={() => {
                                setCustomLocksForm({
                                  visible: true,
                                  content: (
                                    <>
                                      <AnalysisRunJobRequestCustomLocks
                                        key={item[0] + "_locks-form"}
                                        locks={locks[item[0]] || []}
                                        item={{
                                          addr: "",
                                          tag: "",
                                          removable: false,
                                        }}
                                        onAction={(itemLocks: any) => {
                                          const currentLocks: any = {};

                                          currentLocks[item[0]] = [];

                                          currentLocks[item[0]] = itemLocks;

                                          setLocks((prev: any) => ({ ...prev, ...currentLocks }));
                                          setCustomLocksForm({ content: <></>, visible: false });
                                        }}
                                      />
                                    </>
                                  ),
                                });
                              }}
                            />
                          </TokenContractForm>

                          <div className='chainLocks' key={item[0] + "_table"}>
                            {locks[item[0]] && locks[item[0]].length > 0 && item && (
                              <table key={item[0] + "_table-1"}>
                                <thead>
                                  <tr>
                                    <td>Address</td>
                                    <td>Tag</td>
                                  </tr>
                                </thead>
                                <tbody>
                                  {locks[item[0]].map((lock: any, index: number) => (
                                    <tr key={index + "_lock-row"}>
                                      <td>{lock.addr}</td>
                                      <td>{lock.tag}</td>
                                    </tr>
                                  ))}
                                </tbody>
                              </table>
                            )}
                          </div>
                        </div>
                      ))}

                      <TokenContractForm
                        key={"contact-form-default"}
                        _key=''
                        value=''
                        onAction={(value: any) => {
                          setValue(field.name, { ...field.value, ...value.obj });
                        }}
                      />
                    </>
                  )}
                />
              </div>
            </div>

            <Button type='submit' className='p-button-outlined p-button-secondary p-center'>
              {createUpdateTokens.state?.cat ? "Update" : "Create"}
            </Button>
          </Card>
        </form>
      </Dialog>
      <Dialog
        header='Custom Locks'
        visible={customLocksForm.visible}
        onHide={() => setCustomLocksForm({ content: <></>, visible: false })}
      >
        {customLocksForm.content}
      </Dialog>
    </>
  );
}
