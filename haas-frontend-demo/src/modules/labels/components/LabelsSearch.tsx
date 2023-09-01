import useDebounce from "@app/common/hooks/useDebounce";
import { useAppSelector } from "@app/common/store/hooks";
import { searchLabels } from "@app/common/store/slice/appControlState";
import { InputText } from "primereact/inputtext";
import { useEffect, useState } from "react";
import { useDispatch } from "react-redux";

export default function LabelsSearch({ onAction }: any) {
  const [search, setSearch] = useState("");
  const searchDebounced = useDebounce(search, 500);
  const { findLabels } = useAppSelector((state) => state.control);
  const dispatch = useDispatch();

  useEffect(() => onAction(searchDebounced), [searchDebounced]);
  useEffect(() => {
    findLabels.length > 0 && setSearch(findLabels);
  }, [findLabels]);
  useEffect(
    () => () => {
      dispatch(searchLabels(""));
    },
    []
  );

  return (
    <div className='labelsSearch'>
      <div className='p-input-icon-left'>
        <i className='pi pi-search'></i>
        <InputText
          type='search'
          value={search}
          onInput={(e: any) => {
            setSearch(e.target.value);
            dispatch(searchLabels(""));
          }}
          placeholder='Enter tag name'
          size={50}
        />
      </div>
    </div>
  );
}
