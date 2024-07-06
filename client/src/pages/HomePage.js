import "./HomePage.css"
import StockListItem from "../components/StockListItem";
import {
  List,
  Container,
  Grid,
  Button,
  Divider,
} from "@mui/material";
import {
  Link,
} from "react-router-dom";
import React, { useState, useEffect } from "react";
import { API_URL } from "../constants";
import { PrimaryArticleThumbnail, SecondaryArticleThumbnail } from "../components/ArticleThumbnails";
import { getFrontpageStocks } from "../api";

export default function HomePage() {
  const [stocks, setStocks] = useState([]);
  const [articles, setArticles] = useState([]);

  useEffect(() => {
    async function fetchFrontpageStocks() {
      setStocks(await getFrontpageStocks())
    }
    fetchFrontpageStocks();
  }, []);

  useEffect(() => {
    async function fetchArticles() {
      try {
        const response = await fetch(`${API_URL}/v1/public/articles?num=6`);
        if (response.ok) {
          setArticles(await response.json());
        }
      } catch (e) {
        console.log("Failed to fetch articles " + e);
      }
    }
    fetchArticles();
  }, []);

  return (
    <Container style={{ marginTop: "1em" }}>
      <Grid container direction="row" columnSpacing="1em">
        <Grid item justifyContent="center" xs={8}>
          <PrimaryArticleThumbnail article={articles[0]} />
          <Divider />
          {articles.slice(1).map((article) => (
            <SecondaryArticleThumbnail key={article.id} article={article} />
          ))}
        </Grid>
        <Grid item justifyContent="center" xs={4}>
          <List className="StockList">
            <h1 style={{textAlign: 'center'}} className="glow">Hot Memes</h1>
            {stocks.map((id) => (
              <Link
                key={id}
                to={`/stock/${id}`}
                onMouseDown={event => event.target.click()}
                style={{ textDecoration: "none", color: "black" }}
              >
                <Button fullWidth style={{color: "inherit"}}>
                  <StockListItem stockId={id} />
                </Button>
              </Link>
            ))}
          </List>
        </Grid>
      </Grid>
    </Container>
  );
}
