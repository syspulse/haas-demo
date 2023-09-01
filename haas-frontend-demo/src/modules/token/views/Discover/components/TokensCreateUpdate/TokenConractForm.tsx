import { chains } from "@app/common/config/chain";
import { Button } from "primereact/button";
import { Dropdown } from "primereact/dropdown";
import { InputText } from "primereact/inputtext";
import { useEffect, useRef, useState } from "react";

export default function TokenContractForm({ _key, value, removable, onAction, children }: any) {
  const [formValues, setFormValues] = useState<any>({ _key, value, prevKey: _key });
  const [networkName, setNetworkName] = useState<string>("");
  const [chainList, setChainList] = useState<string[]>(Object.keys(chains));
  const dropdownRef = useRef<any>();
  const submit = (value: any) => {
    if (formValues._key.length > 0 && formValues.value.length > 0) {
      onAction({ ...value });

      if (!removable) {
        setFormValues({ _key: "", value: "", prevKey: "" });
      }
    }
  };

  useEffect(() => {
    if (formValues._key.length > 2 && formValues.value.length > 2) {
      const obj: any = {};

      obj[formValues._key] = formValues.value;

      submit({ type: "add", obj, prevKey: formValues.prevKey, currentKey: formValues._key });
    }
  }, [formValues]);

  return (
    <div className='tokenContactForm'>
      <Dropdown
        ref={dropdownRef}
        value={formValues._key}
        onChange={(event) => {
          setFormValues((prev: any) => ({ ...prev, _key: event.target.value }));
        }}
        options={chainList.filter((item: string) => item.includes(networkName))}
        filter
        filterTemplate={() => (
          <>
            <div className='multiselectCategory'>
              <span className='input p-input-icon-right'>
                {networkName.length > 0 && <i className='pi pi-times' onClick={() => setNetworkName("")} />}
                <InputText
                  name='customCategory'
                  placeholder='Find or add own one'
                  value={networkName}
                  onChange={(event) => setNetworkName(event.target.value)}
                />
              </span>

              <Button
                className='p-button-outlined p-button-secondary'
                onClick={() => {
                  setFormValues((prev: any) => ({ ...prev, _key: networkName }));
                  setChainList((prev: string[]) => [networkName, ...prev]);
                  setNetworkName("");
                  dropdownRef.current.hide();
                }}
                label='Add'
              />
            </div>
          </>
        )}
        placeholder='blockchain ID'
      />
      <InputText
        value={formValues.value}
        onChange={(event) => {
          setFormValues((prev: any) => ({ ...prev, value: event.target.value }));
        }}
        placeholder='Address'
      />
      <>
        {children}
        {removable && (
          <i
            className='pi pi-trash tooltip'
            data-pr-tooltip="Delete network"
            data-pr-position="top"
            onClick={() => {
              submit({ type: "remove", obj: formValues });
            }}
          />
        )}
      </>
    </div>
  );
}
