const { DataTypes } = require("sequelize");
const sequelize = require("../config/database");

const Hashtag = sequelize.define(
  "Hashtag",
  {
    postId: {
      type: DataTypes.INTEGER,
      allowNull: false,
    },
    name: {
      type: DataTypes.STRING,
      allowNull: false,
    },
  },
  {
    modelName: "Hashtag",
    tableName: "hashtags",
    timestamps: false,
  }
);

module.exports = Hashtag;
