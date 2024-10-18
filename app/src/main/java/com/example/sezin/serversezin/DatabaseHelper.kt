package com.example.sezin.serversezin

import com.example.sezin.user.User
import java.sql.Connection
import java.sql.DriverManager

class DatabaseHelper {

    private val url = "jdbc:postgresql://localhost:5432/sezin_database"
    private val username = "postgres"
    private val password = "2005Wppt++"

    private fun connect(): Connection {
        return DriverManager.getConnection(url, username, password)
    }

    fun saveUser(user: User): Boolean {
        try {
            val connection = connect()
            val statement = connection.createStatement()

            val createTableSQL = """
                CREATE TABLE IF NOT EXISTS users (
                    id SERIAL PRIMARY KEY,
                    firstname VARCHAR(100),
                    lastname VARCHAR(100),
                    email VARCHAR(100),
                    phone_number VARCHAR(20),
                    password_hash INT
                )
            """
            statement.executeUpdate(createTableSQL)

            if (!isUserExists(user.email, user.phoneNumber)) {
                val insertSQL = """
                    INSERT INTO users (firstname, lastname, email, phone_number, password_hash) 
                    VALUES ('${user.firstname}', '${user.lastname}', '${user.email}', '${user.phoneNumber}', ${user.passwordhash})
                """
                statement.executeUpdate(insertSQL)
            }

            statement.close()
            connection.close()
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    fun isUserExists(email: String, phoneNumber: String): Boolean {
        try {
            val connection = connect()
            val statement = connection.prepareStatement("SELECT * FROM users WHERE email = ? OR phone_number = ?")

            statement.setString(1, email)
            statement.setString(2, phoneNumber)

            val resultSet = statement.executeQuery()

            val userExists = resultSet.next() // Возвращает true, если найдена хотя бы одна строка

            resultSet.close()
            statement.close()
            connection.close()

            return userExists
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }


    fun getAllUsers(): List<User> {
        val users = mutableListOf<User>()

        try {
            val connection = connect()
            val statement = connection.createStatement()

            val resultSet = statement.executeQuery("SELECT * FROM users")

            while (resultSet.next()) {
                val firstname = resultSet.getString("firstname")
                val lastname = resultSet.getString("lastname")
                val email = resultSet.getString("email")
                val phoneNumber = resultSet.getString("phone_number")
                val passwordHash = resultSet.getInt("password_hash")

                val user = User(firstname, lastname, email, phoneNumber, passwordHash)
                users.add(user)
            }

            resultSet.close()
            statement.close()
            connection.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return users
    }

}