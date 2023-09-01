import { defaultNS } from "@app/common/locales/locales";
import { useGetUserQuery } from "@app/common/services/user.service";
import { useAppSelector } from "@app/common/store/hooks";
import { logOut } from "@app/common/store/slice/appCommonState";
import { setTheme } from "@app/common/store/slice/appCommonState";
import { Menu } from "primereact/menu";
import { MenuItem } from "primereact/menuitem";
import { useRef, useState } from "react";
import { useTranslation } from "react-i18next";
import { useDispatch } from "react-redux";
import { useNavigate } from "react-router-dom";

import "./styles.scss";
import AppNotifications from "../AppNotifications/AppNotifications";

function AppProfile() {
  const {data} = useGetUserQuery('');
  const { theme } = useAppSelector((state) => state.common);
  const dispatch = useDispatch();
  const { t } = useTranslation(defaultNS, { keyPrefix: "general.appProfile" });
  const navigate = useNavigate();
  const menu = useRef<Menu>(null);
  const [menuActive, setMenuActive] = useState(false);
  const menuItems: MenuItem[] = [
    {
      label: t("editProfile") as string,
      icon: "pi pi-user-edit",
      command: () => navigate("/profile"),
    },
    {
      label: t("logOut") as string,
      icon: "pi pi-user-minus",
      command: () => {
        dispatch(logOut());
      },
    },
  ];

  return (
    <>
      <div className='rootAppProfile'>
        <span className="profileMenu"
          onClick={(e) => {
            menu?.current?.toggle(e);
            setMenuActive(true);
          }}
        >
          <div className='avatar'>
            {data?.avatar && <img src={data?.avatar} alt={data?.email} />}
            {!data?.avatar && <i className='pi pi-user' />}
          </div>
          <p className='email'>{data?.email}</p>
          <span className='pi pi-ellipsis-v' style={{ color: menuActive ? "var(--accent-green)" : "inherit" }} />
        </span>
        <span className='themeSwitcher' onClick={() => dispatch(setTheme(theme === "light" ? "dark" : "light"))}>
          {theme === "light" && <i className='pi pi-sun icon'></i>}
          {theme === "dark" && <i className='pi pi-moon'></i>}
        </span>
        <AppNotifications />
      </div>

      <Menu onHide={() => setMenuActive(false)} popup ref={menu} model={menuItems} />
    </>
  );
}

export default AppProfile;
