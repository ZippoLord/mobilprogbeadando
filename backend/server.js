const express = require('express')
const cors = require('cors')
const mongoose = require('mongoose')
const dotenv = require('dotenv')
const axios = require('axios');
const moodRoutes = require('./routes/moodRoutes');
const weatherRoutes = require('./routes/weatherRoutes');

const app = express()

dotenv.config()

app.use(cors({
    origin: '*',
    credentials: true,
}))

app.use(express.json());
app.use(express.urlencoded({ extended: true }));

app.get('/', (req, res) => {
    res.send('Server is running!');
});

//routes
app.use('/api', moodRoutes);
app.use('/api', weatherRoutes);

async function startServer() {
    try {
        await mongoose.connect(process.env.DBCONNECTION, { 
            useNewUrlParser: true, 
            useUnifiedTopology: true 
        });
        console.log("Connected to database");
        mongoose.set("bufferCommands", false);

        const PORT = process.env.PORT || 3000;
        const server = app.listen(PORT, '0.0.0.0', () => {
            console.log(`Server running on port ${PORT}`);
        });
    } catch (err) {
        console.log("Connection error:", err);
        process.exit(1);
    }
}

startServer();