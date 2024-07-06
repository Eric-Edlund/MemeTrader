import { styled, alpha } from "@mui/material/styles";
import React, { useEffect, useState } from "react";
import { Autocomplete, Avatar, Box, Icon, ListItem, ListItemIcon, ListItemText, TextField } from "@mui/material";
import { useNavigate } from "react-router-dom";
import { getSearchResults } from "../api"

export default function SearchBar() {
  const [options, setOptions] = useState([]);
  const [inputValue, setInputValue] = useState("");
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate()

  useEffect(() => {
    async function fetchOptions() {
      if (inputValue === "") {
        setOptions([]);
        return;
      }

      setLoading(true);

      const results = await getSearchResults(inputValue)

      setOptions(results);

      setLoading(false);
    }
    fetchOptions();
  }, [inputValue]);

  return (
    <>
      <Autocomplete
        options={options}
        freeSolo
        loading={loading}
        forcePopupIcon={false}
        getOptionLabel={(option) => option.title}
        filterOptions={(x) => x}
        onInputChange={(_, value) => setInputValue(value)}
        autoHighlight={true}
        isOptionEqualToValue={() => true}
        autoComplete={true}
        onChange={(_, value) => {
          document.activeElement.blur()
          if (value) {
            navigate(`/stock/${value.stockId}`)
          }
        }}
        renderInput={(params) => (
          <>
            <Search>
              <TextField placeholder="Search" {...params} />
            </Search>
          </>
        )}
        renderOption={(params, option) => (
          <ListItem {...params} key={option.stockId}>
            <ListItemIcon>
              <Avatar variant="square" alt={option.title} src={option.imageUrl} /></ListItemIcon>
            <ListItemText>{option.title}</ListItemText>
          </ListItem>
        )}
      ></Autocomplete>
    </>
  );
}

const Search = styled("div")(({ theme }) => ({
  position: "relative",
  borderRadius: theme.shape.borderRadius,
  backgroundColor: alpha(theme.palette.common.white, 0.15),
  "&:hover": {
    backgroundColor: alpha(theme.palette.common.white, 0.25),
  },
  marginRight: theme.spacing(2),
  marginLeft: 0,
  width: "100%",
  [theme.breakpoints.up("sm")]: {
    marginLeft: theme.spacing(3),
    width: "auto",
  },
}));

const SearchIconWrapper = styled("div")(({ theme }) => ({
  padding: theme.spacing(0, 2),
  height: "100%",
  position: "absolute",
  pointerEvents: "none",
  display: "flex",
  alignItems: "center",
  justifyContent: "center",
}));
