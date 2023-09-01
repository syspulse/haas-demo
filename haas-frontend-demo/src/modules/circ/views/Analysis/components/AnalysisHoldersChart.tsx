/**
  Required dependencies

  primereact/chart
  chartjs-plugin-datalabels
  chartjs-plugin-zoom
 */

import { ICircTokenHistory } from "@app/common/models/token";
import { getDate } from "@app/common/utils/getDate";
import parseBigInt from "@app/common/utils/parseBigInt";
import { Card } from "primereact/card";
import { Chart } from "primereact/chart";
import { memo, useEffect, useState } from "react";

type ChartDataSet = {
  labels: string[];
  datasets: {
    label: string;
    yAxisID: string;
    data: number[];
    fill: boolean;
    borderColor: string;
    tension: number;
  }[];
};

const AnalysisHoldersChart = ({ currentHistory }: { currentHistory: ICircTokenHistory[] }) => {
  const [chartDataSet, setChartDataSet] = useState<ChartDataSet>({
    labels: [],
    datasets: [
      {
        label: "Holders",
        data: [],
        yAxisID: "y",
        fill: false,
        borderColor: "#11A683",
        tension: 0,
      },
      {
        label: "Circulation supply",
        data: [],
        yAxisID: "y1",
        fill: false,
        borderColor: "#8e1546",
        tension: 0,
      },
    ],
  });

  useEffect(() => {
    if (currentHistory) {
      const labels: string[] = [];
      const data1: number[] = [];
      const data2: number[] = [];

      currentHistory.forEach((item: ICircTokenHistory) => {
        const date = getDate(item.ts);
        labels.push(date);
        data1.push(item.holdersTotal);
        data2.push(parseBigInt(item.supply, 18));
      });

      setChartDataSet((prev) => {
        return {
          ...prev,
          labels,
          datasets: [
            {
              ...prev.datasets[0],
              data: data1,
            },
            {
              ...prev.datasets[1],
              data: data2,
            },
          ],
        };
      });
    }
  }, [currentHistory]);

  return (
    <Card className='p-card--large'>
      <p className='text-20 mb-24'>Holders / Supply</p>
      <Chart
        className='basis'
        type='line'
        data={chartDataSet}
        options={{
          responsive: true,
          interaction: {
            mode: "index",
            intersect: false,
          },
          stacked: false,
          animation: {
            duration: 0,
          },
          plugins: {
            datalabels: false,
            legend: {
              display: true,
              scaleSteps: 100000,
            },
            zoom: {
              pan: {
                enabled: true,
                mode: "x",
                modifierKey: "ctrl",
              },
              zoom: {
                drag: {
                  enabled: true,
                },
                wheel: {
                  enabled: true,
                },
                mode: "x",
              },
            },
          },
          elements: {
            point: {
              radius: 2,
            },
            line: {
              borderWidth: 2,
            },
            scale: {
              border: {
                width: 10,
              },
            },
          },
          scales: {
            x: {
              offset: true,
              grid: {
                drawOnChartArea: true,
                drawTicks: true,
                borderDash: [6, 2],
                offsetGridLines: true,
              },
              ticks: {
                maxTicksLimit: 15,
              },
            },
            y: {
              offset: true,
              type: "linear",
              display: true,
              position: "right",
              min: 0,

              ticks: {
                maxTicksLimit: 2,
                labelOffset: 10,
              },
              grid: {
                drawOnChartArea: true,
                drawTicks: true,
                borderDash: [6, 2],
                offsetGridLines: true,
              },
            },
            y1: {
              offset: true,
              type: "linear",
              display: true,
              position: "left",
              min: 0,
              ticks: {
                maxTicksLimit: 2,
                labelOffset: 10,
              },
              grid: {
                drawTicks: true,
                drawOnChartArea: true,
              },
            },
          },
        }}
      />
    </Card>
  );
};

export default memo(AnalysisHoldersChart);
