import AppLoadingError from "@app/common/components/AppLoadingError/AppLoadingError";
import AppTablePagination from "@app/common/components/AppTablePagination/AppTablePagination";
import { defaultNS } from "@app/common/locales/locales";
import { IToken } from "@app/common/models/token";
import { useGetTokenAllQuery } from "@app/common/services/token.service";
import DiscoverFilter from "@app/modules/circ/views/Discover/components/DiscoverFilter/DiscoverFilter";
import TableCellAddress from "@app/modules/circ/views/Discover/components/DiscoverTable/DIscoverTableCellAddress";
import TableCellCategory from "@app/modules/circ/views/Discover/components/DiscoverTable/DiscoverTableCellCategory";
import TableCellChain from "@app/modules/circ/views/Discover/components/DiscoverTable/DiscoverTableCellChain";
import TableCellHolders from "@app/modules/circ/views/Discover/components/DiscoverTable/DiscoverTableCellHolders";
import TableCellToken from "@app/modules/circ/views/Discover/components/DiscoverTable/DiscoverTableCellToken";
import { Column } from "primereact/column";
import { DataTable } from "primereact/datatable";
import { PaginatorPageState } from "primereact/paginator";
import { useEffect, useLayoutEffect, useRef, useState } from "react";
import { useTranslation } from "react-i18next";
import { useDispatch } from "react-redux";
import { createUpdateToken } from "@app/common/store/slice/appControlState";
import { ConfirmPopup, confirmPopup } from "primereact/confirmpopup";
import { useDeleteTokenMutation } from "@app/common/services/token.service";
import { setModuleOutletHeaderText } from "@app/common/store/slice/appCommonState";
import TableOverlay from "@app/modules/circ/views/Discover/components/DiscoverTable/TableOverleay";

const DiscoverTableRoot = () => {
  const { t } = useTranslation(defaultNS, { keyPrefix: "circ.discover" });
  const dispatch = useDispatch();
  const filterRef = useRef<HTMLDivElement>(null);
  const paginatorRef = useRef<HTMLDivElement>(null);
  const ITEMS_PER_PAGE = 25;
  const [tableHeight, setTableHeight] = useState<string>("100%");
  const [pagination, setTablePagination] = useState<PaginatorPageState>({
    first: 0,
    rows: ITEMS_PER_PAGE,
    page: 1,
    pageCount: 1,
  });
  const [filters, setTableFilters] = useState({});
  const [getQuery, setGetQuery] = useState(`?from=0&size=${ITEMS_PER_PAGE}`);
  const [tableData, setTableData] = useState<IToken[]>([]);
  const { data, isFetching, refetch, isError } = useGetTokenAllQuery(getQuery);
  const [deleteToken] = useDeleteTokenMutation();
  const [action, setAction] = useState<any>({});

  const confirmDeleteToken = (event: any, id: string) => {
    confirmPopup({
      target: event.currentTarget,
      message: "Do you want to delete this Token?",
      icon: "pi pi-info-circle",
      acceptClassName: "p-button-danger",
      accept: () => deleteToken(id),
    });
  };

  useEffect(() => {
    const paginate = `?from=${pagination.first}&size=${pagination.rows}`;
    const filter = Object.entries(filters).reduce((acc, entry) => {
      if (entry[1]) {
        const value = entry[1] as string;
        if (entry[0] === "txt" && value.length > 2) {
          acc = `/typing/${entry[1]}${paginate}`;
        }
      }
      return acc;
    }, "");

    setGetQuery(filter.length > 0 ? filter : paginate);
  }, [filters, pagination]);

  useLayoutEffect(() => {
    setTimeout(() => {
      filterRef.current &&
        paginatorRef.current &&
        setTableHeight(`calc(100% - ${filterRef.current.offsetHeight + paginatorRef.current.offsetHeight}px)`);
    }, 300);
  }, []);

  useEffect(() => {
    if (data) {
      dispatch(setModuleOutletHeaderText(`${t("discover")} (${data.total}) ${t("tokens")}`));

      setTableData(data.tokens);
    }
  }, [data]);

  return (
    <>
      <AppLoadingError
        isError={isError}
        isFetching={isFetching && !data}
        refetch={refetch}
        text={t("errorLoading") as string}
        buttonText={t("reload") as string}
      />
      {data && (
        <DataTable
          className='discoverTable'
          value={tableData}
          resizableColumns
          autoLayout
          scrollHeight={tableHeight}
          style={{ width: "100%" }}
          responsiveLayout='stack'
          header={<DiscoverFilter ref={filterRef} onFilter={setTableFilters} />}
          footer={
            <AppTablePagination
              ref={paginatorRef}
              onPageChange={setTablePagination}
              pagination={pagination}
              total={data.total}
              pageItems={tableData.length}
            />
          }
          editMode='row'
          onRowClick={(row: any) => dispatch(createUpdateToken({ ...row.data }))}
        >
          <Column header={t("table.token")} body={(item) => <TableCellToken item={item} isFetching={isFetching} />} />
          <Column
            header={t("table.chain")}
            body={(item) => (
              <TableCellChain item={item} isFetching={isFetching} onAction={(action: any) => setAction(action)} />
            )}
          />
          <Column
            header={t("table.address")}
            body={(item) => <TableCellAddress item={item} isFetching={isFetching} />}
          />
          <Column
            header={t("table.category")}
            body={(item) => <TableCellCategory item={item} isFetching={isFetching} />}
          />
          <Column
            align='right'
            header={t("table.holders")}
            body={(item) => <TableCellHolders item={item} isFetching={isFetching} />}
          />
          <Column
            align='right'
            header=''
            body={(item) => (
              <span className='crudTokenCell'>
                <i
                  className='pi pi-pencil'
                  onClick={(event) => {
                    event.preventDefault();
                    event.stopPropagation();
                    dispatch(createUpdateToken({ ...item }));
                  }}
                />
                <i
                  className='pi pi-trash'
                  onClick={(event) => {
                    event.preventDefault();
                    event.stopPropagation();
                    confirmDeleteToken(event, item.id);
                  }}
                />
              </span>
            )}
          />
        </DataTable>
      )}
      <TableOverlay action={action} />
      <ConfirmPopup />
    </>
  );
};

export default DiscoverTableRoot;
