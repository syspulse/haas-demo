import { TableCell } from "@app/common/models/general";
import { IToken } from "@app/common/models/token";
import { Tag } from "primereact/tag";
import { ReactNode } from "react";

import DiscoverTableCellLoader from "./DiscoverTableCellLoader";

const TableCellCategory = ({ item, isFetching }: TableCell<IToken>) => {
  if (isFetching || !item) {
    return <DiscoverTableCellLoader isFetching={isFetching} />;
  }

  return (
    <span className='tableCellFlex'>
      {item.cat.reduce((acc: ReactNode[], _item, index) => {
        if (index < 2) {
          acc.push(<Tag severity='info' key={_item + index} value={_item} />);
        }

        if (index === item.cat.length - 1 && index > 1) {
          acc.push(
            <span key={index} className='text-12 text--gray-600'>
              + {item.cat.length - 2}
            </span>
          );
        }

        return acc;
      }, [])}
    </span>
  );
};

export default TableCellCategory;
