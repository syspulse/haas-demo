import { TableCell } from "@app/common/models/general";
import { ITokenMOCK } from "@app/common/models/token";

import DiscoverTableCellLoader from "./DiscoverTableCellLoader";

const TableCellPrice = ({ item, isFetching }: TableCell<ITokenMOCK>) => {
  if (isFetching || !item) {
    return <DiscoverTableCellLoader isFetching={isFetching} />;
  }


  return <span className="cellPrice">$ {item.price.toFixed(2)}</span>;
};

export default TableCellPrice;
