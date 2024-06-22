import React, { useEffect, useState } from "react";
import { Skeleton, ToggleButton, ToggleButtonGroup } from "@mui/material";
import { Line } from "react-chartjs-2";
import {
  Chart as ChartJS,
  TimeScale,
  LinearScale,
  PointElement,
  LineElement,
  Tooltip,
} from "chart.js";
import { useTranslation } from "react-i18next";
import { getPriceHistory } from "./api";
import { globalState } from "../App";

ChartJS.register(TimeScale, LinearScale, PointElement, LineElement, Tooltip);

const LiveGraph = ({ stockId, reloadTrigger }) => {
  const { connected } = globalState;
  const { t } = useTranslation();

  const [stockData, setStockData] = useState(null);
  const [range, setRange] = useState(1); // Days
  const [chartOptions, setChartOptions] = useState({});
  function getStartDate() {
    return new Date(new Date().setDate(new Date().getDate() - range));
  }
  const endDate = new Date();

  useEffect(() => {
    setChartOptions({
      aspectRatio: 16 / 9,
      animation: false,
      scales: {
        x: {
          display: true,
          time: {
            timezone: "UTC",
          },

          drawOnChartArea: false,
          drawTicks: true,
          type: "time",
          min: getStartDate(),
          max: new Date(),
          grid: {
            color: () => "rgba(0,0,0,0)",
          },
        },
        y: {
          beginAtZero: true,
          suggestedMax: 100,
          ticks: {
            callback: function (val) {
              return (val / 100).toLocaleString("en-US", {
                style: "currency",
                currency: "USD",
              });
            },
          },
        },
      },
    });
  }, [range]);

  async function getStockData(/** @type {number}*/ stockId) {
    const data = (
      await getPriceHistory(stockId, getStartDate(), endDate)
    ).points.map(({ time, price }) => {
      return { x: new Date(time), y: price };
    });

    // Extrapolate to now
    data.push({ x: new Date(), y: data[data.length - 1].y });

    setStockData({
      datasets: [
        {
          data: data,
          borderColor: "red",
          borderWidth: 2,
          pointRadius: 0,
        },
      ],
    });
  }

  const click = (event) => event.target.click();

  useEffect(() => {
    getStockData(stockId);
  }, [stockId, range, reloadTrigger]);

  return (
    <>
      <div>
        {stockData ? (
          <Line data={stockData} options={chartOptions} />
        ) : (
          <Skeleton variant="rectangular" width="100%" height="30em" />
        )}
      </div>

      <ToggleButtonGroup
        exclusive
        value={"" + range}
        fullWidth
        onChange={function (event) {
          setRange(event.target.value);
        }}
      >
        <ToggleButton value="1" onMouseDown={click}>
          {t("1D")}
        </ToggleButton>
        <ToggleButton onMouseDown={click} value="7">{t("1W")}</ToggleButton>
        <ToggleButton onMouseDown={click} value="30">{t("1M")}</ToggleButton>
        <ToggleButton onMouseDown={click} value="90">{t("3M")}</ToggleButton>
        <ToggleButton onMouseDown={click} value="365">{t("1Y")}</ToggleButton>
        <ToggleButton onMouseDown={click} value="1825">{t("5Y")}</ToggleButton>
        <ToggleButton onMouseDown={click} value="99999">{t("MAX")}</ToggleButton>
      </ToggleButtonGroup>
    </>
  );
};
export default LiveGraph;
