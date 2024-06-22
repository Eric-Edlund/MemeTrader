import { useMediaQuery } from "@mui/material";
import React, { createContext, useState, useEffect, useReducer } from "react";
import {
  getBalance,
  getHoldings,
  getStockMetadata,
  getStockPrice,
  getUserInfo,
} from "./components/api";

const ApplicationContext = createContext({});

const ApplicationStateProvider = ({ children }) => {
  const [authenticated, setAuthenticated] = useState(false);
  const [user, setUser] = useState({});
  const [holdings, setHoldings] = useState([]);
  const [balance, setBalance] = useState(0);
  const [loading, setLoading] = useState(true);

  const systemPrefersDarkMode = useMediaQuery("(prefers-color-scheme: dark)");
  const [darkMode, setDarkMode] = useState(false);
  const [themeReloaded, triggerThemeReload] = useReducer((a) => a + 1, 0);

  useEffect(() => {
    const stored = window.localStorage.getItem("theme");
    setDarkMode(
      stored === "light"
        ? false
        : stored === "dark"
        ? true
        : systemPrefersDarkMode,
    );
  }, [systemPrefersDarkMode, themeReloaded]);

  const [refresh, triggerRefresh] = useReducer((a) => a + 1, 0);
  useEffect(() => {
    if (!authenticated) return;
    else console.log("Fetching user info")

    const fetchUserData = async () => {
      try {
        const data = await getUserInfo();
        console.log(data)
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
      setLoading(false);
    };

    fetchData();
  }, [refresh, authenticated]);

  return (
    <ApplicationContext.Provider
      value={{
        user,
        balance,
        holdings,
        loading,
        refresh,
        triggerRefresh,
        darkMode,
        triggerThemeReload,
        authenticated,
        setAuthenticated,
      }}
    >
      {children}
    </ApplicationContext.Provider>
  );
};

export { ApplicationStateProvider, ApplicationContext };
