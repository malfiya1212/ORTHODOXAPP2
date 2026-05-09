package com.example.orthodoxapp.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.orthodoxapp.data.local.dao.FinanceDao
import com.example.orthodoxapp.data.local.dao.UserDao
import com.example.orthodoxapp.data.local.dao.SyncDao
import com.example.orthodoxapp.data.model.*

@Database(
    entities = [
        User::class,
        Role::class,
        Diocese::class,
        Church::class,
        Income::class,
        Expense::class,
        SyncQueue::class,
        AuditLog::class,
        FinancialTransaction::class,
        Account::class,
        ChureGroup::class,
        ChureMember::class,
        ChurePayment::class,
        ChurePayout::class,
        Payment::class,
        Permission::class,
        UserRoleJoin::class,
        RolePermissionJoin::class,
        Notification::class,
        Document::class,
        Reminder::class,
        Budget::class,
        LedgerEntry::class,
        Employee::class,
        Salary::class,
        SalaryPayment::class,
        SalaryLedgerEntry::class,
        SalaryApproval::class,
        SyncLog::class,
        LoginHistory::class,
        Report::class,
        ChurchAsset::class,
        FinancialApproval::class
    ],
    version = 21,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun financeDao(): FinanceDao
    abstract fun userDao(): UserDao
    abstract fun syncDao(): SyncDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: android.content.Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = androidx.room.Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "orthodox_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
