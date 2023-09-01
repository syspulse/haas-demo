import { RootState } from "@app/common/store";
import { useAppSelector } from "@app/common/store/hooks";
import { setNotebookSize } from "@app/common/store/slice/appCommonState";
import { useEffect, useRef } from "react";
import { useDispatch } from "react-redux";
import { NavLink } from "react-router-dom";

import "./styles.scss";

export default function Jupyter() {
  const dispatch = useDispatch();
  const selfRef = useRef<HTMLDivElement>(null);
  const notebook = useAppSelector((state: RootState) => state.common.notebook);

  useEffect(() => {
    if (selfRef.current) {   
      const clientRect = selfRef.current.getBoundingClientRect();

      dispatch(
        setNotebookSize({
          width: `calc(100% - ${clientRect.left + 4}px)`,
          height: `calc(100% - ${clientRect.top + 6}px)`,
        })
      );
    }
  }, []);

  useEffect(
    () => () => {
      dispatch(setNotebookSize({ width: "0px", height: "0px" }));
    },
    []
  );
  return (
    <div className='jupyterRoot' ref={selfRef}>
      {/* {!notebook.token && (
        <>
          <p className='text-24 text-center text-w400 my-24'>
            Token not found,{" "}
            <NavLink className='appLink' to={"/profile"}>
              please provide a Jupyter app token
            </NavLink>{" "}
          </p>
        </>
      )} */}
    </div>
  );
}
