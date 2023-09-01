import AppHeader from "@app/common/components/AppHeader/AppHeader";
import AppNavigationSidebar from "@app/common/components/AppNavigationSidebar/AppNavigationSidebar";
import AppSearchTokens from "@app/common/components/AppSearchTokens/AppSearchTokens";
import { RootState } from "@app/common/store";
import { store } from "@app/common/store";
import { useAppSelector } from "@app/common/store/hooks";
import { stateSingletTone } from "@app/index";
import { Outlet } from "react-router-dom";
import CreateLabel from "../labels/components/CreateLabel";
import TokensCreateUpdate from "../token/views/Discover/components/TokensCreateUpdate/TokensCreateUpdate";

function AppRoot() {
  const notebook = useAppSelector((state: RootState) => state.common.notebook);

  stateSingletTone.sync(store);

  return (
    <div className='appRoot'>
      <AppHeader children={<AppSearchTokens />} />
      <div className='appContent'>
        <AppNavigationSidebar />
        <div className='rootOutlet'>
          <Outlet />
        </div>
      </div>

      <CreateLabel />
      <TokensCreateUpdate />

      <iframe
        id='jupyterFrame'
        className='jupyterFrameCross'
        style={{ width: notebook.width, height: notebook.height }}
        src={
          notebook.token
            ? `https://api.demo.hacken.cloud/sparkmagic/lab?token=${notebook.token}`
            : "https://api.demo.hacken.cloud/sparkmagic/lab"
        }
      ></iframe>
    </div>
  );
}

export default AppRoot;
