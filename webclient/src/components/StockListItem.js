import "./StockListItem.css";
import { useEffect, useState } from "react";
import {
  Avatar,
  Grid,
  ListItemIcon,
  Skeleton,
  ListItem,
  useTheme,
  Card,
  Typography,
} from "@mui/material";
import { Line } from "react-chartjs-2";
import "chartjs-adapter-date-fns";
import {
  Chart as ChartJS,
  TimeScale,
  LinearScale,
  PointElement,
  LineElement,
  Tooltip,
} from "chart.js";
import React from "react";
import { usePriceHistory, getStockMetadata, getStockPrice } from "./api";
import { elevatedStyle } from "../styles";

ChartJS.register(TimeScale, LinearScale, PointElement, LineElement, Tooltip);

function StockListItem({ stockId }) {
  const [metadata, setMetadata] = useState({});
  const [price, setPrice] = useState(null);
  const [history, setHistory] = useState(null);

  useEffect(() => {
    async function fetchMetadata() {
      setMetadata(await getStockMetadata(stockId));
    }
    fetchMetadata();
  }, [stockId]);

  useEffect(() => {
    async function fetchPrice() {
      setPrice(await getStockPrice(stockId));
    }
    fetchPrice();
  }, [stockId]);

  useEffect(() => {
    async function fetchHistory() {
      const yesterday = new Date(new Date().setDate(new Date().getDate() - 1));

      const result = await usePriceHistory(stockId, yesterday, new Date());

      if (result.points.length == 0) {
        setHistory("nodata");
        return;
      }

      result.points = result.points.map(({ time, price }) => {
        return { x: new Date(time), y: price };
      });

      // Extrapolate to now
      result.points.push({
        x: new Date(),
        y: result.points[result.points.length - 1].y,
      });

      setHistory({
        datasets: [
          {
            label: "Data",
            data: result.points,
            borderColor: "green",
            backgroundColor: "red",
            pointRadius: 0,
          },
        ],
      });
    }
    fetchHistory();
  }, []);

  const theme = useTheme();

  return (
    <ListItem sx={{ ...elevatedStyle(2)(theme), borderRadius: "0.5em" }}>
        <Grid container spacing="2ch">
          <Grid item xs={3} style={{ display: "flex" }}>
            <ListItemIcon>
              <Avatar
                variant="rounded"
                alt={metadata.title}
                src={metadata.imageUrl}
              />
            </ListItemIcon>
          </Grid>

          <Grid item xs={6} justifyContent="center">
            {history == null ? (
              <Skeleton variant="rectangular" width="20ch" height="2em" />
            ) : history == "nodata" ? (
              <p>No data</p>
            ) : (
              <Line
                width={"100%"}
                height={"2em"}
                options={OPTIONS}
                data={history}
              />
            )}
          </Grid>

          <Grid item justifyContent="right" xs={3}>
            <Typography color={theme.palette.text.primary} textAlign="center">
              {price != null ? (
                (price / 100).toLocaleString("en-US", {
                  style: "currency",
                  currency: "USD",
                })
              ) : (
                <Skeleton variant="text" width={"3ch"} height={"1.2em"} />
              )}
            </Typography>
          </Grid>
        </Grid>
    </ListItem>
  );
}

export default StockListItem;

const OPTIONS = {
  maintainAspectRatio: false,
  scales: {
    x: {
      display: false,
      drawOnChartArea: false,
      ticks: {
        display: false,
      },
      type: "time",
      min: new Date(new Date().setDate(new Date().getDate() - 1)),
      max: new Date(),
      grid: {
        display: false,
      },
      drawBorder: false,
    },
    y: {
      display: false,
      beginAtZero: true,
      drawBorder: false,
      suggestedMax: 100,
      ticks: {
        display: false,
      },
      grid: {
        display: false,
      },
    },
  },
};
