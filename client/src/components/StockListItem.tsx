import { useEffect, useState } from "react";
import {
  Avatar,
  Grid,
  ListItemIcon,
  Skeleton,
  ListItem,
  useTheme,
  Typography,
} from "@mui/material";
import "chartjs-adapter-date-fns";
import React from "react";
import { getPriceHistory, getStockMetadata, getStockPrice } from "../api";
import { elevatedStyle } from "../styles";

function StockListItem({ stockId }) {
  const [metadata, setMetadata] = useState({});
  const [price, setPrice] = useState(null);
  const [history, setHistory] = useState(null);
  const [delta, setDelta] = useState(null); // Stock value in cents last 24hrs

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

      const result: {points: any[]} = await getPriceHistory(stockId, yesterday, new Date());

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

      let yesterdayValue = result.points.find(pt => pt.x <= yesterday) ?? result.points[0]

      setDelta(result.points[result.points.length - 1].y - yesterdayValue.y);
    }
    fetchHistory();
  }, []);

  const theme = useTheme();

  return (
    <ListItem sx={{ ...elevatedStyle(2)(theme), borderRadius: "0.5em" }}>
      <Grid container>
        <Grid item xs={"auto"} style={{ display: "flex" }}>
          <ListItemIcon>
            <Avatar
              variant="rounded"
              alt={metadata.title}
              src={metadata.imageUrl}
            />
          </ListItemIcon>
        </Grid>

        <Grid item xs={true} justifyContent="center">
          <Typography color={theme.palette.text.primary}>
            {metadata.title}
          </Typography>
        </Grid>

        <Grid item justifyContent="right" xs={3}>
          {price != null && delta != null ? (
            <>
              <Typography color={theme.palette.text.primary} textAlign="center">
                {(price / 100).toLocaleString("en-US", {
                  style: "currency",
                  currency: "USD",
                })}
              </Typography>
              <Typography color={delta >= 0 ? 'green' : 'red'} textAlign="center">
                {delta >= 0 ? "+" : "-"}
                {(delta / 100).toLocaleString("en-US", {
                  style: "currency",
                  currency: "USD",
                })}
              </Typography>
            </>
          ) : (
            <Skeleton variant="text" width={"3ch"} height={"1.2em"} />
          )}
        </Grid>
      </Grid>
    </ListItem>
  );
}

export default StockListItem;
