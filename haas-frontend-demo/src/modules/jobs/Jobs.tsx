import "./styles.scss";

import { Outlet } from "react-router-dom";

export default function Jobs() {
  return (
    <div className='rootJobMonitor'>
      <Outlet />
    </div>
  );
}
