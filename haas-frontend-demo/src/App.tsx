import AccessGuard from "@app/common/components/AccessGuard";
import i18n, { defaultNS } from "@app/common/locales/locales";
import { INav } from "@app/common/models/general";
import { useAppSelector } from "@app/common/store/hooks";
import Auth from "@app/modules/auth/Auth";
import AuthSignUp from "@app/modules/auth/Enroll/Enroll";
import AuthLogin from "@app/modules/auth/Login/Login";
import Circ from "@app/modules/circ/Circ";
import CircAnalysis from "@app/modules/circ/views/Analysis/Analysis";
import CircDiscover from "@app/modules/circ/views/Discover/Discover";
import Monitoring from "@app/modules/monitoring/Monitoring";
import Profile from "@app/modules/profile/Profile";
import AppPageNotFound from "@app/modules/root/AppPageNotFound";
import AppRoot from "@app/modules/root/AppRoot";
import { ReactComponent as JupyterIcon } from "@assets/icons/jupyter.svg";
import { Toast } from "primereact/toast";
import { Tooltip } from "primereact/tooltip";
import { useEffect, useRef } from "react";
import { useTranslation } from "react-i18next";
import { Navigate, Route, Routes, useLocation } from "react-router-dom";

import Abi from "./modules/abi/Abi";
import Callback from "./modules/auth/Callback";
import Client from "./modules/client/Client";
import Jupyter from "./modules/jupyter/Jupyter";
import Labels from "./modules/labels/Labels";
import Scripts from "./modules/scripts/Scripts";
import Token from "./modules/token/Token";
import TokenDiscover from "./modules/token/views/Discover/Discover";
import Jobs from "./modules/jobs/Jobs";
import Job from "./modules/jobs/views/Job";
import JobsList from "./modules/jobs/views/JobsList";
import Holders from "./modules/holders/Holders";
import HoldersList from "./modules/holders/views/HoldersList";

export const appNav: INav[] = [
  {
    path: "circulating",
    icon: <i className='pi pi-chart-pie'></i>,
    template: (
      <Route path='circulating' element={<Circ />} key='circulating'>
        <Route index element={<CircDiscover />} />
        <Route path=':entityId' element={<CircAnalysis />} />
      </Route>
    ),
  },
  {
    path: "holders",
    icon: <i className='pi pi-briefcase'></i>,
    template: (
      <Route path='holders' element={<Holders />} key='holders'>
        <Route index element={<HoldersList />} />
      </Route>
    ),
  },

  {
    path: "job",
    icon: <i className='pi pi-box'></i>,
    template: (
      <Route path='job' element={<Jobs />} key='job'>
        <Route index element={<JobsList />} />
        <Route path=':jobId' element={<Job />}></Route>
      </Route>
    ),
  },
  {
    path: "monitoring",
    icon: <i className='pi pi-angle-double-right'></i>,
    template: <Route path='monitoring' element={<Monitoring />} key='monitoring' />,
  },
  {
    path: "token",
    icon: <i className='pi pi-bitcoin'></i>,
    template: (
      <Route path='token' element={<Token />} key='token'>
        <Route index element={<TokenDiscover />} />
      </Route>
    ),
  },
  {
    path: "labels",
    icon: <i className='pi pi-tags'></i>,
    template: <Route path='labels' element={<Labels />} key='labels' />,
  },
  {
    path: "scripts",
    icon: <i className='pi pi-book'></i>,
    template: <Route path='scripts' element={<Scripts />} key='scripts' />,
  },
  {
    path: "abi",
    icon: <i className='pi pi-book'></i>,
    template: <Route path='abi' element={<Abi />} key='abi' />,
    // lvl2: [
    //   {
    //     path: "abi2",
    //     icon: <i className='pi pi-book'></i>,
    //     template: <Route path='abi' element={<Scripts />} key='scripts' />,
    //   },
    // ],
    // expanded: false,
  },
  {
    path: "jupyter",
    icon: <JupyterIcon />,
    template: <Route path='jupyter' element={<Jupyter />} key='jupyter' />,
  },
  {
    path: "client",
    icon: <i className='pi pi-key'></i>,
    template: <Route path='client' element={<Client />} key='client' />,
  },
];

function App() {
  const { t } = useTranslation(defaultNS, { keyPrefix: "general.messages" });
  const { isAuthorized, latestNotification, enroll, theme, language } = useAppSelector((state) => state.common);
  let location = useLocation();
  const toast = useRef<Toast>(null);
  const tooltip = useRef<any>(null);

  useEffect(() => {
    i18n.changeLanguage(language);
  }, [language]);

  useEffect(() => {
    if (latestNotification && latestNotification.stamp + 200 > Date.now()) {
      if (latestNotification.type === "toast") {
        toast?.current?.show({
          severity: latestNotification.severity,
          summary: t(latestNotification.severity).includes("general")
            ? latestNotification.severity
            : t(latestNotification.severity),
          detail: t(latestNotification.message).includes("general")
            ? latestNotification.message
            : t(latestNotification.message),
          closable: true,
        });
      }
    }
  }, [latestNotification, t]);

  useEffect(() => {
    setTimeout(() => tooltip.current && tooltip.current.updateTargetEvents(), 1000);

    window.addEventListener("click", () => {
      setTimeout(() => tooltip.current && tooltip.current.updateTargetEvents(), 300);
    });
  }, [location]);

  useEffect(() => {
    document.documentElement.setAttribute("data-theme", theme);
  }, [theme]);

  return (
    <>
      <Toast ref={toast} position='bottom-right' appendTo={document.body} />
      <Tooltip ref={tooltip} target='.tooltip' appendTo={document.body} />
      <Routes>
        <Route path='twitter' element={<Callback />} />
        <Route path='/auth' element={<AccessGuard isAllowed={!isAuthorized} redirectPath='/' children={<Auth />} />}>
          <Route index element={enroll ? <Navigate to='sign-up' /> : <Navigate to='login' />} />
          <Route path='login' element={enroll ? <Navigate to='/auth/sign-up' replace /> : <AuthLogin />} />
          <Route path='sign-up' element={!enroll ? <Navigate to='/auth/login' replace /> : <AuthSignUp />} />
        </Route>
        <Route element={<AccessGuard isAllowed={isAuthorized} redirectPath='/auth' children={<AppRoot />} />}>
          <Route index element={<Navigate to='/circulating' replace />} />
          {appNav.map((item) => item.template)}
          <Route path='profile' element={<Profile />} />
        </Route>
        <Route path='*' element={<AppPageNotFound />} />
      </Routes>
    </>
  );
}

export default App;
