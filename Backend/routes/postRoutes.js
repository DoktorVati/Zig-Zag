const {
  createPost,
  getPost,
  getPosts,
  deletePost,
  getPostsByHashtag,
  getPostsWithinDistance,
  getAllPosts,
} = require("../controllers/postController");

const router = require("express").Router();

const { query, body, validationResult, param } = require("express-validator");

const validateCoordinates = (req) => {
  const fields = ["distance", "latitude", "longitude"];
  const filledFields = fields.filter((field) => req.query[field] !== undefined);

  if (filledFields.length > 0 && filledFields.length !== fields.length) {
    throw new Error(
      "If any of the following values — distance, latitude, or longitude — is provided, all must be provided."
    );
  }

  return true;
};

router.get(
  "/",
  query("longitude").isFloat(),
  query("latitude").isFloat(),
  query("distance").isNumeric().optional(),
  query("hashtag").isString().optional(),
  async (req, res) => {
    const errors = validationResult(req);

    if (!errors.isEmpty()) {
      return res.status(400).json({ errors: errors.array() });
    }

    const { latitude, longitude, distance = null, hashtag = null } = req.query;

    try {
      let posts;
      if (hashtag) {
        posts = await getPostsByHashtag(hashtag, latitude, longitude);
      } else if (distance) {
        posts = await getPostsWithinDistance(distance, latitude, longitude);
      } else {
        posts = await getAllPosts(latitude, longitude);
      }

      return res.status(200).json(posts);
    } catch (error) {
      console.error(error);
      return res.status(500).json({
        message: "There was an error fetching posts.",
        error: error,
      });
    }
  }
);

router.get(
  "/:id",
  param("id").isNumeric(),
  query("latitude").isFloat(),
  query("longitude").isFloat(),
  async (req, res) => {
    const errors = validationResult(req);

    if (!errors.isEmpty()) {
      return res.status(400).json({ errors });
    }

    const id = req.params.id;
    const { latitude, longitude } = req.query;

    try {
      const post = await getPost(id, latitude, longitude);

      return res.status(200).json(post);
    } catch (error) {
      return res
        .status(500)
        .json({ message: "There was an error getting the post", error: error });
    }
  }
);

router.post(
  "/",
  query("latitude").isFloat(),
  query("longitude").isFloat(),
  body("text").isString().notEmpty(),
  body("author").isString().notEmpty(),
  body("expiryDate")
    .isISO8601()
    .withMessage("expiryDate must be a valid ISO8601 date string.")
    .custom((value, { req }) => {
      const date = new Date(value);
      const currentDate = new Date();

      if (date <= currentDate) {
        throw new Error("expiryDate cannot have already passed.");
      }

      return true;
    })
    .optional(),
  body("postLongitude").isFloat().notEmpty(),
  body("postLatitude").isFloat().notEmpty(),
  async (req, res) => {
    const errors = validationResult(req);

    if (!errors.isEmpty()) {
      return res.status(400).json({ errors: errors.array() });
    }
    const { longitude: userLongitude, latitude: userLatitude } = req.query;
    const {
      text,
      author,
      expiryDate = null,
      postLongitude,
      postLatitude,
    } = req.body;

    try {
      const result = await createPost(
        text,
        author,
        expiryDate,
        postLongitude,
        postLatitude,
        userLongitude,
        userLatitude
      );
      res.status(201).json(result);
    } catch (error) {
      console.error(error);
      res.status(500).json({ message: error.message });
    }
  }
);

router.delete("/:id", param("id").isNumeric(), async (req, res) => {
  const errors = validationResult(req);

  if (!errors.isEmpty()) {
    return res.status(400).json({ errors: errors.array() });
  }

  const id = req.params.id;

  try {
    await deletePost(id);
    return res.status(204).send();
  } catch (error) {
    return res.status(500).json({
      message: "There was an error deleting the post.",
      error: error,
    });
  }
});

module.exports = router;
