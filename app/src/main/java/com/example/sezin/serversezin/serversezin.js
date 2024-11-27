const express = require('express');
const cors = require('cors');
const bodyParser = require('body-parser');
const { Pool } = require('pg');

const app = express();
const port = 3000;

const pool = new Pool({
  user: 'postgres',
  host: '192.168.89.18',
  database: 'sezin_database',
  password: '2005Wppt++',
  port: 5432,
});

app.use(cors());
app.use(bodyParser.json()); // Для работы с JSON-данными

// Маршрут для подключения к базе данных
app.get('/connect', async (req, res) => {
  try {
    const client = await pool.connect();
    res.json({ message: 'Connected to the database successfully!' });
    client.release();
  } catch (err) {
    res.status(500).json({ message: 'Error connecting to the database: ' + err.message });
  }
});

// Маршрут для регистрации пользователя
app.post('/register', async (req, res) => {
  const { firstname, lastname, email, phoneNumber, passwordhash } = req.body;

  if (!firstname || !lastname || !email || !phoneNumber || !passwordhash) {
    return res.status(400).json({ message: 'All fields are required' });
  }

  try {
    const client = await pool.connect();

    // Проверка, существует ли пользователь
    const userCheck = await client.query('SELECT * FROM users WHERE email = $1 OR phone_number = $2', [email, phoneNumber]);
    if (userCheck.rows.length > 0) {
      client.release();
      return res.status(400).json({ message: 'User already exists' });
    }

    // Вставка нового пользователя
    const insertQuery = `
      INSERT INTO users (firstname, lastname, email, phone_number, password_hash)
      VALUES ($1, $2, $3, $4, $5)
      RETURNING id
    `;
    const result = await client.query(insertQuery, [firstname, lastname, email, phoneNumber, passwordhash]);

    console.log('User registered: ', result.rows);
    client.release();
    res.json({ message: 'Registration successful' });
  } catch (err) {
    res.status(500).json({ message: 'Error registering user: ' + err.message });
  }
});

// Запуск сервера
app.listen(port, () => {
  console.log(`Server running on http://localhost:${port}`);
});
