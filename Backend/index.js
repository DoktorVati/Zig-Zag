const express = require("express");
const cors = require("cors");
const morgan = require("morgan");
const postRoutes = require("./routes/postRoutes");

// Local Imports
const sequelize = require("./config/database"); // Database

// Initalize app
const app = express();

// Constants
const PORT = 3000;

// Middleware
app.use(cors());
app.use(express.json());
app.use(express.urlencoded({ extended: true }));
app.use(morgan("combined"));

sequelize.sync({ force: true });

app.use("/posts", postRoutes);

// Start server
app.listen(PORT, (error) => {
  if (!error) {
    console.log(`Server is running and listening on port ${PORT}.`);
  } else {
    console.error("Error occured and server could not start.", error);
  }
});
