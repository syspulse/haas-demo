import AppLoadingError from "@app/common/components/AppLoadingError/AppLoadingError";
import AppTablePagination from "@app/common/components/AppTablePagination/AppTablePagination";
import { useGetAbiQuery, useGetAbiSearchQuery } from "@app/common/services/abi.service";
import OpenContract from "@app/common/utils/OpenAddress";
import { javascript } from "@codemirror/lang-javascript";
import { json } from "@codemirror/lang-json";
import CodeMirror from "@uiw/react-codemirror";
import { Column } from "primereact/column";
import { DataTable } from "primereact/datatable";
import { Dialog } from "primereact/dialog";
import { InputText } from "primereact/inputtext";
import { PaginatorPageState } from "primereact/paginator";
import { useEffect, useRef, useState } from "react";

const beautify = require("simply-beautiful");

export default function AbiTable({ entity, top }: any) {
  const [pagination, setTablePagination] = useState<PaginatorPageState>({
    first: 0,
    rows: top,
    page: 1,
    pageCount: 1,
  });
  const [previewAbi, setPreviewAbi] = useState<any>();
  const paginatorRef = useRef<HTMLDivElement>(null);
  const [index, setIndex] = useState(0);
  const [searchTxt, setSearch] = useState("");

  const fetch = useGetAbiQuery({ index, size: top, entity }, { skip: searchTxt.length > 0 });
  const search = useGetAbiSearchQuery({ index, size: top, entity, txt: searchTxt }, { skip: searchTxt.length === 0 });

  const [data, setData] = useState({ abis: [], total: 0 });

  useEffect(() => setIndex(pagination.first), [pagination]);

  useEffect(() => {
    if (fetch && fetch.isSuccess) {
      setData(fetch.data);
    }
  }, [fetch.data]);

  useEffect(() => {
    if (search && search.isSuccess) {
      setData(search.data);
    }
  }, [search.data]);

  useEffect(() => setIndex(0), [searchTxt]);

  const bodyTemplates: any = {
    addr: (item: any) => (
      <span className='addressCol'>
        {item.addr}
        <OpenContract address={item.addr} provider='etherscan' tooltip='Open Etherscan' />
      </span>
    ),
    json: (item: any) => (
      <span className='previewAbi' onClick={() => setPreviewAbi(item.json)}>
        <span>Preview</span> <i className='pi pi-eye' />
      </span>
    ),
    tex: (item: any) => <span className='codeLine'>{item.tex}</span>,
    hex: (item: any) => <span className='codeLine'>{item.hex}</span>,
  };

  const dynamicColumns =
    data.abis.length > 0 ? (
      Object.keys(data.abis[0]).map((col, i) => {
        return <Column key={col} field={col} header={col} className={`col-${col}`} body={bodyTemplates[col]} />;
      })
    ) : (
      <></>
    );

  return (
    <>
      <AppLoadingError
        isError={fetch.isError}
        isFetching={fetch.isFetching}
        text='Error...'
        buttonText='Retry'
        refetch={fetch.refetch}
      />

      <div className='searchInput'>
        <div className='p-input-icon-left'>
          <i className='pi pi-search'></i>
          <InputText
            type='search'
            onInput={(e: any) => setSearch(e.target.value)}
            placeholder={`Search for ${entity}`}
            size={50}
          />
        </div>
      </div>
      {data.abis.length > 0 && (
        <DataTable
          className='discoverTable'
          value={data.abis}
          responsiveLayout='scroll'
          autoLayout
          scrollHeight='calc(100vh - 280px)'
          footer={
            <AppTablePagination
              ref={paginatorRef}
              onPageChange={setTablePagination}
              pagination={pagination}
              total={data.total}
              pageItems={data.abis.length}
            />
          }
        >
          {dynamicColumns}
        </DataTable>
      )}
      <Dialog
        header='ABI json'
        visible={Boolean(previewAbi)}
        style={{ width: "50vw" }}
        onHide={() => setPreviewAbi(null)}
      >
        {previewAbi && (
          <CodeMirror
            className='codeInput'
            value={beautify.json(previewAbi, {
              indent_size: 2,
            })}
            height='70vh'
            extensions={[json()]}
            readOnly={true}
            onChange={() => {}}
          />
        )}
      </Dialog>
    </>
  );
}
