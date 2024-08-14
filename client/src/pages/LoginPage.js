import {
  FormControl,
  FormLabel,
  Button,
  TextField,
  Typography,
  Alert,
  Divider,
  Container,
} from "@mui/material";
import React, { useState } from "react";
import { useContext } from "react";
import { API_URL } from "../constants";
import { elevatedStyle } from "../styles";
import { ApplicationContext } from "../ApplicationContext";
import { useNavigate } from "react-router";
import { Link } from "react-router-dom";
import { Google } from "@mui/icons-material";

export default function LoginPage() {
  const { triggerRecheckLogin } = useContext(ApplicationContext);

  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [badCredentials, setBadCredentials] = useState(false);

  const navigate = useNavigate();

  function handleSubmit() {
    fetch(`${API_URL}/v1/user`, {
      method: "GET",
      headers: {
        Authorization: "Basic " + btoa(email + ":" + password),
      },
      credentials: "include",
    }).then((response) => {
      if (response.ok) {
        triggerRecheckLogin();
        navigate("/");
      } else setBadCredentials(true);
    });
  }

  return (
    <Container
      sx={{
        alignContent: "center",
        justifyItems: "center",
        height: "100vh",
      }}
    >
      <Container
        sx={(theme) => ({
          ...elevatedStyle(2)(theme),
          borderRadius: "0.5em",
          padding: "1em",
          width: "fit-content",
        })}
      >
        <Typography variant="h3">Sign In</Typography>

        {badCredentials ? (
          <Alert severity="error">The username or password is incorrect.</Alert>
        ) : null}

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

        <br />
        <Divider aria-hidden="true">Or</Divider>
        <br />

        <div style={{ textAlign: "center" }}>
          <Button variant="outlined" sx={{paddingLeft: 0}}>
            <Google sx={{margin: '0.5em'}} />
            Sign in with Google
          </Button>
          <br/>
          <br/>
          <Link to="/signup">Sign up</Link>
        </div>
      </Container>
    </Container>
  );
}
