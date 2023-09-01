import { appNav } from "@app/App";
import { defaultNS } from "@app/common/locales/locales";
import { useState } from "react";
import { useTranslation } from "react-i18next";
import { NavLink } from "react-router-dom";

import "./styles.scss";

const AppNavigationSidebar = () => {
  const { t } = useTranslation(defaultNS, { keyPrefix: "general.navigation" });
  const [collapsed, setCollapsed] = useState(window.innerWidth < 1024);
  const [nav, setNav] = useState(appNav);

  return (
    <>
      <div className='sidebarRoot' style={{ width: collapsed ? "60px" : "240px" }}>
        <div>
          <ul>
            {nav.map((item, index) => (
              <li key={index}>
                {item.lvl2 ? (
                  <a
                    href=''
                    onClick={(event) => {
                      event.preventDefault();

                      setNav((prev) => {
                        prev[index].expanded = !prev[index].expanded;
                        return [...prev];
                      });
                    }}
                  >
                    <span
                      className='icon tooltip'
                      data-pr-tooltip={collapsed ? t(item.path) : ""}
                      data-pr-position='right'
                    >
                      {item.icon}
                    </span>
                    {!collapsed && t(item.path)}

                    <i className={`expand pi pi-angle-${item.expanded ? "up" : "down"}`}></i>
                  </a>
                ) : (
                  <NavLink to={item.path} className={({ isActive }) => (isActive ? "active" : "")}>
                    <span
                      className='icon tooltip'
                      data-pr-tooltip={collapsed ? t(item.path) : ""}
                      data-pr-position='right'
                    >
                      {item.icon}
                    </span>
                    {!collapsed && t(item.path)}
                  </NavLink>
                )}

                {item.lvl2 && item.expanded && (
                  <ul className='lvl2'>
                    {item.lvl2.map((item2, index2) => (
                      <li key={index2 + "lvl2"}>
                        <NavLink to={item.path} className={({ isActive }) => (isActive ? "active" : "")}>
                          <span
                            className='icon tooltip'
                            data-pr-tooltip={collapsed ? t(item.path) : ""}
                            data-pr-position='right'
                          >
                            {item.icon}
                          </span>
                          {!collapsed && t(item.path)}
                        </NavLink>
                      </li>
                    ))}
                  </ul>
                )}
              </li>
            ))}
          </ul>
        </div>

        <div>
          <ul>
            <li>
              <a
                href='/'
                onClick={(event) => {
                  event.preventDefault();
                  setCollapsed((state) => !state);
                }}
              >
                <span className='icon tooltip' data-pr-tooltip={collapsed ? "Expand" : ""}>
                  <i className={`pi pi-angle-${collapsed ? "right" : "left"}`}></i>
                </span>
                {!collapsed && t("collapse")}
              </a>
            </li>
          </ul>
        </div>
      </div>
    </>
  );
};

export default AppNavigationSidebar;
