import { IMonitorScript, typIcons } from "@app/common/models/monitor";
import {
  useDeleteScriptMutation,
  useGetScriptsQuery,
  useUpdateScriptMutation,
} from "@app/common/services/transaction-monitoring";
import { getDate } from "@app/common/utils/getDate";
import { javascript } from "@codemirror/lang-javascript";
import { markdown } from "@codemirror/lang-markdown";
import CodeMirror from "@uiw/react-codemirror";
import MarkdownPreview from "@uiw/react-markdown-preview";
import { Card } from "primereact/card";
import { ConfirmPopup, confirmPopup } from "primereact/confirmpopup";
import { InputText } from "primereact/inputtext";
import { useEffect, useState } from "react";

import "./styles.scss";

export default function Scripts() {
  const scripts = useGetScriptsQuery('');
  const [data, setData] = useState<IMonitorScript[]>([]);
  const [updateScript] = useUpdateScriptMutation();
  const [deleteScript] = useDeleteScriptMutation();
  const [edit, setEdit] = useState<any>(null);

  useEffect(() => {
    if (scripts.isSuccess) {
      setData(scripts.data.scripts);
    }
  }, [scripts]);

  const getHeight = (text: string) => {
    const lines = text.split("\n").length;
    const height = lines > 0 ? lines * 20 + 8 : 24;

    return height > 250 ? 250 : height;
  };

  const confirmDelete = (event: any, script: any) => {
    confirmPopup({
      target: event.currentTarget,
      message: `Do you want to delete ${script.name}?`,
      icon: "pi pi-info-circle",
      acceptClassName: "p-button-danger",
      accept: () => deleteScript(script.id),
    });
  };

  return (
    <div className='scriptsRoot'>
      <h1 className='text-24 mb-16'>Scripts</h1>
      {scripts.isSuccess && (
        <>
          <ConfirmPopup />
          <div className='scriptsList'>
            {data.map((script, index: number) => (
              <Card key={index}>
                <div className='controls'>
                  {edit?.id !== script.id && (
                    <>
                      <i
                        className='pi pi-pencil tooltip'
                        data-pr-tooltip='Edit'
                        data-pr-position='top'
                        onClick={() => setEdit({ ...script, desc: script.desc ? script.desc : "" })}
                      />
                      <i
                        className='pi pi-trash tooltip'
                        data-pr-tooltip='Delete'
                        data-pr-position='top'
                        onClick={(event) => confirmDelete(event, script)}
                      />
                    </>
                  )}
                  {edit?.id === script.id && (
                    <>
                      <i
                        className='pi pi-times tooltip'
                        data-pr-tooltip='Cancel'
                        data-pr-position='top'
                        onClick={() => setEdit(null)}
                      />
                      <i
                        className='pi pi-save tooltip'
                        data-pr-tooltip='Save'
                        data-pr-position='top'
                        onClick={() => {
                          updateScript({ id: edit.id, body: { src: edit.src, desc: edit.desc, name: edit.name } });
                          setEdit(null);
                        }}
                      />
                    </>
                  )}
                </div>
                <p className='keyTitle'>
                  <span>Name:</span>{" "}
                  <span className='valueTitle'>
                    {edit?.id === script.id ? (
                      <>
                        <InputText
                          value={edit.name}
                          onChange={(event) => setEdit((prev: any) => ({ ...prev, name: event.target.value }))}
                        />
                      </>
                    ) : (
                      <>{script.name}</>
                    )}
                  </span>
                </p>
                <p className='keyTitle'>
                  <span>Id:</span> <span className='valueTitle'>{script.id}</span>
                </p>
                <p className='keyTitle'>
                  <span>Ts:</span>
                  <span className='valueTitle '>{getDate(script?.ts0 || Date.now(), "DD MMM YYYY HH:MM")}</span>
                </p>
                <p className='keyTitle'>
                  <span>Type:</span>{" "}
                  {typIcons[script.typ] ? (
                    typIcons[script.typ]
                  ) : (
                    <>
                      <span>script.typ</span>
                    </>
                  )}
                </p>
                {script?.desc && edit?.id !== script.id && (
                  <div className='markdownPreview'>
                    <MarkdownPreview source={script?.desc} wrapperElement={{ "data-color-mode": "light" }} />
                  </div>
                )}

                {edit && edit?.id === script.id && (
                  <>
                    <CodeMirror
                      className='codeInput'
                      value={edit.desc}
                      height='200px'
                      extensions={[markdown()]}
                      onChange={(change) => setEdit((prev: any) => ({ ...prev, desc: change }))}
                    />
                  </>
                )}

                <div className='code'>
                  <CodeMirror
                    className='codeInput'
                    value={edit?.id === script.id ? edit.src : script.src}
                    height={`${getHeight(edit?.id === script.id ? edit.src : script.src)}px`}
                    extensions={[javascript({ jsx: true })]}
                    readOnly={edit?.id !== script.id || !edit}
                    onChange={(change) => setEdit((prev: any) => ({ ...prev, src: change }))}
                  />
                </div>
              </Card>
            ))}
          </div>
        </>
      )}
    </div>
  );
}
