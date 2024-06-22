import React from "react";
import {
  Container,
  Typography,
  Button,
  Divider,
  TextField,
  IconButton,
  Tooltip
} from "@mui/material";
import { useLocation } from "react-router-dom";
import FacebookIcon from "@mui/icons-material/Facebook";
import TwitterIcon from "@mui/icons-material/Twitter";
import LinkedInIcon from "@mui/icons-material/LinkedIn";
import ThumbUpIcon from "@mui/icons-material/ThumbUp";
import ThumbDownIcon from "@mui/icons-material/ThumbDown";
import Comments from "../components/CommentsSection";

function ArticlePage() {
  const location = useLocation();
  const article = location.state?.article;

  if (!article) {
    return <div>Loading...</div>;
  }

  article.body = article.body.trim();

  return (
    <Container>
      <h1>{article.title}</h1>
      <Typography style={{color: "#666"}} variant="small">
        Published{" "}
        {new Date(article.published).toLocaleDateString() +
          " " +
          new Date(article.published).toLocaleTimeString() + 
          "  •  Written by Chief Stock Analyst Chat G.P. T"
        }
      </Typography>
      <img
        style={{
          width: "100%",
          boxShadow: "0 0 10px rgba(0, 0, 0, 0.1)",
          marginTop: "1em",
          marginBottom: "2em",
        }}
        src={article.imageUrl}
      />
      <Typography fontFamily="serif" variant="body1" style={{ whiteSpace: "pre-wrap", fontSize: "1.2em",maxWidth: "70ch", margin: 'auto' }}>
        {article.body}
      </Typography>
      <Divider style={{ margin: "2em 0" }} />
      <div style={{ display: "flex", justifyContent: "space-between" }}>
        <div>
          <IconButton>
            <ThumbUpIcon />
          </IconButton>
          <IconButton>
            <ThumbDownIcon />
          </IconButton>
        </div>
        <div>

        <Tooltip title="None of your Facebook friends care about this article. Stop.">
          <IconButton>
            <FacebookIcon />
          </IconButton>
        </Tooltip>

        <Tooltip title="None of your Twitter followers care about this article. Stop.">
          <IconButton>
            <TwitterIcon />
          </IconButton>
        </Tooltip>
        <Tooltip title="Sharing this article will not help your career. Stop.">
          <IconButton>
            <LinkedInIcon/>
          </IconButton>
        </Tooltip>
        </div>
      </div>
      <Divider style={{ margin: "2em 0" }} />
      <Typography variant="h4">Comments</Typography>
      <TextField
        multiline
        rows={4}
        placeholder="Write a comment..."
        style={{ width: "100%" }}
      />
      <Comments articleId={article.id} />
      <Button variant="contained" style={{ marginTop: "1em" }}>
        Post Comment
      </Button>
      <Typography variant="body2" style={{ marginTop: "2em" }}>
        Copyright 2024 Stock Analysis Inc. All rights reserved.
      </Typography>
    </Container>
  );
}

export default ArticlePage;
