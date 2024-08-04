import { DarkMode, LightMode } from "@mui/icons-material";
import { IconButton, useTheme } from "@mui/material";
import React from "react";
import { useContext } from "react";
import { ApplicationContext } from "../ApplicationContext";

export default function ThemeToggler({ lightButtonColor, darkButtonColor }) {
  const { darkMode, triggerThemeReload } = useContext(ApplicationContext);

  const theme = useTheme();

  return (
    <IconButton
      onPointerDown={function () {
        window.localStorage.setItem("theme", darkMode ? "light" : "dark");
        triggerThemeReload();
      }}
    >
      {darkMode ? (
        <LightMode
          sx={{ color: lightButtonColor ?? theme.palette.common.white }}
        />
      ) : (
        <DarkMode
          sx={{ color: darkButtonColor ?? theme.palette.common.white }}
        />
      )}
    </IconButton>
  );
}
