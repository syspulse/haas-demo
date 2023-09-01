import useDebounce from "@app/common/hooks/useDebounce";
import { ComponentProps } from "@app/common/models/general";
import { ICircTokenHistory } from "@app/common/models/token";
import { useGetCircIdByTokenIdQuery } from "@app/common/services/circ.service";
import moment from "moment";
import { Button } from "primereact/button";
import { Calendar } from "primereact/calendar";
import { Card } from "primereact/card";
import { Dropdown } from "primereact/dropdown";
import { Tag } from "primereact/tag";
import { useEffect, useLayoutEffect, useState } from "react";
import InputRange from "react-input-range";
import "./styles.scss";

type AnalysisHistorySelectionProps = {
  history: ICircTokenHistory[];
  range: {
    ts0: number;
    ts1: number;
  };
  tokenId: string;
} & ComponentProps<any>;

export default function AnalysisHistoryRangeSlider({
  history,
  range,
  tokenId,
  onAction,
}: AnalysisHistorySelectionProps) {
  const TODAY = Math.trunc(Date.now() / 10000000) * 10000000;
  const MIN_YEAR = 2013;
  const MONTH_MS = 86400000;
  const defaultYearsRange = 4;
  const defaultStartYear = new Date(TODAY).getFullYear();
  const MAX_YEAR = defaultStartYear + 2;
  const aggregateYears = (range: number, defaultStartYear: number) =>
    new Array(range)
      .fill(defaultStartYear)
      .map((year, index) => year - index)
      .reverse();

  const [years, setYears] = useState<number[]>(aggregateYears(defaultYearsRange, defaultStartYear + 1));
  const [rangeSliderValue, setRangeSliderValue] = useState<any>({ min: range.ts0, max: range.ts1 });
  const [historySelection, setHistorySelection] = useState({ list: history, active: history[history.length - 1].ts });
  const debouncedRangeSlider = useDebounce(rangeSliderValue, 500);
  const [rangeSliderDate, setRangeSliderDate] = useState<number>(historySelection.active);
  const debouncedRangeSliderDate = useDebounce(rangeSliderDate, 500);
  const [isApply, setApply] = useState<boolean>(false);
  const [datePickerPos, setDatePickerPos] = useState<any>();

  const circ = useGetCircIdByTokenIdQuery(
    {
      id: tokenId,
      ts0: moment(debouncedRangeSlider.min).utc(true).valueOf(),
      ts1: moment(debouncedRangeSlider.max).utc(true).valueOf() + 43200000 * 2,
    },
    {
      skip: debouncedRangeSlider.min === range.ts0 && debouncedRangeSlider.max === range.ts1,
    }
  );

  useEffect(() => {
    if (circ.data) {
      const isHistory = circ.data.history.length > 0;
      const list = isHistory ? circ.data.history : [];
      const active = isHistory ? circ.data.history[circ.data.history.length - 1].ts : null;
      setHistorySelection({ list, active });
    }
  }, [circ]);

  useEffect(() => {
    setApply(circ.data && circ.data.history.length > 0);
  }, [circ.data]);

  const apply = (history: any, ts?: number) => {
    if (ts) {
      const index = history.findIndex((item: any) => item.ts === ts);
      const prevIndex = index > 0 ? index - 1 : index;
      const current = history[index];
      const prev = history[prevIndex];

      onAction({
        current,
        prev,
      });
    } else {
      const current = history[history.length - 1];
      const prev = history.length > 1 ? history[history.length - 2] : history[history.length - 1];

      setApply(false);
      onAction({
        list: history,
        current,
        prev,
      });
    }
  };

  useLayoutEffect(() => {
    setTimeout(() => {
      const el: any = window.document.getElementsByClassName("input-range__track--active")[0];
      const rect = el.getBoundingClientRect();

      setDatePickerPos({ x: el.offsetLeft, y: el.offsetTop, width: rect.width });
    });
  }, []);

  useEffect(() => {
    const minDate = Number(new Date(years[0] + ""));
    const maxYear = new Date(rangeSliderValue.max).getFullYear();
    const newMin = years[0] - 1;
    const newMax = maxYear + 1;

    if (rangeSliderValue.min <= minDate && newMin > MIN_YEAR) {
      setYears((state) => [newMin, ...state]);
    }

    if (maxYear >= years[years.length - 1] && MAX_YEAR > newMax) {
      setYears((state) => [...state, newMax]);
    }
  }, [rangeSliderValue]);

  useEffect(() => {
    let item: any = historySelection.list.find(
      (item: any) => moment(item.ts).format("DD MM YYYY") === moment(debouncedRangeSliderDate).format("DD MM YYYY")
    );

    if (!item) {
      debouncedRangeSliderDate < historySelection.list[0].ts && (item = historySelection.list[0]);
      debouncedRangeSliderDate < historySelection.list[historySelection.list.length - 1].ts &&
        (item = historySelection.list[historySelection.list.length - 1]);
    }

    if (item) {
      setHistorySelection((prev: any) => ({ ...prev, active: item.ts }));
      apply(historySelection.list, item.ts);
    }
  }, [debouncedRangeSliderDate]);

  return (
    <Card className='text-center sticky'>
      {isApply && (
        <Button
          className='applyHistoryRange p-button-secondary p-button-outlined'
          label='Apply'
          onClick={() => {
            apply(circ.data.history);
          }}
        />
      )}
      <div className='historyHeader'>
        <span>History</span>
        {historySelection.list.length > 0 ? (
          <Dropdown
            optionLabel='ts'
            optionValue='ts'
            valueTemplate={(item) => <>{item ? moment(item.ts).utc().format("DD MMM YYYY") : <>/--/</>}</>}
            itemTemplate={(item) => <>{item && moment(item.ts).utc().format("DD MMM YYYY")}</>}
            value={historySelection.active}
            options={historySelection.list}
            onChange={(param) => setRangeSliderDate(param.value)}
            placeholder='Select Language'
          />
        ) : (
          <Tag severity='warning' value='History is empty' />
        )}
      </div>
      <div className='text-12 text-w500 color--gray-700 historySliderLegend'>
        <div
          style={{
            position: "absolute",
            left: `${datePickerPos ? datePickerPos.x - 10 : 0}px`,
            top: `${datePickerPos ? datePickerPos.y + 46 : 0}px`,
          }}
        >
          <Calendar
            value={new Date(rangeSliderValue.min)}
            onChange={(e) => {
              setRangeSliderValue((prev: any) => ({ ...prev, min: Number(e.value) }));
              setTimeout(() => {
                const el: any = window.document.getElementsByClassName("input-range__track--active")[0];
                const rect = el.getBoundingClientRect();

                setDatePickerPos({ x: el.offsetLeft, y: el.offsetTop, width: rect.width });
              }, 100);
            }}
          />
        </div>

        <div
          style={{
            position: "absolute",
            left: `${datePickerPos ? datePickerPos.x + datePickerPos.width + 50 : 0}px`,
            top: `${datePickerPos ? datePickerPos.y + 46 : 0}px`,
          }}
        >
          <Calendar
            value={new Date(rangeSliderValue.max)}
            onChange={(e) => {
              setRangeSliderValue((prev: any) => ({ ...prev, max: Number(e.value) }));
              setTimeout(() => {
                const el: any = window.document.getElementsByClassName("input-range__track--active")[0];
                const rect = el.getBoundingClientRect();

                setDatePickerPos({ x: el.offsetLeft, y: el.offsetTop, width: rect.width });
              }, 100);
            }}
          />
        </div>
      </div>
      <div className='historySliderWrapper'>
        <div className='historySliderNav'>
          {years[0] - 1 > MIN_YEAR && (
            <i
              className='pi pi-plus-circle tooltip'
              data-pr-tooltip='Add year'
              data-pr-position='top'
              onClick={() => setYears((prev: any) => (prev[0] - 1 > MIN_YEAR ? [prev[0] - 1, ...prev] : [...prev]))}
            />
          )}
          {years[0] !== new Date(rangeSliderValue.min).getFullYear() && (
            <i
              className='pi pi-minus-circle tooltip'
              data-pr-tooltip='Reduce year'
              data-pr-position='bottom'
              onClick={() =>
                setYears((prev: any) => {
                  prev.splice(0, 1);

                  return [...prev];
                })
              }
            />
          )}
        </div>
        <div className='historySlider'>
          <div className='selectRangeSlider'>
            <InputRange
              maxValue={moment(years[years.length - 1] + "")
                .utc(true)
                .valueOf()}
              minValue={moment(years[0] + "")
                .utc(true)
                .valueOf()}
              value={rangeSliderValue}
              allowSameValues={false}
              step={MONTH_MS}
              draggableTrack={true}
              onChange={(value: any) => {
                const el: any = window.document.getElementsByClassName("input-range__track--active")[0];
                const rect = el.getBoundingClientRect();
                const maxDate = Number(new Date(MAX_YEAR + ""));
                const minDate = Number(new Date(MIN_YEAR + ""));

                if (maxDate >= value.max && value.min >= minDate) {
                  setDatePickerPos({ x: el.offsetLeft, y: el.offsetTop, width: rect.width });

                  setRangeSliderValue(value);
                }
              }}
            />
          </div>
          {historySelection.list.length > 0 && (
            <div
              className='selectedDateSlider'
              style={{
                left: `${datePickerPos ? datePickerPos.x + 16 : 0}px`,
                width: `${datePickerPos ? datePickerPos.width : 0}px`,
              }}
            >
              <InputRange
                maxValue={rangeSliderValue.max}
                minValue={rangeSliderValue.min}
                value={rangeSliderDate}
                onChange={(event: any) => setRangeSliderDate(event)}
              />
            </div>
          )}
          <div className='yearsRuler'>
            {years.map((year: number) => (
              <div key={year}>{year}</div>
            ))}
          </div>
        </div>
        <div className='historySliderNav'>
          {MAX_YEAR > years[years.length] && (
            <i
              className='pi pi-plus-circle tooltip'
              data-pr-tooltip='Add year'
              data-pr-position='top'
              onClick={() => setYears((prev: any) => [...prev, prev[prev.length - 1] + 1])}
            />
          )}
          {years[years.length - 1] - 1 !== new Date(rangeSliderValue.min).getFullYear() && (
            <i
              className='pi pi-minus-circle tooltip'
              data-pr-tooltip='Reduce year'
              data-pr-position='bottom'
              onClick={() =>
                setYears((prev: any) => {
                  prev.splice(prev.length - 1, 1);
                  return [...prev];
                })
              }
            />
          )}
        </div>
      </div>
    </Card>
  );
}
