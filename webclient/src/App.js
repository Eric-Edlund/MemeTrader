import "./App.css";
import StockPage from "./pages/StockPage";
import {
  Snackbar,
  AppBar,
  Toolbar,
  Typography,
  Alert,
  CssBaseline,
  Button,
  Box,
} from "@mui/material";
import { BrowserRouter, Route, Routes, Link } from "react-router-dom";
import React, {
  useEffect,
  useState,
  useReducer,
  useContext,
  useMemo,
} from "react";
import Search from "./components/Search";
import ArticlePage from "./pages/ArticlePage";
import HomePage from "./pages/HomePage";
import { API_URL } from "./constants";
import AccountPage from "./pages/AccountPage";
import { ApplicationContext } from "./UserContext";
import { createTheme, ThemeProvider, useTheme } from "@mui/material/styles";
import ThemeToggler from "./components/ThemeToggler";
import LoginPage from "./pages/LoginPage";

export const globalState = {};

function NavigationBar() {
  const { user, authenticated, triggerRecheckLogin, setOnLoginPage } =
    useContext(ApplicationContext);
  const theme = useTheme();

  return (
    <AppBar position="sticky" sx={{zIndex: theme.zIndex.drawer + 1}}>
      <Toolbar style={{ display: "flex" }}>
        <Link
          style={{ textDecoration: "none", color: "inherit" }}
          to="/"
          onMouseDown={(event) => event.target.click()}
        >
          <Typography marginX="1ch">Home</Typography>
        </Link>
        <Link
          style={{ textDecoration: "none", color: "inherit" }}
          to="/account"
          onMouseDown={(event) => event.target.click()}
        >
          <Typography marginX="1ch">Account</Typography>
        </Link>

        <div style={{ flexGrow: 3, maxWidth: "50%", margin: "auto" }}>
          <Search />
        </div>

        {authenticated ? (
          <a
            onMouseDownCapture={() => {
              fetch(`${API_URL}/logout`, {
                method: "POST",
                credentials: "include",
              })
                .then((response) => console.log("Logged out: " + response.ok))
                .catch((error) => console.error(error))
                .finally(triggerRecheckLogin);
            }}
          >
            {user.userName}
          </a>
        ) : (
          <>
            <Button onMouseDown={setOnLoginPage}>
              <Typography color={theme.palette.common.white}>Log in</Typography>
            </Button>
          </>
        )}

        <ThemeToggler />
      </Toolbar>
    </AppBar>
  );
}

function App() {
  const { darkMode, onLoginPage } = useContext(ApplicationContext);

  const theme = useMemo(
    () =>
      createTheme({
        palette: {
          mode: darkMode ? "dark" : "light",
        },
      }),
    [darkMode],
  );

  const [connected, setConnected] = useState(true); // To the internet/server
  const [reconnected, triggerReconnected] = useReducer((a) => a + 1, 0);

  globalState.connected = connected;
  globalState.setConnected = setConnected;
  globalState.reconnected = reconnected;

  useEffect(() => {
    if (connected) {
      triggerReconnected();
    }
  }, [connected]);

  return (
      <ThemeProvider theme={theme}>
        <CssBaseline />

        {onLoginPage ? (
          <LoginPage />
        ) : (
          <BrowserRouter>
            <Snackbar open={!connected}>
              <Alert severity="error" variant="filled">
                Unable to connect to server.
              </Alert>
            </Snackbar>

            <NavigationBar />
            <Box flex={1}>
              <Routes>
                <Route path="/" Component={HomePage} />
                <Route path="/stock/:stockId" Component={StockPage} />
                <Route path="/article/:articleId" Component={ArticlePage} />
                <Route path="/account" Component={AccountPage} />
              </Routes>
            </Box>
          </BrowserRouter>
        )}
      </ThemeProvider>
  );
}

export default App;
