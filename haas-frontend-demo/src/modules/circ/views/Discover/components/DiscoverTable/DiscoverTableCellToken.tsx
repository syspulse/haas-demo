import { TableCell } from "@app/common/models/general";
import { IToken } from "@app/common/models/token";

import DiscoverTableCellLoader from "./DiscoverTableCellLoader";

const TableCellToken = ({ item, isFetching }: TableCell<IToken>) => {
  const tokenImagePlaceholder = "https://cdn-icons-png.flaticon.com/512/566/566295.png";

  if (isFetching || !item) {
    return <DiscoverTableCellLoader isFetching={isFetching} />;
  }

  return (
    <>
      <span className='tokenIcon hide-1024'>
        <img src={item.icon ? item.icon : tokenImagePlaceholder} alt={item.name} />
      </span>
      <p className='text-14'>
        {item.name}
        <span className='text--gray-600 text-w500 text-uppercase mx-8'>{item.symbol}</span>
      </p>
    </>
  );
};

export default TableCellToken;
