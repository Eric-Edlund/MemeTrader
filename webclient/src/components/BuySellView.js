import { Button, Box, Container, TextField, useTheme, styled, Typography, Divider } from "@mui/material";
import React from "react";
import { useState } from "react";
import { globalState } from "../App";
import SplitButton from "./SplitButton";

const currencyFormatter = Intl.NumberFormat("en-US", {
  style: "currency",
  currency: "USD",
  minimumFractionDigits: 2,
  maximumFractionDigits: 2,
});

export default function OrderForm({
  stockId,
  userId,
  pricePerShare,
  metadata,
  onTransaction,
}) {
  const theme = useTheme();

  const { connected } = globalState;
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [formData, setFormData] = useState({
    operation: "BUY",
    userId: userId,
    stockId: stockId,
    numShares: 1,
    pricePerShare: pricePerShare,
    totalPrice: 1,
  });

  async function handleSubmit(event) {
    event.preventDefault();
    setIsSubmitting(true);

    try {
      const response = await fetch("http://localhost:8080/v1/user/order", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify(formData),
        credentials: "include",
      });

      if (!response.ok) {
        const errorMessage = await response.text();
        throw new Error(errorMessage);
      }

      onTransaction();
    } catch (error) {
      // display the error message to the user
      console.error(error.message);
    } finally {
      await new Promise((res) => setTimeout(res, 500));
      setIsSubmitting(false);
    }
  }

  function handleChange(event) {
    const { name, value } = event.target;
    setFormData({
      ...formData,
      [name]: value,
    });
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

        <form onSubmit={handleSubmit} style={{
          paddingBottom: theme.spacing(2),
          paddingTop: theme.spacing(2),
        }}>
          <SplitButton
            values={["BUY", "SELL"]}
            labels={["Buy", "Sell"]}
            onChange={handleChange}
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
              onChange={handleChange}
              InputProps={{
                inputProps: {
                  min: 0,
                  step: 1,
                },
              }}
            />
            <br />@ {currencyFormatter.format(pricePerShare / 100)}/share ={" "}
            {currencyFormatter.format(
              (formData.numShares * pricePerShare) / 100,
            )}
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
        </form>
      </Container>
    </>
  );
}
