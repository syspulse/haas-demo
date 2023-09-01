import { Outlet } from "react-router-dom";
import "./styles.scss";

export default function Holders() {
  return (
    <div className='rootJobMonitor'>
        
      <Outlet />
    </div>
  );
}
