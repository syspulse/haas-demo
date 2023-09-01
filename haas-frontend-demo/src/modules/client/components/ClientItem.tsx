import AppLoadingError from "@app/common/components/AppLoadingError/AppLoadingError";
import { useDeleteClientMutation } from "@app/common/services/client.service";
import Clipboard from "@app/common/utils/Clipboard";
import { getDate } from "@app/common/utils/getDate";
import { ConfirmPopup, confirmPopup } from "primereact/confirmpopup";

export default function ClientItem({ item }: any) {
  const [deleteClient, clientData] = useDeleteClientMutation();
  const confirmDelete = (event: any) => {
    confirmPopup({
      target: event.currentTarget,
      message: "Do you want to delete this record?",
      icon: "pi pi-info-circle",
      acceptClassName: "p-button-danger",
      accept: () => deleteClient(item.cid),
    });
  };
  return (
    <>
      <ConfirmPopup />
      <AppLoadingError isError={false} isFetching={clientData.isLoading} />
      <div className='clientItem'>
        <div className='clientItemHeader'>
          <p className='text-16'>{item.name}</p>
          <div className='nav'>
            <i className='pi pi-trash' onClick={confirmDelete} />
          </div>
        </div>
        <p>
          <span className='text--gray-600'>Client Id: </span>
          <span className='text--gray-900'>
            <Clipboard text={item.cid} maxLength={50} />
          </span>
        </p>
        <p>
          <span className='text--gray-600'>Created: </span> <span className='value'>{getDate(item.tsCreated)}</span>
        </p>
        <p>
          <span className='text--gray-600'>Expire: </span> <span className='value'>{getDate(item.expire)}</span>
        </p>
        <p>
          <span className='text--gray-600'>Secret: </span>
          <span className='color--blue-600'>
            <Clipboard text={item.secret} maxLength={20} />
          </span>
        </p>
      </div>
    </>
  );
}
