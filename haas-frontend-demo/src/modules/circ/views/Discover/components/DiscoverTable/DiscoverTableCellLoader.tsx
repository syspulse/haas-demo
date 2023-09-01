import { Skeleton } from "primereact/skeleton";

export default function DiscoverTableCellLoader({ isFetching }: { isFetching: boolean }) {
  return isFetching ? <Skeleton width='100%' height='2rem' /> : <>/--/</>;
}
