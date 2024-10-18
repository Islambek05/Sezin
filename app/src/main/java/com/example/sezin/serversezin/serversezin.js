const express = require('express');
const cors = require('cors');
const { Pool } = require('pg');

const app = express();
const port = 3000;

// PostgreSQL connection
const pool = new Pool({
  user: 'postgres', // ваше имя пользователя
  host: '192.168.89.18', // IP-адрес сервера базы данных (например, '192.168.89.18')
  database: 'sezin_database', // название базы данных
  password: '2005Wppt++', // ваш пароль
  port: 5432, // порт PostgreSQL
});

app.use(cors());

// Маршрут для подключения к базе данных
app.get('/connect', async (req, res) => {
  try {
    const client = await pool.connect();
    res.send('Connected to the database successfully!');
    client.release();
  } catch (err) {
    res.status(500).send('Error connecting to the database: ' + err.message);
  }
});

app.listen(port, () => {
  console.log(`Server running on http://localhost:${port}`);
});
