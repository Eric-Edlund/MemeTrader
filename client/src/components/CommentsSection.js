import React, { useState, useEffect } from 'react';
import { Container, Typography, Grid, Card, CardContent, CardHeader } from '@mui/material';
import axios from 'axios';

function Comments({ articleId }) {
  const [comments, setComments] = useState([]);
  const [page, setPage] = useState(0);
  const [size, setSize] = useState(10);

  useEffect(() => {
    axios.get(`http://localhost:8080/v1/public/articles/${articleId}/comments?page=${page}&size=${size}`)
      .then(response => setComments(response.data))
      .catch(error => console.error(error));
  }, [articleId, page, size]);

  return (
    <Container>
      <Typography variant="h4">Comments</Typography>
      <Grid container spacing={2}>
        {comments.map((comment, index) => (
          <Grid item key={index} xs={12}>
            <Card>
              <CardHeader title={comment.user.username} subheader={comment.postDate} />
              <CardContent>
                <Typography variant="body1">{comment.commentText}</Typography>
              </CardContent>
            </Card>
          </Grid>
        ))}
      </Grid>
    </Container>
  );
}

export default Comments;
