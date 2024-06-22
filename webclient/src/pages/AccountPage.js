import React, { useState, useEffect, useContext } from "react";
import { setUserBio } from "../components/api";
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
  Card,
  CardContent,
  Typography,
  Link as MuiLink,
  useTheme,
} from "@mui/material";
import { Doughnut } from "react-chartjs-2";
import { Chart, ArcElement } from "chart.js";
import { Link } from "react-router-dom";
import { ApplicationContext } from "../UserContext";

Chart.register(ArcElement);

function AccountSummaryCard({}) {
  const { user, triggerRefresh } = useContext(ApplicationContext);

  const [editing, setEditing] = useState(false);
  const [newBio, setNewBio] = useState(user.bio);

  const handleSave = async () => {
    try {
      await setUserBio(newBio);
      setEditing(false);
      triggerRefresh();
    } catch (error) {
      console.error(error);
    }
  };

  return (
    <Card
      sx={{
        boxShadow: 3,
        padding: 2,
        height: "100%",
        display: "flex",
        flexDirection: "column",
      }}
    >
      <CardContent sx={{ flexGrow: 1 }}>
        <Typography variant="h6">{user.userName}</Typography>
        <p>{user.email}</p>
        {editing ? (
          <textarea
            value={newBio}
            onChange={(e) => setNewBio(e.target.value)}
          />
        ) : (
          <Typography variant="body1">{user.bio}</Typography>
        )}
      </CardContent>
      <CardContent>
        {editing ? (
          <MuiLink onClick={handleSave} sx={{ cursor: "pointer" }}>
            Save
          </MuiLink>
        ) : (
          <MuiLink onClick={() => setEditing(true)} sx={{ cursor: "pointer" }}>
            Edit Profile
          </MuiLink>
        )}
      </CardContent>
    </Card>
  );
}

function AccountPage() {
  const { balance, holdings, loading } = useContext(ApplicationContext);
  const theme = useTheme();

  if (loading) {
    return (
      <Grid container spacing={2}>
        <Grid item xs={3}>
          <Skeleton variant="text" />
          <Skeleton variant="text" />
        </Grid>
        <Grid item xs={9}>
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
                      <Skeleton variant="text" />
                    </TableCell>
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
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </TableContainer>
        </Grid>
      </Grid>
    );
  }

  const totalStockValue = holdings.reduce(
    (acc, holding) => acc + holding.amtOwned * holding.currentPrice,
    0,
  );
  const data = {
    labels: ["Account Balance", ...holdings.map(holding => holding.title)],
    datasets: [
      {
        label: "Account Distribution",
        data: [balance, ...holdings.map(holding => holding.amtOwned * holding.currentPrice / 100)],
        borderColor: theme.palette.background.default,
        backgroundColor: ["rgb(255, 99, 132)", "rgb(54, 162, 235)"],
        hoverOffset: 4,
      },
    ],
  };

  return (
    <Grid container spacing={2}>
      <Grid item xs={3}>
        <AccountSummaryCard />
      </Grid>
      <Grid item xs={9}>
        <Grid container spacing={2} padding="1em">
          <Grid item xs={6} alignContent="center" textAlign="right">
            <Typography variant="h4">Cash: ${balance / 100}</Typography>
            <Typography variant="h4">
              Stocks: ${totalStockValue / 100}
            </Typography>
          </Grid>
          <Grid item xs={6} maxHeight="40vh">
            <Doughnut data={data} />
          </Grid>
        </Grid>
        <h2>Holdings</h2>
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
              {holdings.map((holding, index) => (
                <TableRow
                  key={index}
                  component={Link}
                  to={`/stock/${holding.stockId}`}
                  onMouseDown={event => event.target.click()}
                  sx={{ textDecoration: "none" }}
                >
                  <TableCell sx={{display: 'flex'}}>
                    <Avatar
                      sx={{boxShadow: 1}}
                      variant="rounded"
                      src={holding.metadata.imageUrl}
                      alt={holding.metadata.title}
                    />
                    <p style={{alignContent: 'center', marginLeft: "1em"}}>{holding.metadata.title}</p>
                    
                  </TableCell>
                  <TableCell align="center">{holding.amtOwned}</TableCell>
                  <TableCell align="center">
                    ${holding.currentPrice / 100}
                  </TableCell>
                  <TableCell align="center">
                    ${(holding.amtOwned * holding.currentPrice) / 100}
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </TableContainer>
      </Grid>
    </Grid>
  );
}

export default AccountPage;
