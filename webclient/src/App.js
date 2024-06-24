import "./App.css";
import StockPage from "./pages/StockPage";
import {
  Snackbar,
  AppBar,
  Toolbar,
  Typography,
  Alert,
  CssBaseline,
} from "@mui/material";
import { BrowserRouter, Route, Routes, Link } from "react-router-dom";
import React, {
  useEffect,
  useState,
  useReducer,
  useContext,
  createContext,
  useMemo,
} from "react";
import Search from "./components/Search";
import ArticlePage from "./pages/ArticlePage";
import HomePage from "./pages/HomePage";
import { API_URL } from "./constants";
import AccountPage from "./pages/AccountPage";
import { ApplicationContext } from "./UserContext";
import { createTheme, ThemeProvider } from "@mui/material/styles";
import ThemeToggler from "./components/ThemeToggler";

export const globalState = {};

function NavigationBar() {
  const { user, authenticated, triggerRecheckLogin } =
    useContext(ApplicationContext);

  return (
    <AppBar style={{ position: "sticky" }}>
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
                // headers: {
                //   "Content-Type": "application/json",
                //   "X-Requested-With": "XMLHttpRequest",
                // },
                credentials: 'include',
              })
                .then((response) => console.log("Logged out: " + response.ok))
                .catch((error) => console.error(error))
              .finally(triggerRecheckLogin)
            }}
          >
            {user.userName}
          </a>
        ) : (
          <a
            onClick={() => {
              fetch(`${API_URL}/user`, {
                method: "GET",
                headers: {
                  Authorization:
                    "Basic " +
                    btoa(loginData.username + ":" + loginData.password),
                },
                  credentials: 'include'
              }).then((response) => {
                console.log(response.ok);
                if (response.ok) triggerRecheckLogin();
              });
            }}
          >
            Log in
          </a>
        )}

        <ThemeToggler />
      </Toolbar>
    </AppBar>
  );
}

function App() {
  const { darkMode, setAuthenticated } = useContext(ApplicationContext);

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
    <>
      <ThemeProvider theme={theme}>
        <CssBaseline />
        <BrowserRouter>
          <Snackbar open={!connected}>
            <Alert severity="error" variant="filled">
              Unable to connect to server.
            </Alert>
          </Snackbar>

          <NavigationBar />

          <Routes>
            <Route path="/" Component={HomePage} />
            <Route path="/stock/:stockId" Component={StockPage} />
            <Route path="/article/:articleId" Component={ArticlePage} />
            <Route path="/account" Component={AccountPage} />
          </Routes>
        </BrowserRouter>
      </ThemeProvider>
    </>
  );
}

export default App;

const loginData = {
  username: "ericedlund2017@gmail.com",
  password: "password",
};

// fetch(`${API_URL}/`, {
//   method: "POST",
//   headers: {
//     "Content-Type": "application/x-www-form-urlencoded",
//     "Authorization": "Basic username=ericedlund2017@gmail.com password=1234",
//   },
//   // credentials: 'include',
//   body: new URLSearchParams(loginData),
// })
//   .then(async (response) => {
//     if (response.ok) {
//       // Login successful
//       console.log("Login successful");
//       console.log(await response.text());
//     } else {
//       // Login failed
//       console.log("Login failed");
//       console.log(response.statusText);
//       console.log(await response.text());
//     }
//   })
//   .catch((error) => {
//     console.error("Error:", error);
//   });
