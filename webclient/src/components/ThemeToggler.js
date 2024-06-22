import { DarkMode, LightMode } from "@mui/icons-material";
import { IconButton, useTheme } from "@mui/material";
import React from "react";
import { useContext } from "react";
import { ApplicationContext } from "../UserContext";

export default function ThemeToggler() {
  const { darkMode, triggerThemeReload } = useContext(ApplicationContext);

  const theme = useTheme()

  return (
    <IconButton
      onMouseDown={function () {
        window.localStorage.setItem("theme", darkMode ? "light" : "dark");
        triggerThemeReload();
      }}
    >
      {darkMode ? <LightMode /> : <DarkMode sx={{color: theme.palette.common.white}}/>}
    </IconButton>
  );
}
