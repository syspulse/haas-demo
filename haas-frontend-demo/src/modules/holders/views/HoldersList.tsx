import { useGetCircLastQuery } from "@app/common/services/circ.service";
import { useGetHoldersQuery } from "@app/common/services/holders.service";
import { useGetTokenByAddressQuery } from "@app/common/services/token.service";
import Clipboard from "@app/common/utils/Clipboard";
import OpenContract from "@app/common/utils/OpenAddress";
import AnalysisHoldersTable from "@app/modules/circ/views/Analysis/components/AnalysisHoldersTable";
import AnalysisRangeCalculate from "@app/modules/jobs/components/AnalysisRangeCalculate";
import moment from "moment";
import { Card } from "primereact/card";
import { Dropdown } from "primereact/dropdown";
import { Skeleton } from "primereact/skeleton";
import { Tag } from "primereact/tag";
import { useEffect, useState } from "react";
import { useSearchParams } from "react-router-dom";

export default function HoldersList() {
  const [searchParams, setSearchParams] = useSearchParams("");
  const [addr, setAddr] = useState<string>("");
  const [holdersQuery, setHoldersQuery] = useState<string>("");
  const { data, isError, isFetching, isSuccess } = useGetHoldersQuery(holdersQuery, { skip: holdersQuery.length < 43 });
  const token = useGetTokenByAddressQuery(addr, { skip: addr.length < 42 });
  const circLast = useGetCircLastQuery();
  const [holdersCache, setHoldersCache] = useState<any>({});
  const [bucket, setBucket] = useState(0);
  const [from, setFrom] = useState(0);

  const [range, setRange] = useState<any>({
    ts1: null,
    ts0: null,
  });
  const tsOffset = 2629746000;

  useEffect(() => {
    const bid = searchParams.get("bid");

    if (bid) {
      setAddr(bid);

      setSearchParams({});
    } else {
    }
  }, [searchParams]);

  useEffect(() => {
    if (token.data && circLast.data && circLast.data[addr]) {
      if (circLast.data[addr].history && circLast.data[addr].history.length > 0) {
        setRange({
          ts0: circLast.data[addr].history[0].ts - tsOffset,
          ts1: circLast.data[addr].history[0].ts,
        });
      }
    }
  }, [circLast, token, addr]);

  useEffect(() => {
    range.ts0 !== null &&
      addr.length > 41 &&
      setHoldersQuery(
        `${addr}?ts0=${Number(new Date(range.ts0))}&ts1=${Number(new Date(range.ts1))}&from=${from}&size=10`
      );
  }, [range, addr, from]);

  useEffect(() => {
    if (data && data.data && data.data.length > 0) {
      setHoldersCache(
        data.data.reduce((acc: any, item: any) => {
          acc[item.ts] = item;
          return acc;
        }, {})
      );
      setBucket((prev: number) => (prev === 0 ? data.data[0].ts : prev));
    }
  }, [data]);

  return (
    <>
      {addr.length === 0 && !isFetching && (
        <div className='tokenNotSelected'>
          <h3 className='text-18 mb-14'>Token not selected</h3>
          <p>Use search to select a token for holders loading.</p>
          <p>
            To activate search click on search input above or press <strong>"CTRL + /"</strong>
          </p>
        </div>
      )}

      {token.isSuccess && token.data && range.ts0 !== null && (
        <>
          <div className='header'>
            <div className='headerContainer'>
              <div className='icon'>
                {token.isSuccess ? (
                  <img src={token.data.icon} alt={token.data.name} />
                ) : (
                  <Skeleton width='64px' height='64px' />
                )}
              </div>
              <div>
                {token.isSuccess ? (
                  <h1 className='text-24'>{token.data.name}</h1>
                ) : (
                  <Skeleton width='10px' height='24px' />
                )}
                <span className='text-16 text-w500 text--gray-600'>
                  {token.isSuccess ? (
                    <p>{token.data.symbol.toUpperCase()}</p>
                  ) : (
                    <Skeleton width='150px' height='20px' />
                  )}
                </span>
              </div>
            </div>

            <div className="contactInfo">
              <Tag severity='info' className='my-6'>
                <span className='customIcon'>
                  <img src={token.data.icon} alt={token.data.cat[0]} />
                </span>
                {token.data.cat[0]}
              </Tag>
              <p className='text-14 text-w500'>
                <span className='color--primary-500 mx-4'>
                  <Clipboard text={token.data.addr} maxLength={10} />
                </span>
                <span className='color--blue-800 mx-4'>
                  <OpenContract address={token.data.addr} provider='etherscan' tooltip='Open Etherscan' />
                </span>
              </p>
            </div>
          </div>
          <Card className='mb-24'>
            <div className='selectHoldersRange'>
              <AnalysisRangeCalculate
                isActive={true}
                range={range}
                onAction={(value: any) => {
                  setTimeout(() => {
                    setRange({ ts0: Number(new Date(value.ts0)), ts1: Number(new Date(value.ts1)) });
                  });
                }}
              />
            </div>
          </Card>

          {!isFetching && isSuccess && data.data && holdersCache[bucket] && (
            <>
              <div className='holdersSelection'>
                <p className='text-18'>Holders</p>
                <Dropdown
                  value={bucket}
                  optionValue='value'
                  optionLabel='label'
                  options={data.data.map((item: any) => ({
                    value: item.ts,
                    label: moment(item.ts).format("DD MMM YYYY"),
                  }))}
                  placeholder='Select date'
                  onChange={(e) => {
                    setBucket(e.value);
                    setFrom(0);
                  }}
                  tooltip='Select date'
                  tooltipOptions={{ position: "top" }}
                />
              </div>
              <AnalysisHoldersTable
                token={token.data}
                activeHistory={{
                  current: { holders: [...holdersCache[bucket].holders] },
                  ts: holdersCache[bucket].ts,
                }}
                hidePercentage={true}
                hideHeader={true}
                total={holdersCache[bucket].total}
                from={from}
                onAction={(from: number) => {
                  setFrom(from);
                }}
              />
            </>
          )}
        </>
      )}
    </>
  );
}
