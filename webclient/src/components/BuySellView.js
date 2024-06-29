import {
  Button,
  Box,
  Container,
  TextField,
  useTheme,
  styled,
  Typography,
  Divider,
  Alert,
} from "@mui/material";
import React, { useEffect } from "react";
import { useState, useReducer } from "react";
import { globalState } from "../App";
import SplitButton from "./SplitButton";
import { getStockPrice } from "./api";
const currencyFormatter = Intl.NumberFormat("en-US", {
  style: "currency",
  currency: "USD",
  minimumFractionDigits: 2,
  maximumFractionDigits: 2,
});

/**
 * @param formData All the form data needed for the order except the total price.
 * @param totalPrice {number} The total price of the transaction.
 * @param onTransaction {function} Called if the transaction is successful.
 */
async function submitOrder(event, formData, totalPrice, onTransaction) {
  event.preventDefault();

  try {
    await sendOrder({ ...formData, totalPrice: totalPrice });
    onTransaction();
  } catch (error) {
    // display the error message to the user
    console.error(error.message);
  } finally {
    await new Promise((res) => setTimeout(res, 500));
  }
}

/**
 * @param special {"dryRun" | "" | "getTotalPrice"}
 */
async function sendOrder(orderData, special = "") {
  const response = await fetch(
    "http://localhost:8080/v1/user/order" +
      (special == "dryRun"
        ? "?dryRun=true"
        : special == "getTotalPrice"
          ? "?getTotalPrice=true"
          : ""),
    {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        "X-Requested-With": "XMLHttpRequest",
      },
      body: JSON.stringify(orderData),
      credentials: "include",
    },
  );

  if (!response.ok) {
    const errorMessage = await response.text();
    throw new Error(errorMessage);
  }

  return await response.json();
}

export default function OrderForm({
  stockId,
  userId,
  pricePerShare,
  metadata,
  onTransaction,
}) {
  if (!pricePerShare || !metadata || !stockId || !userId) {
    throw new Error("invalid state");
  }
  const theme = useTheme();

  const { connected } = globalState;
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [invalidInput, setInvalidInput] = useState(null); // null if ok, or string reason
  const [formData, setFormData] = useState({
    operation: "BUY",
    userId: userId,
    stockId: stockId,
    numShares: 1,
  });
  const [totalPrice, setTotalPrice] = useState(null);

  async function handleFormChange(event) {
    const { name, value } = event.target;
    const newFormData = {
      ...formData,
      [name]: value,
    };
    setFormData(newFormData);


    //TODO: Fetch new price
    const totalPrice = await sendOrder(newFormData, "getTotalPrice");
    console.log("total price: " + totalPrice);
    setTotalPrice(totalPrice);


    if (newFormData.numShares == 0) {
      return;
    }


    const result = await sendOrder({ ...newFormData, totalPrice }, "dryRun");
    if (!result.success) {
      const problem = result.reason;
      switch (problem) {
        case "InsufficientHoldings":
          setInvalidInput("Insufficient holdings to sell");
          return;
        case "InsufficientFunds":
          setInvalidInput("Insufficient balance");
          return;
        case "InvalidOrder":
          // TODO: Log this somehow
          setInvalidInput("An error is preventing this order");
          return;
      }
    } else {
      setInvalidInput(null);
    }
  }

  return (
    <>
      <Container
        sx={{
          border: `1px solid ${theme.palette.divider}`,
          borderRadius: "0.5em",
          backgroundColor: theme.palette.background.paper,
          padding: theme.spacing(2),
        }}
      >
        <Typography
          fontSize="2.5em"
          sx={{
            color: theme.palette.text.primary,
          }}
        >
          {formData.operation == "BUY" ? "Buy" : "Sell"} {metadata.symbol}
        </Typography>

        <Divider />

        <form
          onSubmit={async (event) => {
            setIsSubmitting(true);
            await submitOrder(event, formData, totalPrice, onTransaction);
            setIsSubmitting(false);
          }}
          style={{
            paddingBottom: theme.spacing(2),
            paddingTop: theme.spacing(2),
          }}
        >
          <SplitButton
            values={["BUY", "SELL"]}
            labels={["Buy", "Sell"]}
            onChange={handleFormChange}
            name="operation"
          />

          <br />

          <label htmlFor="numShares">Shares</label>

          <Box>
            <TextField
              type="number"
              required
              name="numShares"
              defaultValue={formData.numShares}
              placeholder="0"
              onChange={handleFormChange}
              InputProps={{
                inputProps: {
                  min: 0,
                  step: 1,
                },
              }}
            />
            <br />@ {currencyFormatter.format(pricePerShare / 100)}/share ={" "}
            {totalPrice ? currencyFormatter.format(totalPrice / 100) : null}
          </Box>

          <br />

          <Button
            variant="contained"
            type="submit"
            value="Place Order"
            disabled={isSubmitting || !connected}
          >
            Place Order
          </Button>

          {invalidInput ? (
            <Alert severity="warning">{invalidInput}</Alert>
          ) : null}
        </form>
      </Container>
    </>
  );
}
