import AppInputValidationMessage from "@app/common/components/AppInputValidationMessage/AppInputValidationMessage";
import { chains } from "@app/common/config/chain";
import { IToken } from "@app/common/models/token";
import { useUpdateTokenMutation } from "@app/common/services/token.service";
import { RootState } from "@app/common/store";
import { useAppSelector } from "@app/common/store/hooks";
import { Button } from "primereact/button";
import { Dialog } from "primereact/dialog";
import { InputSwitch } from "primereact/inputswitch";
import { InputText } from "primereact/inputtext";
import { classNames } from "primereact/utils";
import { useEffect, useRef, useState } from "react";
import { Controller, useForm } from "react-hook-form";

import AnalysisRangeCalculate from "./AnalysisRangeCalculate";
import AnalysisRunJobRequestCustomLocks from "./AnalysisRunJobRequestCustomLocks";
import { ConfirmPopup } from "primereact/confirmpopup";

export default function AnalysisRunJobRequest({ token, onAction }: { token: IToken; onAction: (param: any) => any }) {
  const common = useAppSelector((state: RootState) => state.common);
  const defaultForm = {
    tokenId: "",
    ts0: 0,
    ts1: 0,
    name: "",
  };
  const {
    control,
    formState: { errors },
    handleSubmit,
    setValue,
    getValues,
    clearErrors,
  } = useForm({
    defaultValues: { ...defaultForm },
  });
  const [estimatedCosts, setEstimatedCosts] = useState("");
  const [tokenAddresses, setTokenAddresses] = useState(token.chain);
  const [customLocks, setCustomLocks] = useState<any>({ content: <></>, visible: false });
  const [jobPayload, setJobPayload] = useState({});
  const [confirmJobVisible, setConfirmJobVisible] = useState(false);
  const confirmJobButton = useRef<any>(null);
  const formJob = useRef<any>(null);
  const [updateToken, updateTokenData] = useUpdateTokenMutation();

  useEffect(() => {
    const tokenLocksHash = token.locks.reduce((acc: any, lock: any) => {
      acc[lock.bid] = lock.lock.map((item: any) => ({ ...item, removable: true }));
      return acc;
    }, {});

    setValue("name", `${token.id}-${common.enroll.email}`);
    setValue("tokenId", token.id);

    setTokenAddresses(
      token.chain
        .reduce((acc: any[], item: any) => {
          if (chains[item.bid] && chains[item.bid].weight > 0) {
            const locks = tokenLocksHash[item.bid] || [];
            acc.push({ ...item, ...chains[item.bid], selected: chains[item.bid].weight === 100, locks });
          }

          return acc;
        }, [])
        .sort((a, b) => b.weight - a.weight)
    );
  }, [token]);

  useEffect(() => {
    if (updateTokenData.isSuccess && !updateTokenData.isLoading) {
      const payload: any = jobPayload;
      const mapTokenAddresses = tokenAddresses.reduce(
        (acc: any, item: any) => {
          if (item.selected) {
            acc.token_addresses.length > 40 && (acc.token_addresses += ",");
            acc.token_addresses += `'${item.bid}': '${item.addr}'`;

            if (item.locks.length > 0) {
              acc.custom_locks.length > 40 && (acc.custom_locks += ",");
              let custom_locks = item.locks.reduce((_acc: string, _item: any) => {
                _acc.length > 40 && (_acc += ",");
                _acc += `'${_item.addr}':'${_item.tag}'`;
                return _acc;
              }, `'${item.bid}': {`);

              custom_locks += "}";
              acc.custom_locks += custom_locks;
            }
          }

          return acc;
        },
        {
          token_addresses: "{",
          custom_locks: "{",
        }
      );
      mapTokenAddresses.token_addresses += "}";
      mapTokenAddresses.custom_locks += "}";
      const submitPayload = {
        name: payload.name,
        src: "file:///store/pipeline/circ-holders-supply.py",
        inputs: {
          from_date_input: `'${payload.ts0}'`,
          to_date_input: `'${payload.ts1}'`,
          token_id: `'${payload.tokenId}'`,
          token_addresses: mapTokenAddresses.token_addresses,
          custom_locks: mapTokenAddresses.custom_locks,
        },
      };
      onAction(submitPayload);
    }
  }, [updateTokenData, jobPayload]);

  return (
    <div className='runJobRequest'>
      <form
        ref={formJob}
        onSubmit={handleSubmit((payload) => {
          setJobPayload(payload);
          updateToken({
            id: token.id,
            locks: tokenAddresses.map((addr: any) => {
              return {
                bid: addr.bid,
                lock: addr.locks.map((item: any) => ({ addr: item.addr, tag: item.tag })),
              };
            }),
          });
        })}
      >
        <div className='appFormControls'>
          <div className='appFormControls2Col'>
            <div className='inputGroup'>
              <label htmlFor='contract'>Job name *</label>
              <Controller
                name='name'
                control={control}
                rules={{
                  required: "Token ID is required.",
                  pattern: {
                    value: /[a-z, A-Z, 0-9]{3,}/,
                    message: "Value should be at least 3 letters",
                  },
                }}
                render={({ field, fieldState }) => (
                  <InputText
                    id={field.name}
                    {...field}
                    placeholder='Enter job name'
                    className={classNames({ "p-invalid": fieldState.invalid })}
                  />
                )}
              />
              <AppInputValidationMessage field={"name"} errors={errors} />
            </div>
            <div className='inputGroup'>
              <label htmlFor='contract'>Token ID *</label>
              <Controller
                name='tokenId'
                control={control}
                rules={{
                  required: "Token ID is required.",
                  pattern: {
                    value: /[a-z, A-Z, 0-9]{3,}/,
                    message: "Value should be at least 3 letters",
                  },
                }}
                render={({ field, fieldState }) => (
                  <InputText
                    id={field.name}
                    {...field}
                    placeholder='Token ID'
                    className={classNames({ "p-invalid": fieldState.invalid })}
                    disabled={true}
                  />
                )}
              />
              <AppInputValidationMessage field={"tokenId"} errors={errors} />
            </div>
          </div>
          <div className='inputGroup'>
            <label>Token Addresses *</label>
          </div>
          {tokenAddresses.map((chain: any, index) => (
            <div className='chainRow' key={index}>
              <InputSwitch
                checked={chain.selected}
                onChange={(e) => {
                  setTokenAddresses((prev: any) => {
                    prev[index] = { ...chain, selected: e.value };
                    return [...prev];
                  });
                }}
              />
              <div className='inputGroup bid'>
                <InputText value={chain.bid} disabled={true} />
              </div>
              <div className='inputGroup addr'>
                <InputText value={chain.addr} disabled={true} />
              </div>
              {chain.selected && (
                <i
                  className='p-tag p-component p-tag-info'
                  onClick={() => {
                    setCustomLocks({
                      visible: true,
                      content: (
                        <>
                          <AnalysisRunJobRequestCustomLocks
                            locks={chain.locks}
                            item={{
                              addr: "",
                              tag: "",
                              removable: false,
                            }}
                            onAction={(locks: any) => {
                              setTokenAddresses((prev: any) => {
                                prev[index] = { ...prev[index], locks: locks };

                                return [...prev];
                              });
                              setCustomLocks({ content: <></>, visible: false });
                            }}
                          />
                        </>
                      ),
                    });
                  }}
                >
                  {chain.locks && chain.locks.length > 0 ? "Edit" : "Add"} custom locks
                </i>
              )}
              <div className='chainLocks'>
                {chain.locks && chain.locks.length > 0 && chain.selected && (
                  <table>
                    <thead>
                      <tr>
                        <td>Address</td>
                        <td>Tag</td>
                      </tr>
                    </thead>
                    <tbody>
                      {chain.locks.map((lock: any, index: number) => (
                        <tr key={index}>
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
        </div>
        <AnalysisRangeCalculate
          isActive={true}
          range={{ ts0: Date.now() - 86400000 * 90, ts1: Date.now() }}
          onAction={(value: any) => {
            setTimeout(() => {
              setEstimatedCosts(value.estimatedCosts);
              setValue("ts0", value.ts0);
              setValue("ts1", value.ts1);
              clearErrors();
            });
          }}
        />

        <Button
          className='p-button-outlined p-button-secondary'
          ref={confirmJobButton}
          onClick={(event: any) => {
            event.preventDefault();
            event.stopPropagation();
            setConfirmJobVisible(true);
          }}
        >
          Calculate (for {estimatedCosts}$)
        </Button>
        <ConfirmPopup
          target={confirmJobButton.current}
          visible={confirmJobVisible}
          onHide={() => setConfirmJobVisible(false)}
          message={
            <>
              <div className='acceptDialog'>
                <p className='acceptTitle'>Please confirm job execution</p>
                <p>
                  <strong>Token:</strong> {getValues("tokenId")}
                </p>
                <p>
                  <strong>Time range:</strong> {getValues("ts0")} {getValues("ts1")}
                </p>
              </div>
            </>
          }
          icon='pi pi-exclamation-triangle'
          accept={() => {
            formJob.current.dispatchEvent(new Event("submit", { cancelable: true, bubbles: true }));
          }}
          reject={() => {}}
        />
      </form>

      <Dialog
        header='Custom Locks'
        visible={customLocks.visible}
        onHide={() => setCustomLocks({ content: <></>, visible: false })}
      >
        {customLocks.content}
      </Dialog>
    </div>
  );
}
