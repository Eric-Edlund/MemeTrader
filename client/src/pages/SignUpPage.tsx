import { FormControl } from "@mui/base";
import {
  Button,
  Container,
  FormLabel,
  TextField,
  Typography,
} from "@mui/material";
import React from "react";
import { createContext, useContext, useState } from "react";
import { useNavigate } from "react-router";
import { API_URL } from "../constants";
import { elevatedStyle } from "../styles";

enum Phase {
  CreatePassword,
  VerifyEmail,
}

const SignupContext = createContext({
  email: null,
  setEmail: (_e: string) => {},
  password: null,
  setPassword: (_p: string) => {},
  setPhase: (_phase: Phase) => {},
  verificationAttemptId: null,
  setVerificationAttemptId: (_id: string) => {},
});

export default function SignUpPage() {
  const [phase, setPhase] = useState<Phase>(Phase.CreatePassword);
  const [email, setEmail] = useState(null);
  const [password, setPassword] = useState(null);
  const [verificationAttemptId, setVerificationAttemptId] = useState(null);
 
  return (
    <SignupContext.Provider
      value={{
        email,
        setEmail,
        password,
        setPassword,
        setPhase,
        verificationAttemptId,
        setVerificationAttemptId,
      }}
    >
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
          {phase === Phase.CreatePassword ? (
            <AccountInfoCollector />
          ) : phase == Phase.VerifyEmail ? (
            <EmailVerifier />
          ) : null}
        </Container>
      </Container>
    </SignupContext.Provider>
  );
}

function EmailVerifier() {
  const { email, verificationAttemptId } = useContext(SignupContext);
  const navigate = useNavigate()

  const [error, setError] = useState(null);
  const [code, setCode] = useState("");

  async function handleSubmit() {
    const response = await fetch(`${API_URL}/v1/user/verifyUser`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      credentials: "include",
      body: JSON.stringify({
        attemptId: verificationAttemptId,
        code,
      }),
    });

    if (response.ok) {
      setError(null)
      navigate("/login")
    } else {
      setError(response.text())
    }
  }

  return (
    <>
      {error}
      <FormControl
        style={{
          display: "flex",
          flexDirection: "column",
        }}
      >
        <Typography variant="h3">Check Your Email</Typography>
        We sent a code to {email}. Check your spam folder.
        <FormLabel htmlFor="code" sx={{ marginTop: "1em" }}>
          Code
        </FormLabel>
        <TextField
          id="code"
          type="text"
          value={code}
          onChange={(e) => setCode(e.target.value)}
        />
        <Button
          variant="contained"
          onMouseDown={handleSubmit}
          sx={{ marginTop: "1em" }}
        >
          Verify
        </Button>
      </FormControl>
    </>
  );
}

function AccountInfoCollector() {
  const{ setPhase, email, password, setEmail, setPassword, setVerificationAttemptId } =
    useContext(SignupContext);

  const [error, setError] = useState(null);

  async function handleSubmit() {
    const response = await fetch(`${API_URL}/v1/user/createUser`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify({
        email,
        password,
      }),
    });

    if (response.ok) {
      setVerificationAttemptId(await response.text())
      setPhase(Phase.VerifyEmail);
    } else {
      setError(await response.text());
    }
  }

  return (
    <>
      {error}
      <FormControl
        style={{
          display: "flex",
          flexDirection: "column",
        }}
      >
        <Typography variant="h3">Sign Up</Typography>
        <FormLabel htmlFor="email">Email</FormLabel>
        <TextField
          id="email"
          type="text"
          placeholder="example@gmail.com"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
        />

        <FormLabel htmlFor="password">Password</FormLabel>
        <TextField
          id="password"
          type="password"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
        />
        <Button
          variant="contained"
          onMouseDown={handleSubmit}
          sx={{ marginTop: "1em" }}
        >
          Proceed
        </Button>
      </FormControl>
    </>
  );
}
