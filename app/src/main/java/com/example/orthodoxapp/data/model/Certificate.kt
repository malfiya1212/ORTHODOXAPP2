package com.example.orthodoxapp.data.model

import androidx.room.*

@Entity(
    tableName = "certificates",
    foreignKeys = [
        ForeignKey(entity = User::class, parentColumns = ["id"], childColumns = ["userId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = Church::class, parentColumns = ["id"], childColumns = ["churchId"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index("userId"), Index("churchId")]
)
data class Certificate(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val churchId: Long,
    val title: String,
    val awardType: String, // EVENT, PROGRAM, BAPTISM, SERVICE
    val description: String? = null,
    val issuedDate: Long = System.currentTimeMillis(),
    val issuedBy: Long,
    val certificateId: String // Unique serial number
)
