import { TableCell } from "@app/common/models/general";
import { IToken } from "@app/common/models/token";
import { Tag } from "primereact/tag";

import DiscoverTableCellLoader from "./DiscoverTableCellLoader";

const TableCellChange = ({ item, isFetching }: TableCell<IToken>) => {
  if (isFetching || !item) {
    return <DiscoverTableCellLoader isFetching={isFetching} />;
  }

  return item ? (
    <Tag severity={0 ? "success" : "danger"} icon={"pi pi-caret-" + (0 ? "up" : "down")} value={0 + "%"} />
  ) : (
    <>/--/</>
  );
};

export default TableCellChange;
