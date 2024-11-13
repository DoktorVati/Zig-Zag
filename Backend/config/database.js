const { Sequelize } = require("sequelize");

require("dotenv").config(); // Load environment variables

const sequelize = new Sequelize(
  process.env.DB_NAME,
  process.env.DB_USER,
  process.env.DB_PASSWORD,
  {
    host: process.env.DB_HOST,
    dialect: "postgres",
    define: {
      underscored: true,
    },
  }
);

sequelize
  .authenticate()
  .then(() =>
    console.log("Connection to database has been established sucessfully.")
  )
  .catch((err) => console.error("Unable to connect to the database:", err));

module.exports = sequelize;
