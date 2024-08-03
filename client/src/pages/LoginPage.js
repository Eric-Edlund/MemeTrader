import {
  FormControl,
  FormLabel,
  Grid,
  Button,
  TextField,
  Typography,
  useTheme,
  Alert,
} from "@mui/material";
import React, { useState } from "react";
import { useContext } from "react";
import { API_URL } from "../constants";
import { elevatedStyle } from "../styles";
import { ApplicationContext } from "../ApplicationContext";

export default function LoginPage() {
  const { triggerRecheckLogin } = useContext(ApplicationContext);

  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [badCredentials, setBadCredentials] = useState(false);

  function handleSubmit() {
    fetch(`${API_URL}/v1/user`, {
      method: "GET",
      headers: {
        Authorization: "Basic " + btoa(email + ":" + password),
      },
      credentials: "include",
    }).then((response) => {
      console.log(response.ok);
      if (response.ok) {
        triggerRecheckLogin();
      } else setBadCredentials(true);
    });
  }

  return (
    <Grid
      container
      direction="column"
      alignItems="center"
      justifyContent="center"
      minHeight="100vh"
    >
      <Grid
        item
        xs={4}
        sx={(theme) => ({
          ...elevatedStyle(2)(theme),
          borderRadius: "0.5em",
          padding: "1em",
        })}
      >
        <Typography variant="h3">Login</Typography>

        {badCredentials ? (
          <Alert severity="error">The username or password is incorrect.</Alert>
        ) : null}

        <br />
        <FormControl
          style={{
            display: "flex",
            flexDirection: "column",
          }}
        >
          <FormLabel htmlFor="email">Email</FormLabel>
          <TextField
            name="email"
            type="email"
            value={email}
            onChange={(event) => setEmail(event.target.value)}
            sx={{ minWidth: "30ch" }}
          />
          <FormLabel htmlFor="password">Password</FormLabel>
          <TextField
            name="password"
            type="password"
            value={password}
            onChange={(event) => setPassword(event.target.value)}
          />
          <Button
            variant="contained"
            sx={{ marginTop: "1em" }}
            onPointerDown={handleSubmit}
          >
            Login
          </Button>
        </FormControl>
      </Grid>
    </Grid>
  );
}
