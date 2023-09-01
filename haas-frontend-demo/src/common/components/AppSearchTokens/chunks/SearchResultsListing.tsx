import { IToken } from "@app/common/models/token";
import { VirtualScroller } from "primereact/virtualscroller";
import { useEffect, useRef } from "react";

import SearchResultsListingItem from "./SearchResultsListingItem";

type SearchResultsListingType = {
  data: IToken[];
  activeItemIndex: number;
  submitSearch: (index: number) => void;
};

function SearchResultsListing({ data, activeItemIndex, submitSearch }: SearchResultsListingType) {
  const virtualScroller = useRef<any>(null);
  useEffect(() => {
    if (virtualScroller.current) {
      virtualScroller.current.scrollToIndex(activeItemIndex);
    }
  }, [activeItemIndex]);

  if (data.length > 2) {
    return (
      <VirtualScroller
        ref={virtualScroller}
        className='searchResultsContainer searchResultsContainer--virtual'
        items={data}
        itemSize={70}
        itemTemplate={(item, scroll) => {
          return (
            <SearchResultsListingItem
              item={item}
              activeItemIndex={activeItemIndex}
              index={scroll.index}
              submitSearch={submitSearch}
            />
          );
        }}
      />
    );
  }

  return (
    <div className='searchResultsContainer'>
      {data.map((item, index: number) => (
        <SearchResultsListingItem
          key={item.id}
          item={item}
          activeItemIndex={activeItemIndex}
          submitSearch={submitSearch}
          index={index}
        />
      ))}
    </div>
  );
}

export default SearchResultsListing;
