import { Box, Button, Typography, useTheme } from "@mui/material";
import React from "react";
import { useState } from "react";

/**
 * A button with a split for each value.
 * Each click switches the selected value.
 */
export default function SplitButton({ values, labels, onChange, name }) {
  const theme = useTheme();
  const [selectedIndex, setSelectedIndex] = useState(0);

  const handleClick = () => {
    const newIndex = (selectedIndex + 1) % values.length;
    onChange({
      target: {
        name: name,
        value: values[newIndex],
      },
    });
    setSelectedIndex(newIndex);
  };

  const options = [];

  for (let i = 0; i < values.length; i++) {
    const label = labels[i];

    options.push(
      <Box
        key={i}
        sx={{
          flex: 1,
          backgroundColor: selectedIndex === i ? theme.palette.primary.main : theme.palette.background.default,
          display: "flex",
          justifyContent: "center",
          alignItems: "center",
          cursor: "pointer",
          padding: "0.5em",
        }}
      >
        <Typography
          variant="button"
          color={selectedIndex === i ? theme.palette.primary.contrastText : theme.palette.text.primary}
        >
          {label}
        </Typography>
      </Box>,
    );
  }

  return (
    <Button onPointerDown={handleClick} sx={{ p: 0 }}>
      <Box
        sx={{
          display: "flex",
          border: `1px solid ${theme.palette.divider}`,
          borderRadius: 2,
          overflow: "hidden",
          aspectRatio: values.length,
        }}
      >
        {options}
      </Box>
    </Button>
  );
}
