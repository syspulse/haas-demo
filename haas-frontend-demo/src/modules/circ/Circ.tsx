import { useAppSelector } from "@app/common/store/hooks";
import { classNames } from "primereact/utils";
import { useEffect, useState } from "react";
import { Outlet } from "react-router-dom";

function Circ() {
  const { moduleOutletHeaderText } = useAppSelector((state) => state.common);
  const [isHeader, setIsHeader] = useState(false);

  useEffect(() => {
    setIsHeader(Boolean(moduleOutletHeaderText));
  }, [moduleOutletHeaderText]);

  return (
    <>
      <div className='moduleHeader'>
        <h1>{moduleOutletHeaderText}</h1>
      </div>
      <div className={classNames({ moduleOutlet: true, "moduleOutlet--withHeader": isHeader })}>
        <Outlet />
      </div>
    </>
  );
}

export default Circ;
