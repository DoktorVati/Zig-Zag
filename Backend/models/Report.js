const { DataTypes } = require("sequelize");
const sequelize = require("../config/database");

const Report = sequelize.define(
  "Report",
  {
    snitch: {
      type: DataTypes.STRING,
      unique: true,
      allowNull: false,
    },
    postId: {
      type: DataTypes.INTEGER,
      allowNull: false,
    },
  },
  {
    modelName: "Report",
    tableName: "reports",
  }
);

module.exports = Report;
