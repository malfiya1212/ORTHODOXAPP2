using EOC.Finance.API.Data;
using EOC.Finance.API.Models;
using Microsoft.EntityFrameworkCore;
using System;
using System.Threading.Tasks;

namespace EOC.Finance.API.Services
{
    public interface IApprovalWorkflowService
    {
        Task<bool> ApproveExpenseAsync(long expenseId, long approverUserId, string role);
        Task<bool> RejectExpenseAsync(long expenseId, long approverUserId, string reason);
    }

    public class ApprovalWorkflowService : IApprovalWorkflowService
    {
        private readonly AppDbContext _context;

        public ApprovalWorkflowService(AppDbContext context)
        {
            _context = context;
        }

        public async Task<bool> ApproveExpenseAsync(long expenseId, long approverUserId, string role)
        {
            var expense = await _context.Expenses.FindAsync(expenseId);
            if (expense == null) return false;

            // Multi-level approval chain logic
            if (expense.Status == "PENDING" && role == "CHURCH_ADMIN")
            {
                expense.Status = "APPROVED_BY_CHURCH";
            }
            else if (expense.Status == "APPROVED_BY_CHURCH" && role == "DIOCESE_ADMIN")
            {
                expense.Status = "FINAL_APPROVED";
            }
            else if (expense.Status == "PENDING" && role == "DIOCESE_ADMIN")
            {
                // Diocese override
                expense.Status = "FINAL_APPROVED";
            }
            else
            {
                return false; // Invalid state transition
            }

            expense.ApprovedBy = approverUserId;

            // Log Approval
            var audit = new AuditLog
            {
                UserId = approverUserId,
                Action = $"EXPENSE_APPROVED_TO_{expense.Status}",
                TableName = "Expenses",
                RecordId = expense.Id,
                Details = $"Expense of {expense.Amount} advanced to {expense.Status}.",
                Timestamp = DateTime.UtcNow
            };
            
            _context.AuditLogs.Add(audit);
            await _context.SaveChangesAsync();

            return true;
        }

        public async Task<bool> RejectExpenseAsync(long expenseId, long approverUserId, string reason)
        {
            var expense = await _context.Expenses.FindAsync(expenseId);
            if (expense == null) return false;

            expense.Status = "REJECTED";
            
            var audit = new AuditLog
            {
                UserId = approverUserId,
                Action = "EXPENSE_REJECTED",
                TableName = "Expenses",
                RecordId = expense.Id,
                Details = $"Expense rejected. Reason: {reason}",
                Timestamp = DateTime.UtcNow
            };

            _context.AuditLogs.Add(audit);
            await _context.SaveChangesAsync();

            return true;
        }
    }
}
