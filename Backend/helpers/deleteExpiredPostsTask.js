const { Post } = require("../models")
const Sequelize = require("sequelize")

module.exports = async function deleteExpiredPostsTask() {
    console.log("Deleting any expired posts.")
    const currentDate = new Date();
    try {
      const rowsDeleted = await Post.destroy({
        where: {
          expiryDate: {
            [Sequelize.Op.lt]: currentDate, // Delete all posts that have an expiryDate less than the currentDate (has already expired)
          },
        }
      })
  
      console.log(`Deleted ${rowsDeleted} posts.`)
  
    } catch (e) {
      console.error(`There was an error deleting expired posts. ${e}`)
    }
  }