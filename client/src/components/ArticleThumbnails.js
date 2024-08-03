import "./ArticleThumbnails.css";
import {
  ListItem,
  Container,
  Skeleton,
  Typography,
  Grid,
  useTheme,
  Box,
} from "@mui/material";
import { useNavigate } from "react-router-dom";
import React from "react";

export function PrimaryArticleThumbnail({ article }) {
  const navigate = useNavigate();
  const theme = useTheme();

  let { id, published, title, body, imageUrl } = article ?? {};

  body = body?.trim();

  return (
    <Container
      className="PrimaryArticleThumbnail"
      sx={{
        cursor: "pointer",
        transition: "background-color 0.2s",
        "&:hover": {
          backgroundColor: theme.palette.action.hover,
        },
        marginBottom: '1em',
      }}
      onPointerDown={() => navigate("/article/" + id, { state: { article } })}
    >
      <div style={{ cursor: "pointer" }}>
        {article ? (
          <>
            <h1>{title}</h1>
            <p style={{ fontSize: "0.8em", color: "#666" }}>
              Published{" "}
              {new Date(published).toLocaleDateString() +
                " " +
                new Date(published).toLocaleTimeString()}
            </p>
          </>
        ) : (
          <>
            <Skeleton variant="text" height="3em" width="100%" />
            <Skeleton variant="text" height="3em" width="40%" />
            <Skeleton variant="text" height="0.8em" width="20ch" />
          </>
        )}

        {article ? (
          <img
            style={{
              width: "100%",
              objectFit: "cover",
              borderRadius: theme.shape.borderRadius,
              boxShadow: "0 0 10px rgba(0, 0, 0, 0.1)",
              marginBottom: "1em",
            }}
            src={imageUrl}
          />
        ) : (
          <Skeleton
            variant="rectangular"
            style={{
              width: "100%",
              height: "30em",
              aspectRatio: 16 / 9,
              marginBottom: "1em",
            }}
          ></Skeleton>
        )}

        {article ? (
          <Typography
            fontFamily="serif"
            sx={{
              whiteSpace: "pre-wrap",
              overflow: "hidden",
              textOverflow: "ellipsis",
              display: "-webkit-box",
              WebkitLineClamp: 5,
              WebkitBoxOrient: "vertical",
            }}
          >
            {body}
          </Typography>
        ) : (
          <>
            <Skeleton variant="text" width="100%" />
            <Skeleton variant="text" width="100%" />
            <Skeleton variant="text" width="100%" />
            <Skeleton variant="text" width="100%" />
            <br />

            <Skeleton variant="text" width="100%" />
            <Skeleton variant="text" width="60%" />
          </>
        )}
      </div>
    </Container>
  );
}

export function SecondaryArticleThumbnail({ article }) {
  const navigate = useNavigate();
  const theme = useTheme();

  let { id, published, title, body, imageUrl } = article ?? {};

  body = body?.trim();

  return (
    <ListItem>
      <Container
        className="PrimaryArticleThumbnail"
        sx={{
          cursor: "pointer",
          transition: "background-color 0.2s",
          "&:hover": {
            backgroundColor: theme.palette.action.hover,
          },
        }}
        style={{ cursor: "pointer" }}
        onPointerDown={() => navigate("/article/" + id, { state: { article } })}
      >
        {article ? (
          <>
            <Typography variant="h5" style={{display: 'inline'}}>{title + " "}</Typography>
            <p style={{ fontSize: "0.8em", color: "#666", display: 'inline' }}>
              Published{" "}
              {new Date(published).toLocaleDateString() +
                " " +
                new Date(published).toLocaleTimeString()}
            </p>
          </>
        ) : (
          <>
            <Skeleton variant="text" height="3em" width="100%" />
            <Skeleton variant="text" height="3em" width="40%" />
            <Skeleton variant="text" height="0.8em" width="20ch" />
          </>
        )}

        <Grid container>
          <Grid item xs={4}>
            {article ? (
              <img
                style={{
                  height: "6em",
                  margin: '1em',
                  objectFit: "cover",
                  borderRadius: theme.shape.borderRadius,
                  boxShadow: "0 0 10px rgba(0, 0, 0, 0.1)",
                }}
                src={imageUrl}
              />
            ) : (
                <Skeleton
                  variant="rectangular"
                  style={{
                    width: "100%",
                    height: "10em",
                    aspectRatio: 16 / 9,
                    marginBottom: "1em",
                  }}
                ></Skeleton>
              )}
          </Grid>

          <Grid item xs={8}>
            {article ? (
              <Typography
                fontFamily="serif"
                style={{
                  whiteSpace: "pre-wrap",
                  overflow: "hidden",
                  textOverflow: "ellipsis",
                  display: "-webkit-box",
                  WebkitLineClamp: 5,
                  WebkitBoxOrient: "vertical",
                }}
              >
                {body}
              </Typography>
            ) : (
                <>
                  <Skeleton variant="text" width="100%" />
                  <Skeleton variant="text" width="100%" />
                  <Skeleton variant="text" width="100%" />
                  <Skeleton variant="text" width="100%" />
                  <br />

                  <Skeleton variant="text" width="100%" />
                  <Skeleton variant="text" width="60%" />
                </>
              )}
          </Grid>
        </Grid>
      </Container>
    </ListItem>
  );
}
