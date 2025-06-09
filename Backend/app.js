
const dotenv = require('dotenv');
dotenv.config();
const express = require('express');
const app = express();
const userRoutes = require('./routes/userRoutes');

const cors = require('cors');
app.use(cors());
app.use(express.json());
app.use(express.urlencoded({ extended: true }));

app.use('/api/user', userRoutes);

app.get('/', (req, res) => {
  res.send('Hello, World!');
});
module.exports = app;