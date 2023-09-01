import { getDate } from "@app/common/utils/getDate";
import getPercentage from "@app/common/utils/getPercentage";
import parseBigInt from "@app/common/utils/parseBigInt";
import { Card } from "primereact/card";
import { Chart } from "primereact/chart";
import { Tag } from "primereact/tag";
import { memo, useEffect, useState } from "react";

const AnalysisCategories = ({ activeHistory, token }: any) => {
  const colors = [
    "#084f3f",
    "#b54c00",
    "#004a80",
    "#007d34",
    "#007787",
    "#614cff",
    "#8e1546",
    "#6b2828",
    "#998000",
    "#477900",
    "#007e0f",
    "#12151d",
    "#063c2f",
    "#002d4d",
    "#004650",
    "#4b33ff",
    "#5f0066",
    "#6c1437",
    "#511e1e",
    "#6b2d00",
    "#665500",
    "#294700",
    "#004609",
    "#004a1f",
    "#373a41",
  ];
  const [categories, setCategories] = useState([]);

  useEffect(() => {
    const categoryHash: any = {};
    const total = activeHistory.current.category.reduce((acc: number, item: any) => (acc += item.value), 0);

    [...activeHistory.current.category]
      .sort((a: any, b: any) => (a.value < b.value ? 1 : -1))
      .forEach((item: any, index: number) => {
        categoryHash[item.label] = {
          ...item,
          change: 0,
          color: colors[index],
          percentage: getPercentage(item.value, total, 2),
        };
      });

    activeHistory.prev.category.forEach((item: any, index: number) => {
      if (categoryHash[item.label]) {
        const currentValue = categoryHash[item.label].value;
        const change = getPercentage(currentValue - item.value, currentValue, 2);
        categoryHash[item.label] = { ...categoryHash[item.label], change };
      } else {
        categoryHash[item.label] = {
          ...item,
          change: -100,
          color: colors[activeHistory.current.category.length - index],
        };
      }
    });

    setCategories(Object.values(categoryHash));
  }, [activeHistory]);

  return (
    <Card>
      <p className='text-20 mb-24'>Supply Categories ({getDate(activeHistory.current.ts)})</p>
      <div className='analysisCategories'>
        <div className='doughnut'>
          <Chart
            type='doughnut'
            data={{
              datasets: [
                {
                  data: categories.map((item: any) => item.percentage),
                  backgroundColor: categories.map((item: any) => item.color),
                  borderWidth: 0,
                  hoverOffset: 0,
                  offset: 0,
                },
              ],
            }}
            options={{
              borderWidth: 1,
              hoverOffset: 5,
              offset: 0,
              layout: {
                padding: 40,
              },
              plugins: {
                tooltip: {
                  callbacks: {
                    label: "",
                  },
                  enabled: false,
                  external: () => {},
                },
                datalabels: {
                  align: "end",
                  anchor: "end",
                  rotation: 0,
                  offset: 0,
                  font: {
                    weight: "bold",
                  },
                  formatter: (item: number) => {
                    return item < 3 ? "" : item + "%";
                  },
                  color: (item: any) => item.dataset.backgroundColor[item.dataIndex],
                },
              },
              animation: {
                duration: 1,
              },
            }}
            style={{ width: "100%" }}
          />
        </div>
        <div className='legend'>
          {categories.map((item: any, index) => (
            <div className='legendItem' key={index}>
              <div className='title'>
                <div className='marker' style={{ backgroundColor: item.color }} />
                <p>{item.label}</p>
              </div>
              <div className='percentage'>
                <p>{item.percentage}%</p>

                <Tag
                  severity={item.change === 0 ? "info" : item.change > 0 ? "success" : "danger"}
                  icon={"pi pi-caret-" + (item.change === 0 ? "" : item.change > 0 ? "up" : "down")}
                  value={item.change + "%"}
                ></Tag>
              </div>
              <p className='amount'>
                {parseBigInt(item.value, token.dcml, "en-US")} {token.symbol.toUpperCase()}
              </p>
            </div>
          ))}
        </div>
      </div>
    </Card>
  );
};

export default memo(AnalysisCategories);
