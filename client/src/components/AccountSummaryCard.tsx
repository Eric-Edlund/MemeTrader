import { CardContent, Drawer, Link, Toolbar, Typography } from "@mui/material";
import React from "react";
import { useContext, useContext, useState } from "react";
import { setUserBio } from "../api";
import { ApplicationContext } from "../ApplicationContext";
import { elevatedStyle } from "../styles";

function AccountSummaryCard({}) {
  const { user, triggerRefresh } = useContext(ApplicationContext);

  const [editing, setEditing] = useState(false);
  const [newBio, setNewBio] = useState(user.bio);

  const handleSave = async () => {
    try {
      await setUserBio(newBio);
      setEditing(false);
      triggerRefresh();
    } catch (error) {
      console.error(error);
    }
  };

  return (
    <Drawer
      variant="permanent"
      sx={(theme) => {
        return {
          ...elevatedStyle(2)(theme),
          height: "100%",
          display: "flex",
          flexDirection: "column",
        };
      }}
    >
      <Toolbar />
      <CardContent sx={{ flexGrow: 1 }}>
        <Typography variant="h6">{user.userName}</Typography>
        <p>{user.email}</p>
        {editing ? (
          <textarea
            value={newBio}
            onChange={(e) => setNewBio(e.target.value)}
          />
        ) : (
          <Typography variant="body1">{user.bio}</Typography>
        )}
      </CardContent>
      <CardContent>
        {editing ? (
          <Link onClick={handleSave} sx={{ cursor: "pointer" }}>
            Save
          </Link>
        ) : (
          <Link onClick={() => setEditing(true)} sx={{ cursor: "pointer" }}>
            Edit Profile
          </Link>
        )}
      </CardContent>
    </Drawer>
  );
}
