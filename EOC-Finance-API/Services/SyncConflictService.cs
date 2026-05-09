using EOC.Finance.API.Data;
using EOC.Finance.API.Models;
using Microsoft.EntityFrameworkCore;
using System;
using System.Threading.Tasks;

namespace EOC.Finance.API.Services
{
    public enum SyncResolution
    {
        Accepted,
        AutoMerged,
        ConflictRequiresManualResolution
    }

    public class SyncConflictResult
    {
        public SyncResolution Status { get; set; }
        public string Message { get; set; } = string.Empty;
        public object? ServerRecord { get; set; }
    }

    public interface ISyncConflictService
    {
        Task<SyncConflictResult> ResolveIncomeSyncAsync(Income offlineRecord, DateTime mobileLastModified);
    }

    public class SyncConflictService : ISyncConflictService
    {
        private readonly AppDbContext _context;

        public SyncConflictService(AppDbContext context)
        {
            _context = context;
        }

        public async Task<SyncConflictResult> ResolveIncomeSyncAsync(Income offlineRecord, DateTime mobileLastModified)
        {
            // Check if the record already exists on the server (an UPDATE scenario)
            var serverRecord = await _context.IncomeRecords.FindAsync(offlineRecord.Id);

            if (serverRecord == null)
            {
                // It's a brand new insert from offline mode -> Accept immediately
                return new SyncConflictResult { Status = SyncResolution.Accepted, Message = "New record accepted." };
            }

            // CONFLICT DETECTION: 
            // If the server record was modified AFTER the mobile device went offline, we have a conflict.
            // Using the 'Date' field or 'ApprovedBy' as a proxy for server modification for this example.
            if (serverRecord.Date > mobileLastModified)
            {
                // Auto-Merge Rule 1: If it's just a status change (e.g. Diocese approved it), keep the server status but accept mobile amounts.
                if (serverRecord.Status == "APPROVED" && offlineRecord.Status == "PENDING")
                {
                    return new SyncConflictResult 
                    { 
                        Status = SyncResolution.AutoMerged, 
                        Message = "Server was already approved. Auto-merging mobile data but keeping APPROVED status." 
                    };
                }

                // If critical financial data was changed on both ends, throw a manual resolution flag
                if (serverRecord.Amount != offlineRecord.Amount)
                {
                    // Log the conflict for the auditor
                    var audit = new AuditLog
                    {
                        Action = "SYNC_CONFLICT_DETECTED",
                        TableName = "Income",
                        RecordId = serverRecord.Id,
                        Details = $"Mobile tried to sync amount {offlineRecord.Amount}, but Server has {serverRecord.Amount}.",
                        Timestamp = DateTime.UtcNow
                    };
                    _context.AuditLogs.Add(audit);
                    await _context.SaveChangesAsync();

                    return new SyncConflictResult 
                    { 
                        Status = SyncResolution.ConflictRequiresManualResolution, 
                        Message = "Critical data mismatch. Requires manual Diocese resolution.",
                        ServerRecord = serverRecord
                    };
                }
            }

            // If no conflict, it's accepted
            return new SyncConflictResult { Status = SyncResolution.Accepted, Message = "Record updated successfully." };
        }
    }
}
