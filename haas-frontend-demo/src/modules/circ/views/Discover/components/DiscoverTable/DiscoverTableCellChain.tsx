import { chains } from "@app/common/config/chain";
import DiscoverTableCellLoader from "./DiscoverTableCellLoader";

const TableCellChain = ({ item, isFetching, onAction }: any) => {
  if (isFetching || !item) {
    return <DiscoverTableCellLoader isFetching={isFetching} />;
  }

  const ch = item.chain
    .map((chain: any) => {
      return chains[chain?.bid]
        ? { ...chains[chain?.bid], addr: chain.addr, bid: chain.bid }
        : {
            icon: "https://chainlist.org/unknown-logo.png",
            name: chain?.bid,
            weight: 0,
            addr: chain.addr,
            bid: chain.bid,
          };
    })
    .sort((a: any, b: any) => b.weight - a.weight);

  const mainList = ch.splice(0, 5);

  return (
    <span className='tableCellChains'>
      {mainList.map((item: any) => (
        <img
          onClick={(event) => {
            event.preventDefault();
            event.stopPropagation();

            window.open(`//chainlist.org/?search=${item.addr}`, "_blank");
          }}
          key={item.name}
          src={item.icon}
          className='tooltip'
          data-pr-tooltip={item.name}
          data-pr-position='top'
        />
      ))}

      {ch.length > 0 && (
        <span
          className='more tooltip'
          data-pr-tooltip={`${ch.length} more...`}
          data-pr-position='top'
          onClick={(event) => {
            event.preventDefault();
            event.stopPropagation();
            onAction({ type: "listOverlay", event, data: ch });
          }}
        >
          <i className='pi pi-ellipsis-h' />
        </span>
      )}
    </span>
  );
};

export default TableCellChain;
