import AppLoadingError from "@app/common/components/AppLoadingError/AppLoadingError";
import AppTablePagination from "@app/common/components/AppTablePagination/AppTablePagination";
import { defaultNS } from "@app/common/locales/locales";
import { THash } from "@app/common/models/general";
import { IToken } from "@app/common/models/token";
import { getBulkTokens, useGetCircLastQuery } from "@app/common/services/circ.service";
import { setModuleOutletHeaderText } from "@app/common/store/slice/appCommonState";
import { Column } from "primereact/column";
import { DataTable, DataTableRowClickEventParams } from "primereact/datatable";
import { OverlayPanel } from "primereact/overlaypanel";
import { PaginatorPageState } from "primereact/paginator";
import { useEffect, useLayoutEffect, useRef, useState } from "react";
import { useTranslation } from "react-i18next";
import { useDispatch } from "react-redux";
import { useNavigate } from "react-router-dom";

import DiscoverFilter from "../DiscoverFilter/DiscoverFilter";
import TableCellAddress from "./DIscoverTableCellAddress";
import TableCellCategory from "./DiscoverTableCellCategory";
import TableCellChain from "./DiscoverTableCellChain";
import TableCellCirculating from "./DiscoverTableCellCirculating";
import TableCellHolders from "./DiscoverTableCellHolders";
import TableCellToken from "./DiscoverTableCellToken";
import TableOverlay from "./TableOverleay";

const DiscoverTableRoot = ({ totalItems }: { totalItems: number }) => {
  const { t } = useTranslation(defaultNS, { keyPrefix: "circ.discover" });
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const filterRef = useRef<HTMLDivElement>(null);
  const paginatorRef = useRef<HTMLDivElement>(null);
  const ITEMS_PER_PAGE = 100;
  const [tableHeight, setTableHeight] = useState<string>("100%");
  const [pagination, setTablePagination] = useState<PaginatorPageState>({
    first: 0,
    rows: ITEMS_PER_PAGE,
    page: 1,
    pageCount: 1,
  });
  const circLast = useGetCircLastQuery();
  const [tableData, setTableData] = useState<any>({});
  const [isTableLoading, setTableLoading] = useState(true);
  const [action, setAction] = useState<any>({});

  useEffect(() => {
    if (circLast.isSuccess) {
      const tokens = Object.values(circLast.data);

      dispatch(setModuleOutletHeaderText(`Circulation Supply (${tokens.length}) ${t("tokens")}`));

      getBulkTokens(tokens.map((token: any) => token.tokenId)).then((res) => {
        setTableData(
          res.reduce((acc: any, resData: any) => {
            const item: any = resData;
            const tmp: THash<IToken> = {};

            tmp[item.id] = circLast.data[item.addr]
              ? { ...item, circ: circLast.data[item.addr] }
              : { ...item, circ: circLast.data[item.id] };

            if (circLast.data[item.addr] && circLast.data[item.addr].history.length > 0) {
              acc = { ...tmp, ...acc };
            } else {
              acc = { ...acc, ...tmp };
            }

            return acc;
          }, {})
        );
        setTableLoading(false);
      });
    }
  }, [circLast]);

  useLayoutEffect(() => {
    filterRef.current &&
      paginatorRef.current &&
      setTableHeight(`calc(100% - ${filterRef.current.offsetHeight + paginatorRef.current.offsetHeight}px)`);
  }, []);

  return (
    <>
      <AppLoadingError
        isError={circLast.isError}
        isFetching={isTableLoading}
        refetch={circLast.refetch}
        text={t("errorLoading") as string}
        buttonText={t("reload") as string}
      />
      <DataTable
        className='discoverTable'
        value={Object.values(tableData)}
        resizableColumns
        autoLayout
        scrollHeight={tableHeight}
        style={{ width: "100%" }}
        responsiveLayout='stack'
        header={<DiscoverFilter ref={filterRef} onFilter={() => {}} />}
        footer={
          <AppTablePagination
            ref={paginatorRef}
            onPageChange={setTablePagination}
            pagination={pagination}
            total={totalItems}
            pageItems={Object.values(tableData).length}
          />
        }
        editMode='row'
        onRowClick={(row: DataTableRowClickEventParams) => navigate(row.data.addr)}
      >
        <Column
          header={t("table.token")}
          body={(item) => <TableCellToken item={item} isFetching={tableData.length === 0} />}
        />
        <Column
          header={t("table.chain")}
          body={(item) => (
            <TableCellChain
              item={item}
              isFetching={tableData.length === 0}
              onAction={(action: any) => setAction(action)}
            />
          )}
        />
        <Column
          header={t("table.address")}
          body={(item) => <TableCellAddress item={item} isFetching={tableData.length === 0} />}
        />
        <Column
          header={t("table.category")}
          body={(item) => <TableCellCategory item={item} isFetching={tableData.length === 0} />}
        />
        <Column
          align='right'
          header={t("table.circulating")}
          body={(item) => <TableCellCirculating item={item} isFetching={tableData.length === 0} />}
        />
        <Column
          align='right'
          header={t("table.holders")}
          body={(item) => <TableCellHolders item={item} isFetching={tableData.length === 0} />}
        />
      </DataTable>
      <TableOverlay action={action} />
    </>
  );
};

export default DiscoverTableRoot;
