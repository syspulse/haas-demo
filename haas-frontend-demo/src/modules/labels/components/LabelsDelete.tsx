import AppLoadingError from "@app/common/components/AppLoadingError/AppLoadingError";
import { bulkLabelsDelete, labelsApi } from "@app/common/services/labels.service";
import { Button } from "primereact/button";
import { Dialog } from "primereact/dialog";
import { useState } from "react";
import { useDispatch } from "react-redux";

export default function LabelsDelete({ deleteLabels, onAction }: any) {
  const [isActive, setIsActive] = useState<boolean>(false);
  const [inProgress, setInProgress] = useState<boolean>(false);
  const dispatch = useDispatch();

  return (
    <>
      {deleteLabels.length > 0 && (
        <Button
          icon='pi pi-trash'
          className='p-button-danger'
          label={`Delete ${deleteLabels.length} selected label${deleteLabels.length === 1 ? "" : "s"}`}
          onClick={() => setIsActive(true)}
        />
      )}
      <Dialog
        header={`Delete ${deleteLabels.length}`}
        visible={isActive}
        style={{ width: "50vw", maxWidth: "600px" }}
        onHide={() => setIsActive(false)}
      >
        <div className='deleteLabelsModal'>
          <AppLoadingError isError={false} isFetching={inProgress} />
          <p className='text-16'>Are you sure you want to delete following labels?</p>
          <ul>
            {deleteLabels.map((item: any, index: number) => (
              <li key={index}>
                {item.cat} / {item.id}
              </li>
            ))}
          </ul>
          <div className='footer'>
            <Button
              className='p-button-danger'
              onClick={async () => {
                setInProgress(true);
                await bulkLabelsDelete(deleteLabels);
                setInProgress(false);
                setIsActive(false);
                dispatch(labelsApi.util.invalidateTags(["GetLabels"])); // Should be moved to service, when bulk delete will be covered by RTK service
              }}
            >
              DELETE
            </Button>
            <Button className='p-button-outlined p-button-secondary' onClick={() => setIsActive(false)}>
              Cancel
            </Button>
          </div>
        </div>
      </Dialog>
    </>
  );
}
