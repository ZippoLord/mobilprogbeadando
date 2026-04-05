const MoodEntry = require('../models/MoodEntry');


exports.createMoodEntry = async (req, res) => {
  try {
    const { date, moodValue, temperature, weatherType, cityName } = req.body;

 
    if (date == null || moodValue == null || temperature == null || !weatherType || !cityName) {
      return res.status(400).json({ message: "Minden mező kötelező!" });
    }

    if (moodValue < 1 || moodValue > 5) {
      return res.status(400).json({ message: "A moodValue 1 és 5 között kell legyen!" });
    }

    const parsedDate = Number(date);
    const newEntry = new MoodEntry({
      date: Number.isNaN(parsedDate) ? Date.now() : parsedDate,
      moodValue,
      temperature,
      weatherType,
      cityName
    });

    await newEntry.save();

    res.status(201).json({
      message: "Mood entry sikeresen hozzáadva!",
      data: {
        _id: newEntry._id,
        date: new Date(newEntry.date).getTime(),
        moodValue: newEntry.moodValue,
        temperature: newEntry.temperature,
        weatherType: newEntry.weatherType,
        cityName: newEntry.cityName
      }
    });

  } catch (error) {
    console.error("Hiba a mood entry létrehozásakor:", error);
    res.status(500).json({ message: "Szerver hiba!" });
  }
};

exports.getAllMoodEntries = async (req, res) => {
  try {
    const entries = await MoodEntry.find().sort({ date: -1 });
    const normalized = entries.map((entry) => ({
      _id: entry._id,
      date: new Date(entry.date).getTime(),
      moodValue: entry.moodValue,
      temperature: entry.temperature,
      weatherType: entry.weatherType,
      cityName: entry.cityName
    }));
    res.json(normalized);
  } catch (error) {
    console.error("Hiba a mood entry lekérésekor:", error);
    res.status(500).json({ message: "Szerver hiba!" });
  }
};

exports.deleteMoodEntry = async (req, res) => {
  try {
    const { id } = req.params;
    const deleted = await MoodEntry.findByIdAndDelete(id);

    if (!deleted) {
      return res.status(404).json({ message: "Bejegyzes nem talalhato" });
    }

    res.json({ message: "Bejegyzes torolve" });
  } catch (error) {
    console.error("Hiba a mood entry torlesekor:", error);
    res.status(500).json({ message: "Szerver hiba!" });
  }
};
