const dotenv = require('dotenv');
dotenv.config();
const express = require('express');
const app = express();
const connectDB = require('./db/db');
const userRoutes = require('./routes/userRoutes');
const captainRoutes = require('./routes/captainRoutes');
const mapsRoutes = require('./routes/mapsRoutes');
const rideRoute = require('./routes/rideRoutes');
const cookieParser = require('cookie-parser');
const cors = require('cors');
// Connect to the database
connectDB();

app.use(cors());
app.use(express.json());
app.use(express.urlencoded({ extended: true }));
app.use(cookieParser());

app.use('/api/user', userRoutes);
app.use('/api/captain', captainRoutes);
app.use('/api/maps', mapsRoutes);
app.use('/api/rides', rideRoute);

app.get('/', (req, res) => {
  res.send('Hello, World!');
});
module.exports = app;