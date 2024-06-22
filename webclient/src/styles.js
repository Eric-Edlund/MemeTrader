// Mixed feelings about how clever this is...

/**
 * Styles the element to be elevated by height. Used as an sx prop;
 * @param height Positive integer
 */
export const elevatedStyle = (height) => (theme) => ({
  boxShadow: theme.palette.mode === "dark" ? "none" : theme.shadows[height],
  backgroundColor:
    theme.palette.mode === "dark" ? theme.palette.grey[900] : "auto",
});
