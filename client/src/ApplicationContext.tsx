import { useMediaQuery } from "@mui/material";
import React, { createContext, useState, useEffect, useReducer } from "react";
import {
  getBalance,
  getHoldings,
  getStockMetadata,
  getStockPrice,
  getUserInfo,
} from "./api";
import { API_URL } from "./constants";

const ApplicationContext = createContext<{
  user?: any;
  balance?: number;
  holdings?: any[];
  loading?: boolean;
  refresh?: () => void;
  triggerRefresh?: () => void;
  darkMode?: boolean;
  triggerThemeReload?: () => void;
  authenticated?: boolean;
  setAuthenticated?: (val: boolean) => void;
  triggerRecheckLogin?: () => void;
  connectedStatus?: string;
  setConnectedStatus?: (val: string) => void;
  loadingAccountInfo?: boolean;
}>({});

const ApplicationStateProvider = ({ children }) => {
  const [recheckLogin, triggerRecheckLogin] = useReducer((a) => a + 1, 0);

  const systemPrefersDarkMode = useMediaQuery("(prefers-color-scheme: dark)");
  const [darkMode, setDarkMode] = useState(false);
  const [themeReloaded, triggerThemeReload] = useReducer((a) => a + 1, 0);
  useEffect(
    () => determineDarkMode(setDarkMode, systemPrefersDarkMode),
    [systemPrefersDarkMode, themeReloaded],
  );

  const [user, setUser] = useState({});
  const [holdings, setHoldings] = useState([]);
  const [balance, setBalance] = useState(0);
  const [loadingAccountInfo, setLoadingAccountInfo] = useState(true);
  const [authenticated, setAuthenticated] = useState(false);
  const [refreshAccountInfo, triggerRefreshAccountInfo] = useReducer(
    (a) => a + 1,
    0,
  );

  const [connectedStatus, setConnectedStatus] = useState("connected"); // "disconnected"

  useEffect(() => {
    if (!authenticated) {
      setLoadingAccountInfo(false);
      return;
    }

    const fetchUserData = async () => {
      try {
        const data = await getUserInfo();
        setUser(data);
      } catch (error) {
        console.error(error);
      }
    };

    const fetchHoldings = async () => {
      try {
        const data = await getHoldings();
        const holdingsWithMetadata = await Promise.all(
          data.holdings.map(async (holding) => {
            const metadata = await getStockMetadata(holding.stockId);
            const currentPrice = await getStockPrice(holding.stockId);
            return { ...holding, metadata, currentPrice };
          }),
        );
        setHoldings(holdingsWithMetadata);
      } catch (error) {
        console.error(error);
      }
    };

    const fetchBalance = async () => {
      try {
        const data = await getBalance();
        setBalance(data.balance);
      } catch (error) {
        console.error(error);
      }
    };

    const fetchData = async () => {
      await Promise.all([fetchUserData(), fetchHoldings(), fetchBalance()]);
      setLoadingAccountInfo(false);
    };

    fetchData();
  }, [refreshAccountInfo, authenticated]);

  useEffect(() => {
    async function checkLogin() {
      const loggedIn = await fetch(`${API_URL}/v1/user`, {
        headers: {
          "X-Requested-With": "XMLHttpRequest",
        },
        credentials: "include",
      });

      if (loggedIn.ok) {
        setAuthenticated(true);
      } else {
        setAuthenticated(false);
      }
    }
    checkLogin();
  }, [recheckLogin]);

  return (
    <ApplicationContext.Provider
      value={{
        user,
        balance,
        holdings,
        loading: loadingAccountInfo,
        refresh: refreshAccountInfo,
        triggerRefresh: triggerRefreshAccountInfo,
        darkMode,
        triggerThemeReload,
        authenticated,
        setAuthenticated,
        triggerRecheckLogin,
        loadingAccountInfo,

        connectedStatus,
        setConnectedStatus,
      }}
    >
      {children}
    </ApplicationContext.Provider>
  );
};

export { ApplicationStateProvider, ApplicationContext };

function determineDarkMode(setDarkMode, systemPrefersDarkMode) {
  const stored = window.localStorage.getItem("theme");
  setDarkMode(
    stored === "light"
      ? false
      : stored === "dark"
        ? true
        : systemPrefersDarkMode,
  );
}
