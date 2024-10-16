const { DataTypes } = require("sequelize");
const sequelize = require("../config/database");

const Post = sequelize.define(
  "Post",
  {
    authorId: {
      type: DataTypes.STRING,
      allowNull: false,
    },
    text: {
      type: DataTypes.STRING,
      allowNull: false,
    },
    expiryDate: {
      type: DataTypes.DATE,
      allowNull: true,
    },
    location: {
      type: DataTypes.GEOMETRY("POINT", 4326),
      allowNull: false,
    },
  },
  {
    modelName: "post",
    tableName: "posts",
  }
);

module.exports = Post;
