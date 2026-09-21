package com.example.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface JarvisDao {
    // Chat Messages
    @Query("SELECT * FROM chat_messages ORDER BY timestamp ASC")
    fun getAllMessages(): Flow<List<ChatMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessage): Long

    @Query("DELETE FROM chat_messages")
    suspend fun clearChat()

    @Query("DELETE FROM chat_messages WHERE id = :msgId")
    suspend fun deleteMessageById(msgId: Long)

    // Generated Files
    @Query("SELECT * FROM generated_files ORDER BY timestamp DESC")
    fun getAllFiles(): Flow<List<GeneratedFile>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFile(file: GeneratedFile): Long

    @Delete
    suspend fun deleteFile(file: GeneratedFile)

    // Eaglercraft Servers
    @Query("SELECT * FROM eaglercraft_servers ORDER BY id DESC")
    fun getAllServers(): Flow<List<EaglercraftServer>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertServer(server: EaglercraftServer)

    @Update
    suspend fun updateServer(server: EaglercraftServer)

    @Delete
    suspend fun deleteServer(server: EaglercraftServer)

    // Integration Messages
    @Query("SELECT * FROM integration_messages ORDER BY timestamp DESC")
    fun getAllIntegrationMessages(): Flow<List<IntegrationMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIntegrationMessage(msg: IntegrationMessage)

    // User Profile
    @Query("SELECT * FROM user_profile WHERE id = 1")
    fun getUserProfile(): Flow<UserProfile?>

    @Query("SELECT * FROM user_profile WHERE id = 1")
    suspend fun getUserProfileSync(): UserProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveUserProfile(profile: UserProfile)
}
