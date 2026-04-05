const express = require('express');
const weatherController = require('../controllers/weatherController');
const router = express.Router();


router.get('/weather/:city', weatherController.getWeatherByCity);

module.exports = router;
