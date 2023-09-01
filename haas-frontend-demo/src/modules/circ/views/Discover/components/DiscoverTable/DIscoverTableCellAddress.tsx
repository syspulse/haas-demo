import { TableCell } from "@app/common/models/general";
import { IToken } from "@app/common/models/token";
import Clipboard from "@app/common/utils/Clipboard";
import OpenContract from "@app/common/utils/OpenAddress";

import DiscoverTableCellLoader from "./DiscoverTableCellLoader";

const TableCellAddress = ({ item, isFetching }: TableCell<IToken>) => {
  if (isFetching || !item) {
    return <DiscoverTableCellLoader isFetching={isFetching} />;
  }

  return (
    <span className='text--gray-600 tableCellFlex'>
      {item.addr && (
        <>
          <Clipboard text={item.addr} maxLength={10} />
          <OpenContract address={item.addr} provider='etherscan' tooltip='Open Etherscan' />
        </>
      )}
    </span>
  );
};

export default TableCellAddress;
