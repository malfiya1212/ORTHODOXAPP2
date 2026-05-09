package com.example.orthodoxapp.data.model

import androidx.room.*

/**
 * Ethiopian Orthodox Church Administrative Roles
 */
enum class UserRole {
    SYNOD_ADMIN,    // Holy Synod - supreme church authority
    DIOCESE_ADMIN,  // Diocese / Eparchy - bishop's area
    CHURCH_ADMIN,   // Individual church / parish
    ACCOUNTANT,     // Church financial officer
    AUDITOR,        // Read-only auditor
    MEMBER          // Regular member
}

// --- USER CORE TABLES ---

@Entity(tableName = "roles")
data class Role(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String // SYNOD_ADMIN, DIOCESE_ADMIN, CHURCH_ADMIN, etc.
)

@Entity(tableName = "permissions")
data class Permission(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String // INCOME_CREATE, EXPENSE_APPROVE, etc.
)

// ============================================================
//  ETHIOPIAN ORTHODOX CHURCH HIERARCHY
//
//  Holy Synod (ቅዱስ ሲኖዶስ)
//      ↓
//  Diocese / Eparchy (ሀገረ ስብከት) — Bishop's Area
//      ↓
//  Church / Parish (ቤተ ክርስቲያን)
//      ↓
//  Members (ምዕመናን)
// ============================================================

/**
 * Diocese (ሀገረ ስብከት) — Ethiopian Orthodox episcopal territory.
 */
@Entity(tableName = "dioceses")
data class Diocese(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val bishopName: String? = null,
    val description: String? = null
)


/**
 * Individual Church / Parish (ቤተ ክርስቲያን).
 */
    @Entity(
        tableName = "churches",
        foreignKeys = [
            ForeignKey(entity = Diocese::class, parentColumns = ["id"], childColumns = ["dioceseId"], onDelete = ForeignKey.SET_NULL)
        ],
        indices = [
            Index("dioceseId")
        ]
    )
    data class Church(
        @PrimaryKey(autoGenerate = true) val id: Long = 0,
        val name: String,
        val location: String? = null,
        val latitude: Double? = null,
        val longitude: Double? = null,
        val dioceseId: Long? = null,
        val status: String = "ACTIVE"
    )

/**
 * App User — linked to their level in the church hierarchy.
 * - SYNOD_ADMIN: no dioceseId / churchId needed (sees everything)
 * - DIOCESE_ADMIN: has dioceseId
 * - CHURCH_ADMIN / ACCOUNTANT: has dioceseId + churchId
 * - MEMBER: has churchId
 */
@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val email: String,
    val roleId: Long? = null,
    val passwordHash: String,
    val churchId: Long? = null,
    val dioceseId: Long? = null,
    val phone: String? = null,
    val position: String? = null, // e.g., "Priest", "Chief Accountant", "Deacon"
    val status: String = "ACTIVE",
    val customChurchName: String? = null,
    val role: String? = null
)

