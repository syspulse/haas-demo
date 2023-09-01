import useDebounce from "@app/common/hooks/useDebounce";
import { Button } from "primereact/button";
import { forwardRef, useEffect, useState } from "react";
import { useDispatch } from "react-redux";
import { createUpdateToken } from "@app/common/store/slice/appControlState";
import { InputText } from "primereact/inputtext";

const DiscoverFilter = forwardRef((props: { onFilter: any }, ref: any) => {
  const dispatch = useDispatch();
  const [filters, setFilters] = useState({ category: "", sortDir: "", txt: "" });
  const debouncedFilters = useDebounce(filters, 500);

  const sortDir = [
    {
      key: "asc",
      value: "A-Z",
    },
    {
      key: "desk",
      value: "Z-A",
    },
  ];

  useEffect(() => {
    props.onFilter(debouncedFilters);
  }, [debouncedFilters, props]);

  return (
    <div ref={ref} className='discoverTableFilers'>
      <div className='discoverTableFilerItem hideCirc'>
        <label className='text-12 text-w400 text--gray-600' htmlFor='sortByName'>
          Filter by name
        </label>
        <span className='p-input-icon-right'>
          <InputText
            placeholder='Enter token name'
            value={filters.txt}
            onChange={(e) => setFilters((value) => ({ ...value, txt: e.target.value }))}
          />
          {filters.txt.length > 0 && (
            <i className='pi pi-times' onClick={(e) => setFilters((value) => ({ ...value, txt: "" }))} />
          )}
        </span>
      </div>
      {/* <div className='discoverTableFilerItem'>
        <label className='text-12 text-w400 text--gray-600' htmlFor='sortByName'>
          Sort by Name
        </label>
        <Dropdown
          id='sortByName'
          optionLabel='value'
          optionValue='key'
          value={filters.sortDir ? filters.sortDir : sortDir[0].key}
          options={sortDir}
          placeholder='Sort by Name'
          onChange={(e) => setFilters((value) => ({ ...value, sortDir: e.value }))}
          tooltip='Sort by Name'
          tooltipOptions={{ position: "top" }}
        />
      </div>
      <div className='discoverTableFilerItem'>
        <label className='text-12 text-w400 text--gray-600' htmlFor='sortByCategory'>
          Filter by Category
        </label>
        <Dropdown
          id='sortByCategory'
          value={filters.category ? filters.category : "all"}
          options={tokenCategories.map((item) => item.value)}
          placeholder='Filter by Category'
          filter
          filterBy='value'
          onChange={(e) => setFilters((value) => ({ ...value, category: e.value }))}
          tooltip='Filter by Category'
          tooltipOptions={{ position: "top" }}
        />
      </div> */}
      <Button
        className='p-button-secondary p-button-outlined addToken'
        onClick={() => {
          dispatch(createUpdateToken({}));
        }}
      >
        Add new Token
      </Button>
    </div>
  );
});

export default DiscoverFilter;
