import { ComponentProps } from "@app/common/models/general";
import { monitorTypesPreset } from "@app/common/models/monitor";
import { Button } from "primereact/button";
import { Dropdown } from "primereact/dropdown";
import { TreeSelect } from "primereact/treeselect";
import { useState } from "react";

export default function MonitoringFilters({ isActive, onAction }: ComponentProps<any>) {
  const [filter, setFilter] = useState<any>({
    filter: {
      All: {
        checked: true,
        partialChecked: false,
      },
      Status: {
        checked: true,
        partialChecked: false,
      },
      Started: {
        checked: true,
        partialChecked: false,
      },
      Stopped: {
        checked: true,
        partialChecked: false,
      },
      "Entity type": {
        checked: true,
        partialChecked: false,
      },
      Transaction: {
        checked: true,
        partialChecked: false,
      },
      Block: {
        checked: true,
        partialChecked: false,
      },
      Token: {
        checked: true,
        partialChecked: false,
      },
      Event: {
        checked: true,
        partialChecked: false,
      },
      Function: {
        checked: true,
        partialChecked: false,
      },
      Mempool: {
        checked: true,
        partialChecked: false,
      },
      Email: {
        checked: true,
        partialChecked: false,
      },
      Websocket: {
        checked: true,
        partialChecked: false,
      },
    },
    sort: "Alerts",
  });
  if (!isActive) return <></>;

  return (
    <div className='monitoringFilters'>
      <div className='appFormControls'>
        <div className='inputGroup'>
          <label>Filter by</label>
          <TreeSelect
            value={filter.filter}
            panelHeaderTemplate={() => <></>}
            valueTemplate={(item: any) => {
              const isAll = item.findIndex((i: any) => i.key === "All") > -1;

              return isAll
                ? "All"
                : item.reduce((acc: string, i: any) => {
                    if (i.key !== "All" && i.key !== "Status" && i.key !== "Entity type") {
                      if (acc.length > 0) {
                        acc += `, ${i.key}`;
                      } else {
                        acc += i.key;
                      }
                    }

                    return acc;
                  }, "");
            }}
            options={[
              {
                key: "All",
                label: "All",
                children: [
                  {
                    key: "Status",
                    label: "Status",
                    selectable: false,
                    children: [
                      {
                        key: "Started",
                        label: "Started",
                      },
                      {
                        key: "Stopped",
                        label: "Stopped",
                      },
                    ],
                  },
                  {
                    key: "Entity type",
                    label: "Entity type",
                    selectable: false,
                    children: Object.values(monitorTypesPreset).map((item) => ({ key: item.title, label: item.title })),
                  },
                  {
                    key: "Notification",
                    label: "Notification",
                    selectable: false,
                    children: [
                      {
                        key: "Websocket",
                        label: "Websocket",
                      },
                      {
                        key: "Email",
                        label: "Email",
                      },
                    ],
                  },
                ],
              },
            ]}
            onChange={(e) => {
              setFilter((prev: any) => ({ ...prev, filter: e.value }));
              const selection = e.value as any;
              onAction({
                type: "filter",
                data: Object.entries(selection).reduce((acc: string[], item: any) => {
                  if (item[1].checked) {
                    acc.push(item[0] === "Transaction" ? "tx" : item[0].toLowerCase());
                  }
                  return acc;
                }, []),
              });
            }}
            selectionMode='checkbox'
            metaKeySelection={false}
          />
        </div>
        <div className='inputGroup'>
          <label>Sort by</label>
          <Dropdown
            value={filter.sort}
            options={["Alerts", "Name"]}
            onChange={(e) => {
              setFilter((prev: any) => ({ ...prev, sort: e.value }));
              onAction({ type: "sort", data: e.value.toLowerCase() });
            }}
            placeholder='Sort by'
          />
        </div>
      </div>

      <Button
        label='Add New'
        icon='pi pi-plus'
        iconPos='right'
        className='p-button-outlined p-button-secondary p-center'
        // className='p-button-primary p-center'
        onClick={() => onAction({ type: "new" })}
      />
    </div>
  );
}
