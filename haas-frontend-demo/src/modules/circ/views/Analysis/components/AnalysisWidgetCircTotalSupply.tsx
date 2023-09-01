import parseBigInt from "@app/common/utils/parseBigInt";
import { Card } from "primereact/card";

const AnalysisWidgetCircTotalSupply = ({ activeHistory, token }: any) => {
  return (
    <Card className='p-card--content-center'>
      <p className='text-14 text-w500  color--gray-700'>Total Supply</p>
      <p className='text-24 my-6'>{parseBigInt(activeHistory.current.totalSupply, token.dcml, "en-US")}</p>
    </Card>
  );
};

export default AnalysisWidgetCircTotalSupply;
