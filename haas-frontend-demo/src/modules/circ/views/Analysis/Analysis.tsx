import { useGetCircIdByTokenIdQuery, useGetCircLastQuery } from "@app/common/services/circ.service";
import { useGetTokenByAddressQuery } from "@app/common/services/token.service";
import { CommonNotification, setModuleOutletHeaderText, setNotification } from "@app/common/store/slice/appCommonState";
import getPercentage, { formatPercentage } from "@app/common/utils/getPercentage";
import parseBigInt from "@app/common/utils/parseBigInt";
import priceFormat from "@app/common/utils/priceFormat";
// Register chart label
import { Chart } from "chart.js";
import ChartDataLabels from "chartjs-plugin-datalabels";
import zoomPlugin from "chartjs-plugin-zoom";
import { Skeleton } from "primereact/skeleton";
import { Tag } from "primereact/tag";
import { useEffect, useState } from "react";
import { useDispatch } from "react-redux";
import { useParams } from "react-router";

import AnalysisCategories from "./components/AnalysisCategories";
import AnalysisHistoryRangeSlider from "./components/HistorySlider/AnalysisHistoryRangeSlider";
import AnalysisHoldersChart from "./components/AnalysisHoldersChart";
import AnalysisHoldersTable from "./components/AnalysisHoldersTable";
import AnalysisHoldersWidget from "./components/AnalysisHoldersWidget";
import AnalysisNetworks from "./components/AnalysisNetworks";
import AnalysisWidgetBlock from "./components/AnalysisWidgetBlock";
import AnalysisWidgetChange from "./components/AnalysisWidgetChange";
import AnalysisWidgetCircSupply from "./components/AnalysisWidgetCircSupply";
import AnalysisWidgetCircTotalSupply from "./components/AnalysisWidgetCircTotalSupply";
import AnalysisWidgetContract from "./components/AnalysisWidgetContract";
import AnalysisWidgetSkeleton from "./components/AnalysisWidgetSkeleton";
import "./styles.scss";
import { Button } from "primereact/button";
import { useNavigate } from "react-router-dom";

Chart.register(ChartDataLabels);
Chart.register(zoomPlugin);

