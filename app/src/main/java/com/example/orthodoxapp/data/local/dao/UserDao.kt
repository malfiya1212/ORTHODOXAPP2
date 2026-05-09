package com.example.orthodoxapp.data.local.dao

import androidx.room.*
import com.example.orthodoxapp.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User): Long

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): User?

    @Query("SELECT * FROM users ORDER BY name ASC")
    fun getAllUsers(): Flow<List<User>>

    @Query("SELECT * FROM users WHERE churchId = :churchId")
    fun getUsersByChurch(churchId: Long): Flow<List<User>>

    @Query("SELECT * FROM users WHERE dioceseId = :dioceseId")
    fun getUsersByDiocese(dioceseId: Long): Flow<List<User>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDiocese(diocese: Diocese): Long

    @Query("SELECT * FROM dioceses ORDER BY name ASC")
    fun getAllDioceses(): Flow<List<Diocese>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChurch(church: Church): Long

    @Query("SELECT * FROM churches ORDER BY name ASC")
    fun getAllChurches(): Flow<List<Church>>

    @Query("SELECT * FROM churches WHERE dioceseId = :dioceseId ORDER BY name ASC")
    fun getChurchesByDiocese(dioceseId: Long): Flow<List<Church>>
}
