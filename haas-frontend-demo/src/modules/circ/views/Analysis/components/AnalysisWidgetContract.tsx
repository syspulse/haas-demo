import { IToken } from "@app/common/models/token";
import Clipboard from "@app/common/utils/Clipboard";
import OpenContract from "@app/common/utils/OpenAddress";
import { Card } from "primereact/card";
import { Tag } from "primereact/tag";

const AnalysisWidgetContract = ({ token }: { token: IToken | any }) => {
  return (
    <Card className='p-card--content-center'>
      <p className='text-14 text-w500 color--gray-700'>Contract</p>
      <Tag severity='info' className='my-6'>
        <span className='customIcon'>
          <img src={token.icon} alt={token.cat[0]} />
        </span>
        {token.cat[0]}
      </Tag>
      <p className='text-14 text-w500'>
        <span className="color--primary-500 mx-4"><Clipboard text={token.addr} maxLength={10} /></span>
        <span className="color--blue-800 mx-4"><OpenContract address={token.addr} provider='etherscan' tooltip="Open Etherscan" /></span>
      </p>
    </Card>
  );
};

export default AnalysisWidgetContract;
