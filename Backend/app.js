const dotenv = require('dotenv');
dotenv.config();
const express = require('express');
const app = express();
const userRoutes = require('./routes/userRoutes');
const captainRoutes = require('./routes/captainRoutes');
const cookieParser = require('cookie-parser');
const cors = require('cors');
app.use(cors());
app.use(express.json());
app.use(express.urlencoded({ extended: true }));
app.use(cookieParser());
app.use('/api/user', userRoutes);
app.use('/api/captain', captainRoutes);

app.get('/', (req, res) => {
  res.send('Hello, World!');
});
module.exports = app;