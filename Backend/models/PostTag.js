const { DataTypes } = require("sequelize");
const sequelize = require("../config/database");

const PostTag = sequelize.define(
  "PostTag",
  {
    postId: {
      type: DataTypes.INTEGER,
      allowNull: false,
    },
    tagId: {
      type: DataTypes.INTEGER,
      allowNull: false,
    },
    tagOrder: {
      type: DataTypes.INTEGER,
      allowNull: false,
    },
  },
  {
    modelName: "post_tag",
    tableName: "post_tags",
    timestamps: false,
  }
);

module.exports = PostTag;
