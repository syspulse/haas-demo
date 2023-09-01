import { InputText } from "primereact/inputtext";
import { classNames } from "primereact/utils";
import { useEffect, useState } from "react";

export default function AnalysisRunJobRequestCustomLocks({ locks, item, onAction }: any) {
  const defaultItem = {
    addr: "",
    tag: "",
    removable: false,
  };
  const [data, setData] = useState<any>({ ...defaultItem });
  const [savingActive, setSavingActive] = useState(false);
  const [pendingLocks, setPendingLocks] = useState(locks);

  useEffect(() => setData(item), [item]);

  useEffect(() => {
    const savingActive = Boolean(data.addr.length >= 42);

    setSavingActive(savingActive);

    if (item?.removable && savingActive) {
      onAction({ type: "update", data });
    }
  }, [data]);

  return (
    <>
      {pendingLocks &&
        pendingLocks.map((lock: any, index: number) => (
          <AnalysisRunJobRequestCustomLocks
            key={index + '_item-cs'}
            item={lock}
            onAction={(action: any) => {
              if (action.type === "remove") {
                setPendingLocks((prev: any) => {
                  const locks = [...prev];

                  locks.splice(index, 1);
                  return locks;
                });
              }

              if (action.type === "update") {
                setPendingLocks((prev: any) => {
                  prev[index] = action.data;

                  return [...prev];
                });
              }
            }}
          />
        ))}
      <div className='appFormControls customLocksRow'>
        <div className='inputGroup'>
          <InputText
            placeholder='Address'
            value={data.addr}
            onChange={(event) => setData((prev: any) => ({ ...prev, addr: event.target.value }))}
          />
        </div>
        <div className='inputGroup'>
          <InputText
            placeholder='Tag'
            value={data.tag}
            onChange={(event) => setData((prev: any) => ({ ...prev, tag: event.target.value }))}
          />
        </div>

        <i
          className={classNames({ "p-tag  p-component p-tag-info button tooltip": true, disabled: !savingActive })}
          data-pr-tooltip={savingActive ? "" : "1. Address should be equal or longer than 42 symbols"}
          data-pr-position='top'
          onClick={() => {
            if (!data.removable && savingActive && data) {
              setPendingLocks((prev: any) => [...prev, { ...data, removable: true }]);

              setData({ ...defaultItem });
            } else {
              onAction({ type: "remove" });
            }
          }}
        >
          {data.removable ? "Remove" : "Add"}
        </i>
      </div>
      <div className='customLocksFooter'>
        {locks && (
          <i className='button' onClick={() => onAction(locks)}>
            Cancel
          </i>
        )}
        {locks && (
          <i
            className='p-tag  p-component p-tag-info button'
            onClick={() => {
              const locks = savingActive ? [...pendingLocks, { ...data, removable: true }] : pendingLocks;

              onAction(locks);
            }}
          >
            Done
          </i>
        )}
      </div>
    </>
  );
}
