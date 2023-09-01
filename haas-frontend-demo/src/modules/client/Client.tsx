import AppLoadingError from "@app/common/components/AppLoadingError/AppLoadingError";
import { useCreateClientMutation, useGetClientsQuery } from "@app/common/services/client.service";
import { Button } from "primereact/button";
import { Card } from "primereact/card";
import { useEffect, useState } from "react";

import ClientItem from "./components/ClientItem";
import CreateClient from "./components/CreateClient";
import "./styles.scss";

const defaultClient = {
  cid: "",
  name: "",
};

const clientMock = {
  cid: "cd3b0da8f3bf6edb761492e9db1e7585",
  secret: "",
  name: "",
};

export default function Client() {
  const [createClient] = useCreateClientMutation();
  const clients = useGetClientsQuery();
  const [data, setData] = useState<any[]>([]);
  const [draftClient, setDraftClient] = useState<any>(null);

  useEffect(() => {
    clients.data && setData(clients.data);
    setDraftClient(null);
  }, [clients]);

  useEffect(() => {
    if (draftClient) {
      setData((prev: any[]) => [...prev, draftClient]);
    }
  }, [draftClient]);

  return (
    <div className='clientRoot'>
      <h1 className='text-24 mb-16'>Clients</h1>
      <div className='clientsList'>
        <AppLoadingError isError={false} isFetching={clients.isLoading} />
        {data.map((item, index) =>
          item?.cid ? (
            <Card key={index}>
              <ClientItem item={item} />
            </Card>
          ) : (
            <Card key={index}>
              <CreateClient />
            </Card>
          )
        )}

        {!draftClient && (
          <Card>
            <Button className='addClientButton' onClick={() => setDraftClient({ ...defaultClient })}>
              Add client
            </Button>
          </Card>
        )}
      </div>
    </div>
  );
}
