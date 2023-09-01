import AppInputValidationMessage from "@app/common/components/AppInputValidationMessage/AppInputValidationMessage";
import AppLoadingError from "@app/common/components/AppLoadingError/AppLoadingError";
import { labelsConfig } from "@app/common/config/tokekCategories";
import { useCreateTagMutation, useUpdateTagMutation } from "@app/common/services/labels.service";
import { useAppSelector } from "@app/common/store/hooks";
import { hideLabelForm } from "@app/common/store/slice/appControlState";
import { Button } from "primereact/button";
import { Card } from "primereact/card";
import { Chips } from "primereact/chips";
import { Dialog } from "primereact/dialog";
import { Dropdown } from "primereact/dropdown";
import { InputText } from "primereact/inputtext";
import { classNames } from "primereact/utils";
import { useEffect } from "react";
import { Controller, useForm } from "react-hook-form";
import { useDispatch } from "react-redux";

const defaultLabel = {
  id: "",
  cat: "",
  tags: [],
  ts: "",
};

export default function CreateLabel() {
  const { createUpdateLabels } = useAppSelector((state) => state.control);
  const dispatch = useDispatch();
  const [createTag, createTagData] = useCreateTagMutation();
  const [updateTag, updateTagData] = useUpdateTagMutation();
  const {
    control,
    formState: { errors },
    handleSubmit,
    setValue,
    clearErrors,
    setError,
    reset,
  } = useForm({
    defaultValues: { ...defaultLabel },
  });

  useEffect(() => {
    reset();

    createUpdateLabels.state.id && setValue("id", createUpdateLabels.state.id);

    createUpdateLabels.state.tags && setValue("tags", createUpdateLabels.state.tags);

    if (createUpdateLabels.state.cat) {
      setValue("cat", "");

      setTimeout(() => setValue("cat", createUpdateLabels.state.cat));
    }

    clearErrors();
  }, [createUpdateLabels]);

  useEffect(() => {
    createTagData.isSuccess && dispatch(hideLabelForm());
  }, [createTagData]);

  useEffect(() => {
    updateTagData.isSuccess && dispatch(hideLabelForm());
  }, [updateTagData]);

  return (
    <>
      <Dialog
        header={createUpdateLabels.state.ts ? "Edit label" : "Add new label"}
        visible={createUpdateLabels.active}
        style={{ width: "50vw", maxWidth: "400px" }}
        onHide={() => dispatch(hideLabelForm())}
      >
        <form
          className='monitorEditFormWrapper'
          onSubmit={handleSubmit((payload) => {
            const cat = Boolean(payload.cat) ? payload.cat : "";

            if (createUpdateLabels.state.ts) {
              updateTag({
                body: {
                  id: payload.id,
                  cat,
                  tags: payload.tags,
                },
              });
            } else {
              createTag({
                body: {
                  id: payload.id,
                  cat,
                  tags: payload.tags,
                  ts: Date.now(),
                },
              });
            }

            reset();
          })}
        >
          <Card>
            <div className='appFormControls'>
              <AppLoadingError isError={false} isFetching={createTagData.isLoading || updateTagData.isLoading} />
              <div className='inputGroup'>
                <label htmlFor='name'>Address *</label>
                <Controller
                  name='id'
                  control={control}
                  rules={{
                    required: "Address is required.",
                    pattern: {
                      value: /[a-z, A-Z, 0-9]{42,}/,
                      message: "Address should contain 42 symbols",
                    },
                  }}
                  render={({ field, fieldState }) => (
                    <InputText
                      id={field.name}
                      {...field}
                      placeholder='Address'
                      className={classNames({ "p-invalid": fieldState.invalid })}
                      disabled={createUpdateLabels.state.ts}
                    />
                  )}
                />
                <AppInputValidationMessage field={"id"} errors={errors} />
              </div>
              <div className='inputGroup'>
                <label htmlFor='name'>Category</label>
                <Controller
                  name='cat'
                  control={control}
                  render={({ field, fieldState }) => (
                    <Dropdown
                      id={field.name}
                      {...field}
                      onChange={(e) => field.onChange(e.value)}
                      className={classNames({ "p-invalid": fieldState.invalid })}
                      options={labelsConfig}
                      editable
                      showClear={field.value.length > 0}
                      placeholder='Create category name or select from a list'
                      filter
                      filterTemplate={() => (
                        <>
                          <Button
                            className='p-button-outlined p-button-secondary p-button-sm'
                            onClick={() => field.onChange("")}
                          >
                            Add new +
                          </Button>
                        </>
                      )}
                    />
                  )}
                />
                <AppInputValidationMessage field={"cat"} errors={errors} />
              </div>
              <div className='inputGroup'>
                <label htmlFor='tags'>Tags</label>
                <Controller
                  name='tags'
                  control={control}
                  render={({ field, fieldState }) => (
                    <Chips
                      id={field.name}
                      className={classNames({ "p-invalid": fieldState.invalid })}
                      value={field.value}
                      allowDuplicate={false}
                      placeholder='Enter tag name and push Enter to add'
                      onChange={(e: any) => setValue(field.name, e.value)}
                    />
                  )}
                />
                <AppInputValidationMessage field={"tags"} errors={errors} />
              </div>
            </div>

            <Button type='submit' className='p-button-outlined p-button-secondary p-center'>
              {createUpdateLabels.state?.ts ? "Update" : "Create"}
            </Button>
          </Card>
        </form>
      </Dialog>
    </>
  );
}
