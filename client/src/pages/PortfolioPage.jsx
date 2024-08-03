import React, { useState, useEffect, useContext } from "react";
import {
  Grid,
  Skeleton,
  Avatar,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Typography,
  useTheme,
  Container,
} from "@mui/material";
import { elevatedStyle } from "../styles";

import {
  Chart as ChartJS,
  TimeScale,
  LinearScale,
  PointElement,
  LineElement,
  Tooltip,
  ArcElement,
  Filler,
} from "chart.js";
import { Doughnut, Line } from "react-chartjs-2";
import { Link } from "react-router-dom";
import { ApplicationContext } from "../ApplicationContext";
import { API_URL } from "../constants";

ChartJS.register(
  ArcElement,
  TimeScale,
  LinearScale,
  PointElement,
  LineElement,
  Tooltip,
  Filler,
);

const chartColors = ["#00a676", "#104911", "#F1DABF", "#9D6381", "#D64933"];
let colorIndex = 0;
const colorMap = {}
function assignColor(groupId: number) {
  // Check if the groupId already has a color
  if (!colorMap[groupId]) {
    // If the colorIndex reaches the end of the array, go back to 0
    colorIndex = colorIndex % chartColors.length;
    colorMap[groupId] = chartColors[colorIndex];
    colorIndex++;
  }
  return colorMap[groupId];
}


function HoldingsTable({ holdings }) {
  return (
    <TableContainer>
      <Table>
        <TableHead>
          <TableRow>
            <TableCell>Stock Name</TableCell>
            <TableCell align="center">Shares</TableCell>
            <TableCell align="center">Price</TableCell>
            <TableCell align="center">Total Value</TableCell>
          </TableRow>
        </TableHead>

        <TableBody>
          {holdings
            .sort(
              (a, b) =>
                b.amtOwned * b.currentPrice - a.amtOwned * a.currentPrice,
            )
            .map((holding, index) => (
              <TableRow
                key={index}
                component={Link}
                to={`/stock/${holding.stockId}`}
                onPointerDown={(event) => event.target.click()}
                sx={{ textDecoration: "none" }}
              >
                <TableCell sx={{ display: "flex" }}>
                  <Avatar
                    sx={{ boxShadow: 1 }}
                    variant="rounded"
                    src={holding.metadata.imageUrl}
                    alt={holding.metadata.title}
                  />
                  <p style={{ alignContent: "center", marginLeft: "1em" }}>
                    {holding.metadata.title}
                  </p>
                </TableCell>
                <TableCell align="center">{holding.amtOwned}</TableCell>
                <TableCell align="center">
                  {moneyFormatter.format(holding.currentPrice / 100)}
                </TableCell>
                <TableCell align="center">
                  {moneyFormatter.format(
                    (holding.amtOwned * holding.currentPrice) / 100,
                  )}
                </TableCell>
              </TableRow>
            ))}
        </TableBody>
      </Table>
    </TableContainer>
  );
}

