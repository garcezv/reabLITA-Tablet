package com.audiometry.threshold.database.dao

import androidx.room.*
import com.audiometry.threshold.database.entity.User

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE email = :email AND password = :password LIMIT 1")
    suspend fun login(email: String, password: String): User?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(user: User)

    @Query("SELECT COUNT(*) FROM users")
    suspend fun count(): Int
}
