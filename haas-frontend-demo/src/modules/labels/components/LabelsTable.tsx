import AppTablePagination from "@app/common/components/AppTablePagination/AppTablePagination";
import { labelsConfig } from "@app/common/config/tokekCategories";
import { useUpdateTagMutation } from "@app/common/services/labels.service";
import Clipboard from "@app/common/utils/Clipboard";
import OpenContract from "@app/common/utils/OpenAddress";
import { getDate } from "@app/common/utils/getDate";
import { Button } from "primereact/button";
import { Chips } from "primereact/chips";
import { Column } from "primereact/column";
import { DataTable } from "primereact/datatable";
import { Dropdown } from "primereact/dropdown";
import { PaginatorPageState } from "primereact/paginator";
import { Skeleton } from "primereact/skeleton";
import { Tag } from "primereact/tag";
import { useEffect, useRef, useState } from "react";

export default function LabelsTable({ data, total, top, onAction, fetchQuery }: any) {
  const [updateTag, updateTagData] = useUpdateTagMutation();
  const [pagination, setTablePagination] = useState<PaginatorPageState>({
    first: 0,
    rows: top,
    page: 1,
    pageCount: 1,
  });
  const paginatorRef = useRef<HTMLDivElement>(null);
  let updatingIdRef = useRef<string>("");
  const [selectedLabels, setSelectedLabels] = useState(null);

  useEffect(() => {
    setSelectedLabels(null);
  }, [data]);

  useEffect(() => {
    onAction({ type: "pagination", value: pagination.first });
  }, [pagination]);

  useEffect(() => {
    onAction({ type: "selection", value: selectedLabels });
  }, [selectedLabels]);

  const bodyTemplates: any = {
    id: (item: any) => (
      <span className='labelAddress'>
        <span className='address'>
          <Clipboard text={item.id} maxLength={20} />
        </span>
        <OpenContract address={item.id} provider='etherscan' tooltip='Open Etherscan' />
      </span>
    ),
    cat: (item: any) => (
      <span className='category-col'>
        <span className='haas-tag haas-tag--category'>{item.cat}</span>
      </span>
    ),
    tags: (item: any) => (
      <span className='tagList'>
        {item.tags.map((tag: string, index: number) => (
          <span key={tag + index} className='haas-tag haas-tag--label'>
            {tag}
          </span>
        ))}
      </span>
    ),
    tagsEdit: (options: any) => (
      <Chips value={options.value} allowDuplicate={false} onChange={(e) => options.editorCallback(e.value)} />
    ),
    catEdit: (options: any) => (
      <Dropdown
        value={options.value}
        onChange={(e) => options.editorCallback(e.target.value)}
        options={labelsConfig}
        editable
        filter
        filterTemplate={() => (
          <>
            <Button
              className='p-button-outlined p-button-secondary p-button-sm'
              onClick={() => options.editorCallback("")}
            >
              Add new +
            </Button>
          </>
        )}
        placeholder='Create category name or select from a list'
      />
    ),
    ts: (item: any) => getDate(item.ts),
  };

  const dynamicColumns =
    data.length > 0 ? (
      Object.keys(data[0]).map((col) => {
        return (
          <Column
            key={col}
            field={col}
            header={col}
            className={`col-${col}`}
            body={
              updateTagData.isLoading
                ? (item) =>
                    updatingIdRef.current === item.id ? (
                      <Skeleton width='100%' height='24px' />
                    ) : bodyTemplates[col] ? (
                      bodyTemplates[col](item)
                    ) : (
                      <>{item[col]}</>
                    )
                : bodyTemplates[col]
            }
            editor={(options: any) => {
              if (bodyTemplates[options.field + "Edit"]) {
                return bodyTemplates[options.field + "Edit"](options);
              }

              if (bodyTemplates[options.field]) {
                return bodyTemplates[options.field](options.rowData);
              }

              return <>{options.value}</>;
            }}
          />
        );
      })
    ) : (
      <></>
    );

  return (
    <>
      {data.length > 0 && (
        <>
          <DataTable
            editMode='row'
            dataKey='id'
            selectionMode={"checkbox"}
            selection={selectedLabels}
            onSelectionChange={(e) => setSelectedLabels(e.value)}
            onRowEditComplete={(e) => {
              updatingIdRef.current = e.newData.id;
              updateTag({
                body: {
                  id: e.newData.id,
                  cat: e.newData.cat,
                  tags: e.newData.tags,
                },
                query: fetchQuery,
              });
            }}
            className='discoverTable'
            value={data}
            responsiveLayout='scroll'
            autoLayout
            scrollHeight='calc(100vh - 280px)'
            footer={
              <AppTablePagination
                ref={paginatorRef}
                onPageChange={setTablePagination}
                pagination={pagination}
                total={total}
                pageItems={Object.values(data).length}
              />
            }
          >
            <Column selectionMode='multiple' headerStyle={{ width: "3rem" }}></Column>
            {dynamicColumns}
            <Column
              rowEditor
              headerStyle={{ width: "10%", minWidth: "8rem" }}
              bodyStyle={{ textAlign: "center" }}
            ></Column>
          </DataTable>
        </>
      )}
    </>
  );
}