function AccountPage() {
  const { balance, holdings, loading } = useContext(ApplicationContext);
  const theme = useTheme();

  if (loading) {
    return (
      <Container>
        <Skeleton variant="rounded" sx={{width: "100%", height: '50vh' }}></Skeleton>
          <TableContainer>
            <Table>
              <TableHead>
                <TableRow>
                  <TableCell>Stock Title</TableCell>
                  <TableCell>Image</TableCell>
                  <TableCell>Amount Held</TableCell>
                  <TableCell>Current Value</TableCell>
                  <TableCell>Total Value</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {[1, 2, 3, 4, 5].map((index) => (
                  <TableRow key={index}>
                  <TableCell>
                    <Skeleton variant="circular" width={40} height={40} />
                  </TableCell>
                    <TableCell>
                      <Skeleton variant="text" />
                    </TableCell>
                    <TableCell>
                      <Skeleton variant="text" />
                    </TableCell>
                    <TableCell>
                      <Skeleton variant="text" />
                    </TableCell>
                    <TableCell>
                      <Skeleton variant="text" />
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </TableContainer>
      </Container>
    );
  }

  const totalStockValue = holdings.reduce(
    (acc, holding) => acc + holding.amtOwned * holding.currentPrice,
    0,
  );
  const data = {
    labels: [
      "Account Balance",
      ...holdings.map((holding) => holding.metadata.title),
    ],
    datasets: [
      {
        label: "Value",
        data: [
          balance / 100,
          ...holdings.map(
            (holding) => (holding.amtOwned * holding.currentPrice) / 100,
          ),
        ],
        borderColor: theme.palette.background.default,
        backgroundColor: chartColors,
        hoverOffset: 4,
      },
    ],
  };

  return (
        <Container>
          <FinancialSummary
            balance={balance}
            data={data}
            holdings={holdings}
            totalStockValue={totalStockValue}
          />
        </Container>
  );
}

// function NetworthHistory() {
//   const { user } = useContext(ApplicationContext);
//   const [financialHistory, setFinancialHistory] = useState({});
//   const [chartData, setChartData] = useState(null);
//
//   useEffect(() => {
//     async function getData() {
//       const response = await fetch(
//         `${API_URL}/v1/user/accountHistory?` +
//           new URLSearchParams({
//             startDate: new Date(
//               new Date().setDate(new Date().getDay() - 1),
//             ).toISOString(),
//             endDate: new Date().toISOString(),
//             stockId: user.userId,
//           }),
//         {
//           credentials: "include",
//         },
//       );
//
//       const data = await response.json();
//       console.log(data);
//       setFinancialHistory(data.history);
//     }
//     getData();
//   }, [user]);
//
//   useEffect(() => {
//     if (!financialHistory) return;
//
//     const entries = Object.entries(financialHistory).sort(
//       (a, b) => new Date(b[0]) - new Date(a[0]),
//     );
//
//     const balanceData = entries
//       .map(([key, { balance, holdings }]) => {
//         return { x: new Date(key), y: balance };
//       })
//       .sort((a, b) => b.x - a.x);
//
//     const holdingDatas = new Map(); // stockId, total holding value at times
//     let lastDate;
//     for (const [key, { holdings }] of entries) {
//       for (const { stockId, amtOwned, sharePrice } of holdings) {
//         if (!holdingDatas.has(stockId)) {
//           holdingDatas.set(stockId, [{ x: lastDate, y: 0 }]);
//         }
//         lastDate = key;
//         holdingDatas.get(stockId).push({ x: key, y: amtOwned * sharePrice });
//       }
//     }
//
//     setChartData({
//       datasets: [
//         {
//           data: balanceData,
//           borderColor: "blue",
//           backgroundColor: "blue",
//           pointRadius: 0,
//           fill: true,
//         },
//         ...[...holdingDatas.entries()].map(([stockId, data], index) => {
//           return {
//             data: data.sort((a, b) => b.x - a.x),
//             borderColor: assignColor(stockId),
//             backgroundColor: assignColor(stockId),
//             pointRadius: 0,
//             fill: true,
//           };
//         }),
//       ],
//     });
//   }, [financialHistory]);
//
//   return chartData ? (
//     <Line options={CHART_OPTIONS} data={chartData}></Line>
//   ) : null;
// }

// const CHART_OPTIONS = {
//   aspectRatio: 16 / 9,
//   animation: false,
//   scales: {
//     x: {
//       display: true,
//       time: {
//         timezone: "UTC",
//       },
//
//       drawOnChartArea: false,
//       drawTicks: true,
//       type: "time",
//       // min: new Date(new Date().setDate(new Date().getDay() - 3)),
//       // max: new Date(new Date().setDate(new Date().getDay() - 2)),
//     },
//     y: {
//       stacked: true,
//       beginAtZero: true,
//       suggestedMax: 100,
//       min: 0,
//       ticks: {
//         callback: function (val) {
//           return (val / 100).toLocaleString("en-US", {
//             style: "currency",
//             currency: "USD",
//           });
//         },
//       },
//     },
//   },
// };

function FinancialSummary({ balance, totalStockValue, holdings, data }) {
  return (
    <>
      <Grid container spacing={2} padding="1em">
        <Grid item xs={6} alignContent="center" textAlign="right">
          <Typography variant="h4">
            Cash: {moneyFormatter.format(balance / 100)}
          </Typography>
          <Typography variant="h4">
            Stocks: {moneyFormatter.format(totalStockValue / 100)}
          </Typography>
        </Grid>

        <Grid item xs={6} maxHeight="40vh">
          <Doughnut data={data} />
        </Grid>
      </Grid>

      <h2>Holdings</h2>
      <HoldingsTable holdings={holdings} />
    </>
  );
}

export default AccountPage;

const moneyFormatter = new Intl.NumberFormat("en-US", {
  style: "currency",
  currency: "USD",
});
