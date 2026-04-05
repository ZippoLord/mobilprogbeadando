const mongoose = require('mongoose');

const MoodEntrySchema = new mongoose.Schema({
  date: {
    type: Date,
    required: true
  },
  moodValue: {
    type: Number,
    min: 1,
    max: 5,
    required: true
  },
  temperature: {
    type: Number,
    required: true
  },
  weatherType: {
    type: String,
    required: true
  },
  cityName: {
    type: String,
    required: true
  }
});

module.exports = mongoose.model('MoodEntry', MoodEntrySchema);
