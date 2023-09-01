import { ComponentProps } from "@app/common/models/general";

type MonitoringEmptyFilteringProps = ComponentProps<any> & {
  filter: string[];
};

export default function MonitoringEmptyFiltering({ isActive, filter }: MonitoringEmptyFilteringProps) {
  if (!isActive) {
    return <></>;
  }

  return (
    <div className='transactionEmptyState'>
      <p className='text-20'>No items for filters: {filter.join(', ')} monitors...</p>
    </div>
  );
}
