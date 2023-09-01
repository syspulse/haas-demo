import { IToken } from "@app/common/models/token";
import { classNames } from "primereact/utils";
import { RefObject } from "react";

type SearchResultsListingItemType = {
  item: IToken;
  activeItemIndex: number;
  index: number;
  submitSearch: (index: number) => void;
};

function SearchResultsListingItem({ item, activeItemIndex, index, submitSearch }: SearchResultsListingItemType) {
  const tokenImagePlaceholder = "https://cdn-icons-png.flaticon.com/512/566/566295.png";

  return (
    <div
      className={classNames({ searchResultsItem: true, active: activeItemIndex === index })}
      onClick={() => submitSearch(index)}
    >
      <div className='icon'>
        <img src={item.icon ? item.icon : tokenImagePlaceholder} alt={item.name} />
      </div>
      <p className='text-16'>
        {item.name} <span className='color--gray-700 text-w400 text-uppercase'>{item.symbol}</span>
      </p>
      {Boolean(item?.addr) && <p className='text-12 text-w400 color--gray-700'>{item.addr}</p>}
    </div>
  );
}

export default SearchResultsListingItem;