export default function CircAnalysis() {
  const tsOffset = 2629746000;
  const [range, setRange] = useState<any>({
    ts1: null,
    ts0: null,
  });

  const dispatch = useDispatch();
  const { entityId }: { entityId: string } = useParams() as { entityId: string };

  const token = useGetTokenByAddressQuery(entityId, { skip: !entityId });
  const circ = useGetCircIdByTokenIdQuery(
    {
      id: entityId,
      ts0: range.ts0,
      ts1: range.ts1 + 86400000,
    },
    {
      skip: !Boolean(range.ts0) || !entityId,
    }
  );

  const [activeHistory, setActiveHistory] = useState<any>({
    current: null,
    prev: null,
    list: [],
  });
  const circLast = useGetCircLastQuery();
  const navigate = useNavigate();

  useEffect(() => {
    dispatch(setModuleOutletHeaderText(`Circulation Supply`));
  }, []);

  useEffect(() => {
    if (token.data && circLast.data && circLast.data[entityId]) {
      if (circLast.data[entityId].history && circLast.data[entityId].history.length > 0) {
        setRange({
          ts0: circLast.data[entityId].history[0].ts - tsOffset,
          ts1: circLast.data[entityId].history[0].ts,
        });
      }
    }
  }, [circLast, token]);

  useEffect(() => {
    if (circ.data) {
      setActiveHistory({
        list: circ.data.history.length > 0 ? circ.data.history : [],
        current: circ.data.history.length > 0 ? circ.data.history[circ.data.history.length - 1] : null,
        prev: circ.data.history[circ.data.history.length - 2]
          ? circ.data.history[circ.data.history.length - 2]
          : circ.data.history[circ.data.history.length - 1],
      });
    }

    if (circ.data && !circ.data.isFetching && circ.data.history && circ.data.history.length === 0) {
      dispatch(setNotification(JSON.parse(JSON.stringify(new CommonNotification("toast", "warn", {}, "noHistory")))));
    }
  }, [circ]);

  return (
    <div className='rootAnalysis'>
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
            {token.isSuccess ? <h1 className='text-24'>{token.data.name}</h1> : <Skeleton width='10px' height='24px' />}
            <span className='text-16 text-w500 text--gray-600'>
              {token.isSuccess ? <p>{token.data.symbol.toUpperCase()}</p> : <Skeleton width='150px' height='20px' />}
            </span>
          </div>
          {!(circLast.isSuccess && (circ.isError || !circLast.data[entityId]) && token.isSuccess) && (
            <Tag className='newJobBtn' icon='pi pi-plus' value={"Calculate"} severity='info' onClick={() => navigate(`/job?bid=${entityId}`)} />
          )}
        </div>

        {token.isSuccess && token.data.cat.length > 0 && (
          <div className='taggedCategories'>
            <p className='text-14 mb-8'>Tagged by {token.data.cat.length} categories </p>
            <div className='list'>
              {token.data.cat.map((item: string, index: number) => (
                <Tag severity='info' key={item + index} value={item} />
              ))}
            </div>
          </div>
        )}
      </div>

      {circLast.isSuccess &&
        (circ.isError || !circLast.data[entityId] || circLast.data[entityId].history.length === 0) &&
        token.isSuccess && (
          <div className='noCirc'>
            <p className='title'>No Circulation supply</p>
            <p className='description'>Create new job to calculate circulation supply</p>
            <Button className='p-button-outlined' onClick={() => navigate(`/job?bid=${entityId}`)}>Calculate</Button>
          </div>
        )}

      {!circ.isError &&
        token.isSuccess &&
        circLast.isSuccess &&
        circLast.data[entityId] &&
        circLast.data[entityId].history.length > 0 && (
          <>
            <div className='grid'>
              <AnalysisWidgetBlock>
                <AnalysisWidgetSkeleton isActive={token.isSuccess} height='125px'>
                  <AnalysisWidgetContract token={token.data} />
                </AnalysisWidgetSkeleton>
                <AnalysisWidgetSkeleton isActive={Boolean(token?.isSuccess && activeHistory?.current)} height='125px'>
                  <AnalysisWidgetCircTotalSupply activeHistory={activeHistory} token={token} />
                </AnalysisWidgetSkeleton>
                <AnalysisWidgetSkeleton isActive={Boolean(token?.isSuccess && activeHistory?.current)} height='125px'>
                  <AnalysisWidgetChange
                    title='Inflation'
                    value={formatPercentage(activeHistory?.current?.inflation)}
                    vector={0}
                  />
                </AnalysisWidgetSkeleton>
                <AnalysisWidgetSkeleton isActive={Boolean(token?.isSuccess && activeHistory?.current)} height='125px'>
                  <AnalysisWidgetChange
                    title='Price'
                    value={priceFormat(activeHistory?.current?.price)}
                    vector={getPercentage(
                      activeHistory?.current?.price - activeHistory?.prev?.price,
                      activeHistory?.current?.price
                    )}
                  />
                </AnalysisWidgetSkeleton>
                <AnalysisWidgetSkeleton isActive={Boolean(token?.isSuccess && activeHistory?.current)} height='125px'>
                  <AnalysisWidgetChange
                    title='Market Cap'
                    value={priceFormat(
                      activeHistory?.current?.price * parseBigInt<number>(activeHistory?.current?.supply, 18)
                    )}
                    vector={getPercentage(
                      activeHistory?.current?.price * parseBigInt<number>(activeHistory?.current?.supply, 18) -
                        activeHistory?.prev?.price * parseBigInt<number>(activeHistory?.prev?.supply, 18),
                      activeHistory?.current?.price * parseBigInt<number>(activeHistory?.current?.supply, 18)
                    )}
                  />
                </AnalysisWidgetSkeleton>
              </AnalysisWidgetBlock>
              <AnalysisWidgetBlock>
                <AnalysisWidgetSkeleton isActive={Boolean(token.isSuccess && activeHistory.current)} height='125px'>
                  <AnalysisWidgetCircSupply activeHistory={activeHistory} token={token} />
                </AnalysisWidgetSkeleton>
                <AnalysisWidgetSkeleton
                  isActive={Boolean(token.isSuccess && circ.isSuccess && circLast.isSuccess && !circ.isFetching)}
                  height='125px'
                >
                  <AnalysisHistoryRangeSlider
                    history={circ?.data?.history || []}
                    range={range}
                    tokenId={token.data?.addr}
                    isActive={true}
                    onAction={(dr: any) =>
                      setActiveHistory((prev: any) => {
                        if (!dr.list) {
                          return { ...prev, current: dr.current, prev: dr.prev };
                        }

                        return dr;
                      })
                    }
                  />
                </AnalysisWidgetSkeleton>
              </AnalysisWidgetBlock>
              <AnalysisWidgetBlock>
                <AnalysisWidgetSkeleton
                  isActive={Boolean(token.isSuccess && activeHistory.current && !circ.isFetching)}
                  height='674px'
                >
                  <AnalysisCategories activeHistory={activeHistory} token={token.data} />
                </AnalysisWidgetSkeleton>
              </AnalysisWidgetBlock>
              <AnalysisWidgetBlock>
                <AnalysisWidgetSkeleton
                  isActive={Boolean(token.isSuccess && activeHistory.current && !circ.isFetching)}
                  height='674px'
                >
                  <AnalysisNetworks activeHistory={activeHistory} token={token.data} />
                </AnalysisWidgetSkeleton>
              </AnalysisWidgetBlock>

              <AnalysisWidgetBlock orientation='vertical' maxWidth='625px'>
                <AnalysisWidgetSkeleton
                  isActive={Boolean(token.isSuccess && activeHistory.current && !circ.isFetching)}
                  height='674px'
                >
                  <AnalysisHoldersWidget activeHistory={activeHistory} />
                  <AnalysisHoldersChart currentHistory={activeHistory.list} />
                </AnalysisWidgetSkeleton>
              </AnalysisWidgetBlock>

              <AnalysisWidgetSkeleton
                isActive={Boolean(token.isSuccess && activeHistory.current && !circ.isFetching)}
                height='674px'
              >
                <AnalysisHoldersTable activeHistory={activeHistory} token={token} />
              </AnalysisWidgetSkeleton>
            </div>
          </>
        )}
    </div>
  );
}
