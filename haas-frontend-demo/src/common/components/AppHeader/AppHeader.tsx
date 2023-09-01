import "./styles.scss";

import { ReactNode } from "react";
import { ReactComponent as HaasLogo } from "@assets/icons/haasLogo.svg";
import AppProfile from "@app/common/components/AppProfile/AppProfile";
import { useNavigate } from "react-router-dom";

type AppHeaderProps = {
  children?: ReactNode;
};

function AppHeader({ children }: AppHeaderProps) {
  const navigate = useNavigate();
  return (
    <div className="appHeaderRoot">
      <div className="logo">
        <HaasLogo onClick={() => navigate("/")} />
      </div>

      {children}
      <AppProfile />
    </div>
  );
}

export default AppHeader;
