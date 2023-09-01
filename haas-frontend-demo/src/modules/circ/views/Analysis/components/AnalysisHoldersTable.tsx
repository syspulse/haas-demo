import AppTablePagination from "@app/common/components/AppTablePagination/AppTablePagination";
import { ICircTokenHolder } from "@app/common/models/token";
import { useGetLabelsByAddressQuery } from "@app/common/services/labels.service";
import { createUpdateLabel, searchLabels } from "@app/common/store/slice/appControlState";
import Clipboard from "@app/common/utils/Clipboard";
import OpenContract from "@app/common/utils/OpenAddress";
import { getDate } from "@app/common/utils/getDate";
import parseBigInt from "@app/common/utils/parseBigInt";
import { Card } from "primereact/card";
import { Column } from "primereact/column";
import { DataTable } from "primereact/datatable";
import { PaginatorPageState } from "primereact/paginator";
import { useEffect, useState } from "react";
import { useDispatch } from "react-redux";
import { useNavigate } from "react-router-dom";

export default function AnalysisHoldersTable({
  activeHistory,
  token,
  hidePercentage,
  hideHeader,
  total,
  from,
  onAction,
}: any) {
  const ITEMS_PER_PAGE = 10;
  const navigate = useNavigate();
  const dispatch = useDispatch();
  const [data, setData] = useState<any[]>([]);
  const [tagsHash, setTagsHash] = useState<any>({});
  const [pagination, setTablePagination] = useState<PaginatorPageState>({
    first: 0,
    rows: ITEMS_PER_PAGE,
    page: 1,
    pageCount: 1,
  });
  const labels = useGetLabelsByAddressQuery(
    data.map((item: any) => item.addr),
    { skip: data.length === 0 }
  );

  useEffect(() => {
    setTablePagination({
      first: from,
      rows: ITEMS_PER_PAGE,
      page: from ? from / ITEMS_PER_PAGE : 1,
      pageCount: 1,
    });
  }, [activeHistory]);

  useEffect(() => {
    if (labels.data) {
      const _tagsHash = labels.data.tags.reduce((acc: any, item: any) => {
        acc[item.id] = { list: [...item.tags], cat: item.cat, item };
        return acc;
      }, {});

      setTagsHash(_tagsHash);
    }
  }, [labels]);

  useEffect(() => {
    if (total) {
      setData(
        [...activeHistory.current.holders].map((item: any) => {
          return { ...item, lb: tagsHash[item.addr] ? tagsHash[item.addr] : {} };
        })
      );
    } else {
      setData(
        [...activeHistory.current.holders].splice(pagination.first, ITEMS_PER_PAGE).map((item: any) => {
          return { ...item, lb: tagsHash[item.addr] ? tagsHash[item.addr] : {} };
        })
      );
    }
  }, [activeHistory, tagsHash, pagination, total]);

  return (
    <Card className='p-card--large'>
      {!hideHeader && <p className='text-20 mb-24'>Holders ({getDate(activeHistory.current.ts)})</p>}
      <DataTable
        className='csHoldersTable'
        value={data}
        totalRecords={1000}
        responsiveLayout='scroll'
        currentPageReportTemplate='Showing {first} to {last} of {totalRecords}'
        size='small'
        style={{ width: "100%" }}
        footer={
          <AppTablePagination
            onPageChange={(pagination: any) => {
              setTablePagination(pagination);
              onAction(pagination.first);
            }}
            pagination={pagination}
            total={total || activeHistory.current.holders.length}
            pageItems={data.length}
          />
        }
      >
        <Column
          header='Address'
          body={(item: ICircTokenHolder) => {
            return (
              <span className='tableCellFlex'>
                <Clipboard text={item.addr} maxLength={15} />
                <OpenContract address={item.addr} provider='etherscan' tooltip='Open Etherscan' />
              </span>
            );
          }}
        ></Column>
        <Column
          header='Label'
          style={{ width: "40%" }}
          body={(item: any) => (
            <span className='tagsList'>
              {item.lb?.cat && (
                <>
                  {item.lb?.cat.length > 0 ? (
                    <span className='haas-tag haas-tag--category'>{item.lb.cat}</span>
                  ) : (
                    <>
                      <i className='navLink' onClick={() => dispatch(createUpdateLabel({ id: item.addr }))}>
                        Add label
                      </i>
                    </>
                  )}
                </>
              )}
              {item.lb?.list && item.lb?.list.length > 0 ? (
                <>
                  {item.lb.list
                    .filter((lb: any) => lb.length > 0)
                    .map((lb: any, index: number) => (
                      <span key={item.addr + index} className='haas-tag haas-tag--label'>
                        {lb}
                      </span>
                    ))}
                  <span className='tagsControls'>
                    <i
                      className='edit pi pi-pencil tooltip'
                      data-pr-tooltip='Edit label'
                      data-pr-position='top'
                      onClick={() => dispatch(createUpdateLabel(tagsHash[item.addr].item))}
                    />
                    <i
                      className='edit pi pi-search tooltip'
                      data-pr-tooltip='Find similar addresses'
                      data-pr-position='top'
                      onClick={() => {
                        dispatch(searchLabels(item.lb.list[0]));
                        navigate("/labels");
                      }}
                    />
                  </span>
                </>
              ) : (
                <i
                  className='navLink'
                  onClick={() =>
                    dispatch(
                      createUpdateLabel(
                        item.lb?.list && item.lb?.list.length === 0 ? tagsHash[item.addr].item : { id: item.addr }
                      )
                    )
                  }
                >
                  Add label
                </i>
              )}
            </span>
          )}
        ></Column>
        <Column
          header='Quantity'
          body={(row) => (
            <span className='tableCell'>{parseBigInt<string>(row.v || row.bal, token.dcml, "en-US")}</span>
          )}
        ></Column>
        {!hidePercentage && (
          <Column
            align='right'
            header='Percentage'
            body={(row) => <span className='tableCell'>{row.r}%</span>}
          ></Column>
        )}
      </DataTable>
    </Card>
  );
}
