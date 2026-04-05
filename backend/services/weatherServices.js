const axios = require('axios');

async function getWeatherByCity(cityName) {
  const apiKey = process.env.WEATHER_API_KEY;

  const url = `https://api.openweathermap.org/data/2.5/weather?q=${cityName}&units=metric&appid=${apiKey}`;

  const response = await axios.get(url);
  console.log('Weather API response:', response.data); // Debug log

  return {
    temperature: response.data.main.temp,
    weatherType: response.data.weather[0].main,
    cityName: response.data.name
  };
}

module.exports = { getWeatherByCity };
