const Post = require("./Post");
const Hashtag = require("./Hashtag");

Post.hasMany(Hashtag, {
  foreignKey: "postId",
  onDelete: "CASCADE",
});

Hashtag.belongsTo(Post, {
  foreignKey: "postId",
});

module.exports = {
  Post,
  Hashtag,
};
