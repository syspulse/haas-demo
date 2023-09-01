import { Skeleton } from "primereact/skeleton";
import { ReactNode } from "react";

export default function AnalysisWidgetSkeleton({
  children,
  isActive,
  height,
}: {
  children: ReactNode;
  isActive: boolean;
  height: string;
}) {
  if (isActive) {
    return <>{children}</>;
  }
  return <Skeleton className='p-card' height={height} />;
}
