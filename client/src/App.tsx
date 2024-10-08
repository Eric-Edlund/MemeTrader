import StockPage from "./pages/StockPage";
import {
  Snackbar,
  AppBar,
  Toolbar,
  Typography,
  Alert,
  CssBaseline,
  Box,
  IconButton,
  Avatar,
  Menu,
  MenuItem,
  ListItemIcon,
} from "@mui/material";
import { BrowserRouter, Route, Routes, Link } from "react-router-dom";
import React, { useContext, useMemo, useState } from "react";
import Search from "./components/Search";
import ArticlePage from "./pages/ArticlePage";
import HomePage from "./pages/HomePage";
import { API_URL } from "./constants";
import PortfolioPage from "./pages/PortfolioPage";
import { ApplicationContext } from "./ApplicationContext";
import { createTheme, ThemeProvider, useTheme } from "@mui/material/styles";
import ThemeToggler from "./components/ThemeToggler";
import LoginPage from "./pages/LoginPage";
import { DarkMode, LightMode, Logout } from "@mui/icons-material";
import SignUpPage from "./pages/SignUpPage";

function NavigationBar() {
  const { authenticated, loadingAccountInfo } =
    useContext(ApplicationContext);
  const theme = useTheme();

  return (
    <AppBar position="sticky" sx={{ zIndex: theme.zIndex.drawer + 1 }}>
      <Toolbar style={{ display: "flex" }}>
        <Link
          style={{ textDecoration: "none", color: "inherit" }}
          to="/"
          onPointerDown={(event) => (event.target as HTMLElement).click()}
        >
          <Typography marginX="1ch">Home</Typography>
        </Link>
        {authenticated ? (
          <Link
            style={{ textDecoration: "none", color: "inherit" }}
            to="/portfolio"
            onPointerDown={(event) => (event.target as HTMLElement).click()}
          >
            <Typography marginX="1ch">Portfolio</Typography>
          </Link>
        ) : null}

        <div style={{ flexGrow: 3, maxWidth: "50%", margin: "auto" }}>
          <Search />
        </div>

        {loadingAccountInfo ? null : !authenticated ? (
          <>
            <Link
              to="/login"
              style={{ textDecoration: "none", color: "inherit" }}
            >
              <Typography color={theme.palette.common.white}>Log in</Typography>
            </Link>

            <ThemeToggler lightButtonColor={null} darkButtonColor={null} />
          </>
        ) : (
          <>
            <AccountIcon />
          </>
        )}
      </Toolbar>
    </AppBar>
  );
}

function AccountIcon() {
  const { user, triggerRecheckLogin, darkMode, triggerThemeReload } =
    useContext(ApplicationContext);
  const [anchorEl, setAnchorEl] = useState<null | HTMLElement>(null);
  const open = Boolean(anchorEl);

  return (
    <>
      <IconButton
        onPointerDown={(e) => setAnchorEl(e.currentTarget)}
        aria-controls={open ? "account-menu" : undefined}
        aria-haspopup="true"
        aria-expanded={open ? "true" : undefined}
      >
        <Avatar sx={{ width: 32, height: 32 }}>E</Avatar>
      </IconButton>

      <Menu
        open={open}
        anchorEl={anchorEl}
        id="account-menu"
        onClose={() => {
          setAnchorEl(null);
        }}
        onClick={() => setAnchorEl(null)}
        transformOrigin={{ horizontal: "right", vertical: "top" }}
        anchorOrigin={{ horizontal: "right", vertical: "bottom" }}
      >
        <MenuItem>
          <Avatar>E</Avatar>
          <Typography fontSize="large" sx={{ marginInlineStart: "1em" }}>
            {user.userName}
          </Typography>
        </MenuItem>

        <MenuItem
          onMouseDown={() => {
            fetch(`${API_URL}/logout`, {
              method: "POST",
              credentials: "include",
            })
              .then((response) => console.log("Logged out: " + response.ok))
              .catch((error) => console.error(error))
              .finally(triggerRecheckLogin);
          }}
        >
          <ListItemIcon>
            <Logout />
          </ListItemIcon>
          Logout
        </MenuItem>
        <MenuItem
          onMouseDown={() => {
            window.localStorage.setItem("theme", darkMode ? "light" : "dark");
            triggerThemeReload();
          }}
        >
          <ListItemIcon>{darkMode ? <LightMode /> : <DarkMode />}</ListItemIcon>
          <Typography>{darkMode ? "Light Mode" : "Dark Mode"}</Typography>
        </MenuItem>
      </Menu>
    </>
  );
}

function App() {
  const { darkMode, connectedStatus } = useContext(ApplicationContext);

  const theme = useMemo(
    () =>
      createTheme({
        palette: {
          mode: darkMode ? "dark" : "light",
        },
      }),
    [darkMode],
  );

  function withNavBar(page: React.JSX.Element) {
    return (
      <>
        <NavigationBar />
        {page}
      </>
    );
  }

  return (
    <ThemeProvider theme={theme}>
      <CssBaseline />

      <BrowserRouter>
        <Snackbar open={connectedStatus !== "connected"}>
          <Alert severity="error" variant="filled">
            Unable to connect to server.
          </Alert>
        </Snackbar>

        <Box flex={1}>
          <Routes>
            <Route path="/" element={withNavBar(<HomePage />)} />
            <Route path="/stock/:stockId" element={withNavBar(<StockPage />)} />
            <Route
              path="/article/:articleId"
              element={withNavBar(<ArticlePage />)}
            />
            <Route path="/portfolio" element={withNavBar(<PortfolioPage />)} />
            <Route path="/login" Component={LoginPage} />
            <Route path="/signup" Component={SignUpPage} />
          </Routes>
        </Box>
      </BrowserRouter>
    </ThemeProvider>
  );
}

export default App;
