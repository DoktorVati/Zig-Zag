const express = require("express");
const cors = require("cors");
const morgan = require("morgan");


// Local Imports
const sequelize = require("./config/database"); // Database
const postRoutes = require("./routes/postRoutes");
const deleteExpiredPostsTask = require("./helpers/deleteExpiredPostsTask")

// Initalize app
const app = express();

// Constants
const PORT = 3000;

// Middleware
app.use(cors());
app.use(express.json());
app.use(express.urlencoded({ extended: true }));
app.use(morgan("combined"));


app.use("/posts", postRoutes);

sequelize.sync() // Syncs database, not ideal for production but great for testing.

deleteExpiredPostsTask() // Delete all expired posts on first run
setInterval(deleteExpiredPostsTask, 24 * 60 * 60 * 1000) // Run deleteExpiredPostsTask every hour.

// Start server
app.listen(PORT, (error) => {
  if (!error) {
    console.log(`Server is running and listening on port ${PORT}.`);
  } else {
    console.error("Error occured and server could not start.", error);
  }
});
