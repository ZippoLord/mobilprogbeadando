const { getWeatherByCity } = require('../services/weatherServices');

exports.getWeatherByCity = async (req, res) => {
  try {
    const city = req.params.city;
    console.log("BACKEND CITY:", req.params.city);
    const weather = await getWeatherByCity(city);
    res.json(weather);
  } catch (error) {
    console.error('Weather API hiba:', error.response?.data || error.message);
    res.status(500).json({ message: 'Nem sikerült lekérni az időjárást' });
  }
};
