import { Card } from "primereact/card";
import { Tag } from "primereact/tag";

const AnalysisWidgetChange = ({ title, value, vector }: any) => {
  return (
    <Card className='p-card--content-center'>
      <p className='text-14 text-w500 color--gray-700'>{title}</p>
      <p className='text-24 my-6'>{value}</p>
      {vector !== null && (
        <Tag
          severity={vector === 0 ? "info" : vector > 0 ? "success" : "danger"}
          icon={"pi pi-caret-" + (vector === 0 ? "" : vector > 0 ? "up" : "down")}
          value={vector.toFixed(2) + "%"}
        ></Tag>
      )}
    </Card>
  );
};

export default AnalysisWidgetChange;
