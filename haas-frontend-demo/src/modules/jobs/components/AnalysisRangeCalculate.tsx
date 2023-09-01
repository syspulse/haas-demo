import useDebounce from "@app/common/hooks/useDebounce";
import { ComponentProps } from "@app/common/models/general";
import moment from "moment";
import { Button } from "primereact/button";
import { Calendar } from "primereact/calendar";
import { useEffect, useLayoutEffect, useState } from "react";
import InputRange from "react-input-range";

type AnalysisHistorySelectionProps = {
  range: {
    ts0: number;
    ts1: number;
  };
} & ComponentProps<any>;

export default function AnalysisRangeCalculate({ range, onAction }: AnalysisHistorySelectionProps) {
  const STEP_DAYS = 90;
  const DAY_MS = 86400000;
  const STEP_MS = DAY_MS * STEP_DAYS; // 86400000 === 1 day in MS
  const PRICE_PER_DAY = 0.01; // value in USD
  const TODAY = Math.trunc(Date.now() / 10000000) * 10000000;
  const MIN_YEAR = 2013;

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
  const debouncedRangeSlider = useDebounce(rangeSliderValue, 500);
  const [isApply, setApply] = useState<boolean>(false);
  const [datePickerPos, setDatePickerPos] = useState<any>();

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
    const estimatedCosts = ((debouncedRangeSlider.max - debouncedRangeSlider.min) / STEP_MS) * PRICE_PER_DAY;
    onAction({
      ts0: moment(debouncedRangeSlider.min).format("YYYY-MM-DD"),
      ts1: moment(debouncedRangeSlider.max).format("YYYY-MM-DD"),
      estimatedCosts: Number(
        estimatedCosts.toLocaleString("en-US", { minimumFractionDigits: 0, maximumFractionDigits: 2 })
      ),
    });
  }, [debouncedRangeSlider]);

  return (
    <div className='rootRangeCalculate'>
      <p className='label'>Select date range*</p>
      <div className='sliderWrapper'>
        {isApply && (
          <Button className='applyHistoryRange p-button-secondary p-button-outlined' label='Apply' onClick={() => {}} />
        )}
        <div className='text-12 text-w500 color--gray-700 historySliderLegend'>
          <div
            style={{
              position: "absolute",
              left: `${datePickerPos ? datePickerPos.x : 0}px`,
              top: `${datePickerPos ? datePickerPos.y - 10 : 0}px`,
              transform: `translateX(-50%)`,
              zIndex: 10,
              background: "white",
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
              left: `${datePickerPos ? datePickerPos.x + datePickerPos.width : 0}px`,
              top: `${datePickerPos ? datePickerPos.y - 10 : 0}px`,
              transform: `translateX(50%)`,
              zIndex: 10,
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
                step={DAY_MS}
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
      </div>
    </div>
  );
}