@Entity(
    tableName = "user_roles",
    primaryKeys = ["userId", "roleId"],
    foreignKeys = [
        ForeignKey(entity = User::class, parentColumns = ["id"], childColumns = ["userId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = Role::class, parentColumns = ["id"], childColumns = ["roleId"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index("userId"), Index("roleId")]
)
data class UserRoleJoin(
    val userId: Long,
    val roleId: Long
)

@Entity(
    tableName = "role_permissions",
    primaryKeys = ["roleId", "permissionId"],
    foreignKeys = [
        ForeignKey(entity = Role::class, parentColumns = ["id"], childColumns = ["roleId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = Permission::class, parentColumns = ["id"], childColumns = ["permissionId"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index("roleId"), Index("permissionId")]
)
data class RolePermissionJoin(
    val roleId: Long,
    val permissionId: Long
)

// --- FINANCIAL CORE SYSTEM ---

@Entity(
    tableName = "accounts",
    foreignKeys = [
        ForeignKey(entity = Church::class, parentColumns = ["id"], childColumns = ["churchId"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index("churchId")]
)
data class Account(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val churchId: Long,
    val name: String, // Cash, Bank, Project
    val balance: Double = 0.0
)

@Entity(
    tableName = "income",
    foreignKeys = [
        ForeignKey(entity = Church::class, parentColumns = ["id"], childColumns = ["churchId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = Account::class, parentColumns = ["id"], childColumns = ["accountId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = User::class, parentColumns = ["id"], childColumns = ["createdBy"], onDelete = ForeignKey.SET_NULL),
        ForeignKey(entity = User::class, parentColumns = ["id"], childColumns = ["approvedBy"], onDelete = ForeignKey.SET_NULL)
    ],
    indices = [Index("churchId"), Index("accountId"), Index("createdBy"), Index("approvedBy")]
)
data class Income(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val churchId: Long,
    val accountId: Long,
    val amount: Double,
    val source: String, // Tithe, Donation
    val status: String = "PENDING", // PENDING, APPROVED
    val createdBy: Long,
    val approvedBy: Long? = null,
    val category: String? = "Tithe",
    val date: Long = System.currentTimeMillis(),
    val description: String? = null,
    val paymentMethod: String? = "Cash",
    val referenceNumber: String? = null
)

@Entity(
    tableName = "expenses",
    foreignKeys = [
        ForeignKey(entity = Church::class, parentColumns = ["id"], childColumns = ["churchId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = Account::class, parentColumns = ["id"], childColumns = ["accountId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = User::class, parentColumns = ["id"], childColumns = ["requestedBy"], onDelete = ForeignKey.SET_NULL),
        ForeignKey(entity = User::class, parentColumns = ["id"], childColumns = ["approvedBy"], onDelete = ForeignKey.SET_NULL)
    ],
    indices = [Index("churchId"), Index("accountId"), Index("requestedBy"), Index("approvedBy")]
)
data class Expense(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val churchId: Long,
    val accountId: Long,
    val amount: Double,
    val category: String,
    val status: String = "PENDING", // PENDING, APPROVED, REJECTED
    val requestedBy: Long,
    val approvedBy: Long? = null,
    val recipient: String? = null,
    val date: Long = System.currentTimeMillis(),
    val description: String? = null,
    val paymentMethod: String? = "Cash",
    val referenceNumber: String? = null
)

// --- CHURE SYSTEM (UNIQUE FEATURE) ---

@Entity(
    tableName = "chure_groups",
    foreignKeys = [
        ForeignKey(entity = Church::class, parentColumns = ["id"], childColumns = ["churchId"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index("churchId")]
)
data class ChureGroup(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val churchId: Long,
    val name: String,
    val contributionAmount: Double = 0.0,
    val frequency: String = "",
    val startDate: Long = 0L,
    val rotationIndex: Int = 0
)

@Entity(
    tableName = "chure_members",
    foreignKeys = [
        ForeignKey(entity = ChureGroup::class, parentColumns = ["id"], childColumns = ["groupId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = User::class, parentColumns = ["id"], childColumns = ["userId"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index("groupId"), Index("userId")]
)
data class ChureMember(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val groupId: Long,
    val userId: Long,
    val contributionAmount: Double,
    val status: String = "ACTIVE"
)

@Entity(
    tableName = "chure_payments",
    foreignKeys = [
        ForeignKey(entity = ChureMember::class, parentColumns = ["id"], childColumns = ["memberId"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index("memberId")]
)
data class ChurePayment(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val memberId: Long,
    val amount: Double,
    val paymentDate: Long = System.currentTimeMillis(),
    val periodNumber: Int = 0,
    val status: String = "PAID" // PAID, LATE
)

@Entity(
    tableName = "chure_payouts",
    foreignKeys = [
        ForeignKey(entity = ChureGroup::class, parentColumns = ["id"], childColumns = ["groupId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = User::class, parentColumns = ["id"], childColumns = ["recipientUserId"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index("groupId"), Index("recipientUserId")]
)
data class ChurePayout(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val groupId: Long,
    val recipientUserId: Long,
    val amount: Double,
    val payoutDate: Long = System.currentTimeMillis(),
    val periodNumber: Int = 0
)

// --- AUDIT SYSTEM ---

@Entity(
    tableName = "audit_logs",
    foreignKeys = [
        ForeignKey(entity = User::class, parentColumns = ["id"], childColumns = ["userId"], onDelete = ForeignKey.SET_NULL)
    ],
    indices = [Index("userId")]
)
data class AuditLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long?,
    val action: String,
    val details: String? = null,
    val tableName: String = "",
    val recordId: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val oldValue: String? = null,
    val newValue: String? = null
)

// --- NOTIFICATION SYSTEM ---

@Entity(
    tableName = "notifications",
    foreignKeys = [
        ForeignKey(entity = User::class, parentColumns = ["id"], childColumns = ["userId"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index("userId")]
)
data class Notification(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val title: String,
    val message: String,
    val type: String, // Payment, Approval, Alert
    val isRead: Boolean = false,
    val date: Long = System.currentTimeMillis()
)

// --- DOCUMENT SYSTEM ---

@Entity(
    tableName = "documents",
    foreignKeys = [
        ForeignKey(entity = User::class, parentColumns = ["id"], childColumns = ["uploadedBy"], onDelete = ForeignKey.SET_NULL),
        ForeignKey(entity = Church::class, parentColumns = ["id"], childColumns = ["churchId"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index("uploadedBy"), Index("churchId")]
)
data class Document(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fileUrl: String,
    val fileType: String, // Receipt, Report
    val uploadedBy: Long?,
    val churchId: Long,
    val createdAt: Long = System.currentTimeMillis(),
    val referenceType: String? = null,
    val referenceId: Long? = null
)

@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(entity = Account::class, parentColumns = ["id"], childColumns = ["accountId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = Church::class, parentColumns = ["id"], childColumns = ["churchId"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index("accountId"), Index("churchId")]
)
data class FinancialTransaction(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val accountId: Long,
    val churchId: Long,
    val amount: Double,
    val type: String, // INCOME, EXPENSE
    val description: String,
    val date: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "reminders",
    foreignKeys = [
        ForeignKey(entity = User::class, parentColumns = ["id"], childColumns = ["userId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = Church::class, parentColumns = ["id"], childColumns = ["churchId"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index("userId"), Index("churchId")]
)
data class Reminder(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val churchId: Long,
    val title: String,
    val description: String,
    val dueDate: Long,
    val isCompleted: Boolean = false
)

@Entity(
    tableName = "budgets",
    foreignKeys = [
        ForeignKey(entity = Church::class, parentColumns = ["id"], childColumns = ["churchId"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index("churchId")]
)
data class Budget(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val churchId: Long,
    val category: String,
    val amount: Double,
    val period: String, // Monthly, Yearly
    val startDate: Long,
    val endDate: Long
)

@Entity(
    tableName = "ledger",
    foreignKeys = [
        ForeignKey(entity = Account::class, parentColumns = ["id"], childColumns = ["accountId"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index("accountId")]
)
data class LedgerEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val accountId: Long,
    val churchId: Long = 0,
    val description: String = "",
    val debit: Double = 0.0,
    val credit: Double = 0.0,
    val referenceType: String, // Income, Expense
    val referenceId: Long,
    val date: Long = System.currentTimeMillis()
)

// --- SALARY MANAGEMENT SYSTEM ---

@Entity(
    tableName = "employees",
    foreignKeys = [
        ForeignKey(entity = Church::class, parentColumns = ["id"], childColumns = ["churchId"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index("churchId")]
)
data class Employee(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fullName: String,
    val role: String, // Pastor, Cleaner, Accountant, etc.
    val churchId: Long,
    val phone: String? = null,
    val salary: Double = 0.0,
    val isActive: Boolean = true
)

@Entity(
    tableName = "salaries",
    foreignKeys = [
        ForeignKey(entity = Employee::class, parentColumns = ["id"], childColumns = ["employeeId"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index("employeeId")]
)
data class Salary(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val employeeId: Long,
    val amount: Double,
    val month: Int,
    val year: Int,
    val status: String = "PENDING", // PENDING, PAID, APPROVED
    val paidDate: Long? = null
)

@Entity(
    tableName = "salary_payments",
    foreignKeys = [
        ForeignKey(entity = Salary::class, parentColumns = ["id"], childColumns = ["salaryId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = User::class, parentColumns = ["id"], childColumns = ["approvedBy"], onDelete = ForeignKey.SET_NULL)
    ],
    indices = [Index("salaryId"), Index("approvedBy")]
)
data class SalaryPayment(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val salaryId: Long,
    val amountPaid: Double,
    val paymentMethod: String, // Cash, Bank, Telebirr
    val transactionDate: Long = System.currentTimeMillis(),
    val approvedBy: Long?
)

@Entity(
    tableName = "salary_ledger",
    foreignKeys = [
        ForeignKey(entity = Employee::class, parentColumns = ["id"], childColumns = ["employeeId"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index("employeeId")]
)
data class SalaryLedgerEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val employeeId: Long,
    val debit: Double = 0.0,
    val credit: Double = 0.0,
    val balanceAfter: Double,
    val referenceId: Long,
    val date: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "financial_approvals",
    foreignKeys = [
        ForeignKey(entity = User::class, parentColumns = ["id"], childColumns = ["reviewerId"], onDelete = ForeignKey.SET_NULL)
    ],
    indices = [Index("reviewerId")]
)
data class FinancialApproval(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val entityType: String, // INCOME, EXPENSE, CHURCH
    val entityId: Long,
    val reviewerId: Long?,
    val status: String = "PENDING", // PENDING, APPROVED, REJECTED
    val comments: String? = null,
    val stage: Int = 1,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "church_assets",
    foreignKeys = [
        ForeignKey(entity = Church::class, parentColumns = ["id"], childColumns = ["churchId"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index("churchId")]
)
data class ChurchAsset(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val type: String, // Land, Sacred Object, Vehicle, etc.
    val value: Double,
    val churchId: Long,
    val description: String? = null,
    val condition: String = "GOOD", // EXCELLENT, GOOD, FAIR, POOR
    val purchaseDate: Long = System.currentTimeMillis()
)


@Entity(
    tableName = "salary_approvals",
    foreignKeys = [
        ForeignKey(entity = Salary::class, parentColumns = ["id"], childColumns = ["salaryId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = User::class, parentColumns = ["id"], childColumns = ["approvedBy"], onDelete = ForeignKey.SET_NULL)
    ],
    indices = [Index("salaryId"), Index("approvedBy")]
)
data class SalaryApproval(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val salaryId: Long,
    val approvedBy: Long?,
    val status: String, // APPROVED, REJECTED
    val comment: String?,
    val date: Long = System.currentTimeMillis()
)


@Entity(
    tableName = "payments",
    foreignKeys = [
        ForeignKey(entity = Church::class, parentColumns = ["id"], childColumns = ["churchId"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index("churchId")]
)
data class Payment(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val paymentType: String,
    val amount: Double,
    val originalAmount: Double,
    val currencyCode: String,
    val paymentDate: Long = System.currentTimeMillis(),
    val churchId: Long,
    val memberId: Long? = null,
    val paymentMethod: String,
    val referenceNumber: String? = null,
    val status: String = "Pending",
    val receiptNumber: String = ""
)

@Entity(tableName = "sync_queue")
data class SyncQueue(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val entityType: String,
    val entityId: Long,
    val action: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isSynced: Int = 0,
    val retryCount: Int = 0,
    val lastError: String? = null
)

@Entity(tableName = "sync_logs")
data class SyncLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val batchId: String,
    val startTime: Long,
    val endTime: Long,
    val itemsProcessed: Int,
    val status: String,
    val errorMessage: String? = null
)

@Entity(
    tableName = "login_history",
    foreignKeys = [
        ForeignKey(entity = User::class, parentColumns = ["id"], childColumns = ["userId"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index("userId")]
)
data class LoginHistory(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val loginTime: Long = System.currentTimeMillis(),
    val deviceName: String? = null,
    val ipAddress: String? = null,
    val status: String = "SUCCESS"
)

@Entity(
    tableName = "reports",
    foreignKeys = [
        ForeignKey(entity = Church::class, parentColumns = ["id"], childColumns = ["churchId"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index("churchId")]
)
data class Report(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val churchId: Long,
    val title: String,
    val type: String,
    val format: String,
    val generatedAt: Long = System.currentTimeMillis(),
    val fileUrl: String? = null,
    val generatedBy: Long
)

// --- DTOs for UI ---

data class PaymentReceipt(
    val churchName: String,
    val amount: Double,
    val receiptNumber: String,
    val paymentDate: String,
    val paymentType: String,
    val paymentMethod: String,
    val referenceNumber: String? = null,
    val memberName: String? = null,
    val generatedBy: String,
    val verificationUrl: String
)
