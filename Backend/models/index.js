const Post = require("./Post");
const Hashtag = require("./Hashtag");
const Comment = require("./Comment");
const Report = require("./Report")

Post.hasMany(Hashtag, {
  foreignKey: "postId",
  onDelete: "CASCADE",
});

Hashtag.belongsTo(Post, {
  foreignKey: "postId",
});

Post.hasMany(Comment, {
  foreignKey: "postId",
  onDelete: "CASCADE",
});

Comment.belongsTo(Post, {
  foreignKey: "postId",
});

Post.hasMany(Report, {
  foreignKey: "postId",
  onDelete: "CASCADE"
})

Report.belongsTo(Post, {
  foreignKey: "postId"
})

module.exports = {
  Post,
  Hashtag,
  Comment,
  Report
};
