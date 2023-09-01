import { TableCell } from "@app/common/models/general";
import { ICircToken, IToken } from "@app/common/models/token";
import { useGetCircIdByTokenIdQuery } from "@app/common/services/circ.service";
import parseBigInt from "@app/common/utils/parseBigInt";
import { Button } from "primereact/button";
import { ProgressBar } from "primereact/progressbar";
import { useEffect } from "react";

import DiscoverTableCellLoader from "./DiscoverTableCellLoader";

type TableCellCirculatingType = TableCell<IToken>;

const TableCellCirculating = ({ item, isFetching }: TableCellCirculatingType) => {
  if (isFetching || !item) {
    return <DiscoverTableCellLoader isFetching={isFetching} />;
  }

  return item.circ?.history && item.circ?.history.length > 0 ? (
    <>
      <p className='text-14 mb-8'>
        <span className='mx-8'>{parseBigInt(item?.circ?.history[0].supply, item.dcml, "en-US")}</span>
      </p>
      <ProgressBar
        className='hide-1024 '
        value={(item?.circ?.history[0].supply / item?.circ?.history[0].totalSupply) * 100}
        style={{ height: "var(--4)" }}
        showValue={false}
      ></ProgressBar>
    </>
  ) : (
    <></>
  );
};

export default TableCellCirculating;
