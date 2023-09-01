import AppLoadingError from "@app/common/components/AppLoadingError/AppLoadingError";
import { useGetLabelsQuery } from "@app/common/services/labels.service";
import { useAppSelector } from "@app/common/store/hooks";
import { createUpdateLabel } from "@app/common/store/slice/appControlState";
import { Button } from "primereact/button";
import { useEffect, useState } from "react";
import { useDispatch } from "react-redux";
import { useNavigate } from "react-router-dom";

import LabelsDelete from "./components/LabelsDelete";
import LabelsEmptyState from "./components/LabelsEmptyState";
import LabelsSearch from "./components/LabelsSearch";
import LabelsTable from "./components/LabelsTable";
import "./styles.scss";

export default function Labels() {
  const TOP = 100;
  const [index, setIndex] = useState(0);
  const [deleteLabels, setDeleteLabels] = useState<String[]>([]);
  const [tagsSearch, setTagsSearch] = useState<string>("");
  const [fetchQuery, setFetchQuery] = useState(`?from=${index}&size=${TOP}`);
  const fetch = useGetLabelsQuery(fetchQuery);
  const { findLabels } = useAppSelector((state) => state.control);
  const dispatch = useDispatch();

  useEffect(() => {
    if (tagsSearch && tagsSearch.length > 1) {
      // inconsistent flow in the current scope, please fix as API will support filter by category or tag
      setIndex(0);
      const searchString =
        findLabels.length > 0
          ? `/find?cat=${tagsSearch}&from=${index}&size=${TOP}`
          : `/typing/${tagsSearch}?from=${index}&size=${TOP}`;
      setFetchQuery(searchString);
    } else {
      setFetchQuery(`?from=${index}&size=${TOP}`);
    }
  }, [tagsSearch, index, findLabels]);

  const handleTableAction = (action: any) => {
    if (action.type === "pagination") {
      setIndex(action.value);
    }

    if (action.type === "selection") {
      setDeleteLabels(action.value ? action.value : []);
    }
  };

  return (
    <div className='labelsRoot'>
      <h1 className='text-24 mb-16'>Labels</h1>
      <div className='labelsHeader'>
        <LabelsSearch onAction={setTagsSearch} />
        <div className='labelsHeaderOptions'>
          <Button icon='pi pi-plus' label='Add new label' onClick={() => dispatch(createUpdateLabel({}))} />
          <LabelsDelete deleteLabels={deleteLabels} />
        </div>
      </div>
      <AppLoadingError
        isError={fetch.isError}
        isFetching={fetch.isFetching}
        text='Error...'
        buttonText='Retry'
        refetch={fetch.refetch}
      />
      {fetch?.data && (
        <LabelsTable
          data={fetch.data?.tags}
          total={fetch.data?.total}
          top={TOP}
          fetchQuery={fetchQuery}
          onAction={handleTableAction}
        />
      )}
      {fetch?.data?.total === 0 && tagsSearch && <LabelsEmptyState />}
    </div>
  );
}
