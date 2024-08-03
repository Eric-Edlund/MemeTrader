import axios from "axios";
import { useContext } from "react";
import { API_URL } from "./constants";
import { ApplicationContext } from "./ApplicationContext";

/*
 * All api functions block until reconnection.
 */

export async function getPriceHistory(stockId, startDate, endDate) {
  return (
    await retrying(() =>
      axios.get(
        `${API_URL}/v1/public/stock/history?stockId=${stockId}&startDate=${startDate.toISOString()}&endDate=${endDate.toISOString()}`,
      ),
    )
  ).data;
}

export async function getStockMetadata(stockId) {
  let response;
  do {
    response = await retrying(() =>
      fetch(`${API_URL}/v1/public/stock/metadata?stockId=${stockId}`),
    );
  } while (!response.ok);

  return await response.json();
}

export async function getStockPrice(stockId) {
  let response;
  do {
    response = await retrying(() =>
      fetch(`${API_URL}/v1/public/stock/price?stockId=${stockId}`),
    );
  } while (!response.ok);

  return await response.json();
}

export async function getFrontpageStocks() {
  const response = await retrying(() =>
    fetch(`${API_URL}/v1/public/frontpagestocks`),
  );
  return await response.json();
}

export async function getSearchResults(inputValue) {
  const response = await retrying(() =>
    fetch(
      `${API_URL}/v1/public/searchStocks?` +
        new URLSearchParams([["searchString", inputValue]]),
    ),
  );

  return await response.json();
}

export async function getUserInfo(userId) {
  const response = await retrying(() =>
    fetch(`${API_URL}/v1/user`, {
      credentials: "include",

      headers: {
        "X-Requested-With": "XMLHttpRequest",
      },
    }),
  );
  return await response.json();
}

export async function setUserBio(newBio) {
  const response = await retrying(() =>
    fetch(`${API_URL}/v1/user/description`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",

        "X-Requested-With": "XMLHttpRequest",
      },
      body: JSON.stringify({ newBio }),
      credentials: "include",
    }),
  );
  return await response.json();
}

export async function getHoldings() {
  const response = await retrying(() =>
    fetch(`${API_URL}/v1/user/holdings`, {
      credentials: "include",
      headers: {
        "X-Requested-With": "XMLHttpRequest",
      },
    }),
  );
  return await response.json();
}

export async function getBalance() {
  const response = await retrying(() =>
    fetch(`${API_URL}/v1/user/balance`, {
      credentials: "include",

      headers: {
        "X-Requested-With": "XMLHttpRequest",
      },
    }),
  );
  return await response.json();
}

/**
 * Tries it retries times then waits for reconnection.
 * Never throws.
 * @param requestGenerator A function which returns a promise which will be
 * awaited and it's result returned. If the promise rejects, a new one will
 * be created and tried.
 */
async function retrying(requestGenerator, retries = 2) {
  // const { setConnectedStatus } = useContext(ApplicationContext);

  try {
    const result = await requestGenerator();
    // setConnectedStatus("connected");
    return result;
  } catch (error) {
    if (retries <= 0) {
      // setConnectedStatus("disconnected");
      await reconnect();
      return await retrying(requestGenerator);
    }
    await new Promise((res) => setTimeout(res, 1000));
    return await retrying(requestGenerator, retries - 1);
  }
}

let trying = null;
async function reconnect() {
  // const { setConnectedStatus} = useContext(ApplicationContext)
  if (trying) {
    return await trying;
  }

  trying = new Promise((res) => {
    let ping = setInterval(async () => {
      try {
        await axios.get(`${API_URL}/v1/public/stock/price?stockId=10`);
        clearInterval(ping);
        // setConnectedStatus("connected");
        trying = null;
        res();
      } catch (e) {}
    }, 5000);
  });

  await trying;
}
