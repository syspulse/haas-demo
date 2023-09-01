import { TableCell } from "@app/common/models/general";
import { IToken } from "@app/common/models/token";

import DiscoverTableCellLoader from "./DiscoverTableCellLoader";

const TableCellHolders = ({ item, isFetching }: TableCell<IToken>) => {
  if (isFetching || !item) {
    return <DiscoverTableCellLoader isFetching={isFetching} />;
  }

  return (
    <p className='text-14'>
      {item?.circ &&
        item?.circ.history[item?.circ.history.length - 1] &&
        Math.trunc(item?.circ.history[item?.circ.history.length - 1]?.holdersTotal).toLocaleString("en-US")}
    </p>
  );
};

export default TableCellHolders;
