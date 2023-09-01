import { ReactComponent as HaasLogo } from "@assets/icons/haasLogo.svg";
import { Outlet } from "react-router-dom";

import "./style.scss";

function Auth() {
  return (
    <div className='appRoot'>
      <div className='appContent'>
        <div className='rootOutlet'>
          <HaasLogo className='authHaasLogo' />
          <Outlet />
        </div>
      </div>
    </div>
  );
}

export default Auth;
