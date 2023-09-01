import { getDate } from "@app/common/utils/getDate";
import getPercentage from "@app/common/utils/getPercentage";
import parseBigInt from "@app/common/utils/parseBigInt";
import { Card } from "primereact/card";
import { ProgressBar } from "primereact/progressbar";
import { useEffect, useState } from "react";

export default function AnalysisWidgetCircSupply({ activeHistory, token }: any) {
  const [supply, setSupply] = useState({
    total: "",
    valuePer: 0,
  });

  useEffect(() => {
    if (activeHistory.current) {
      setSupply({
        total: parseBigInt(activeHistory.current.supply, token.dcml, "en-US"),
        valuePer: getPercentage(activeHistory.current.supply, activeHistory.current.totalSupply, 0),
      });
    }
  }, [activeHistory]);

  return (
    <Card className='p-card--content-center' style={{ flex: 0.75 }}>
      <p className='text-14 text-w500  color--gray-700'>Circulation Supply ({getDate(activeHistory.current.ts)})</p>
      <p className='text-24 my-6'>{supply.total}</p>
      <ProgressBar value={supply.valuePer} style={{ height: "4px", width: "100%" }} showValue={false}></ProgressBar>
      <p className='flex flex--between-x pt-4'>
        <span className='color--black-100'>{supply.valuePer}%</span>
        <span className='text--gray-500'>100%</span>
      </p>
    </Card>
  );
}
