const MoodEntry = require('../models/MoodEntry');


exports.createMoodEntry = async (req, res) => {
  try {
    const { date, moodValue, temperature, weatherType, cityName } = req.body;

 
    if (!date || !moodValue || !temperature || !weatherType || !cityName) {
      return res.status(400).json({ message: "Minden mező kötelező!" });
    }

    if (moodValue < 1 || moodValue > 5) {
      return res.status(400).json({ message: "A moodValue 1 és 5 között kell legyen!" });
    }

    const newEntry = new MoodEntry({
      date,
      moodValue,
      temperature,
      weatherType,
      cityName
    });

    await newEntry.save();

    res.status(201).json({
      message: "Mood entry sikeresen hozzáadva!",
      data: newEntry
    });

  } catch (error) {
    console.error("Hiba a mood entry létrehozásakor:", error);
    res.status(500).json({ message: "Szerver hiba!" });
  }

  getAllMoodEntries = async (req, res) => {
    try {
      const entries = await MoodEntry.find().sort({ date: -1 });
      res.json(entries);
    } catch (error) {
      console.error("Hiba a mood entry lekérésekor:", error);
      res.status(500).json({ message: "Szerver hiba!" });
    }   }
};
