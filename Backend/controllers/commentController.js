const sequelize = require("../config/database");
const { Comment } = require("../models");

async function getCommentsByPostId(postId) {
  try {
    const comments = await Comment.findAll({
      where: {
        postId: postId,
      },
    });

    return comments;
  } catch (error) {
    throw new Error(
      `There was an error getting comments for POST ${postId}. ${error.message}`
    );
  }
}

async function createComment(postId, commentText, author) {
  try {
    const createdComment = Comment.create({
      postId: postId,
      authorId: author,
      text: commentText,
    });

    return createdComment;
  } catch (error) {
    throw new Error(
      `There was an error creating a comment for POST ${postId}. ${error.message}`
    );
  }
}

module.exports = { getCommentsByPostId, createComment };
