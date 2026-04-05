const express = require('express');
const router = express.Router();
const moodController = require('../controllers/moodController');


router.post('/mood', moodController.createMoodEntry);
router.get('/mood/getallmoods', moodController.getAllMoodEntries);

module.exports = router;
