import { ReactNode } from "react";
import { Navigate, Outlet } from "react-router-dom";

type AccessGuardProps = {
  isAllowed: boolean;
  redirectPath?: string;
  children?: ReactNode;
};

function AccessGuard({
  isAllowed,
  redirectPath = "/login",
  children,
}: AccessGuardProps) {
  if (!isAllowed) {
    return <Navigate to={redirectPath} replace />;
  }

  return <>{children ? children : <Outlet />}</>;
}

export default AccessGuard;
