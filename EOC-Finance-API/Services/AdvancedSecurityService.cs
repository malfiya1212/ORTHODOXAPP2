using EOC.Finance.API.Data;
using EOC.Finance.API.Models;
using Microsoft.EntityFrameworkCore;
using System;
using System.Linq;
using System.Threading.Tasks;

namespace EOC.Finance.API.Services
{
    public interface IAdvancedSecurityService
    {
        Task<bool> ValidateLoginAttemptAsync(string email, string ipAddress, string deviceName);
        Task RecordFailedLoginAsync(string email, string ipAddress);
        Task<bool> RequireTwoFactorAuthAsync(long userId);
    }

    public class AdvancedSecurityService : IAdvancedSecurityService
    {
        private readonly AppDbContext _context;
        private const int MAX_FAILED_ATTEMPTS = 5;

        public AdvancedSecurityService(AppDbContext context)
        {
            _context = context;
        }

        public async Task<bool> ValidateLoginAttemptAsync(string email, string ipAddress, string deviceName)
        {
            var user = await _context.Users.FirstOrDefaultAsync(u => u.Email == email);
            if (user == null) return false;

            // 1. Check for brute force (Login Attempt Limits)
            var recentFailures = await _context.AuditLogs
                .Where(a => a.Action == "FAILED_LOGIN" && a.Details.Contains(email) && a.Timestamp > DateTime.UtcNow.AddMinutes(-15))
                .CountAsync();

            if (recentFailures >= MAX_FAILED_ATTEMPTS)
            {
                // Lockout triggered
                var lockoutAudit = new AuditLog
                {
                    UserId = user.Id,
                    Action = "ACCOUNT_LOCKED",
                    Details = $"Account locked due to {MAX_FAILED_ATTEMPTS} failed attempts from IP {ipAddress}.",
                    Timestamp = DateTime.UtcNow
                };
                _context.AuditLogs.Add(lockoutAudit);
                await _context.SaveChangesAsync();
                return false; // Login blocked
            }

            // 2. IP Monitoring & Suspicious Activity
            // If the user logs in from a completely new IP that they've never used before, flag it.
            var knownIp = await _context.LoginHistory
                .AnyAsync(l => l.UserId == user.Id && l.IpAddress == ipAddress);

            if (!knownIp && await _context.LoginHistory.AnyAsync(l => l.UserId == user.Id))
            {
                // Unrecognized IP - flag as suspicious but allow login (would trigger 2FA in production)
                var suspiciousAudit = new AuditLog
                {
                    UserId = user.Id,
                    Action = "SUSPICIOUS_LOGIN",
                    Details = $"Login from unrecognized IP Address: {ipAddress}. Device: {deviceName}",
                    Timestamp = DateTime.UtcNow
                };
                _context.AuditLogs.Add(suspiciousAudit);
            }

            await _context.SaveChangesAsync();
            return true;
        }

        public async Task RecordFailedLoginAsync(string email, string ipAddress)
        {
            var audit = new AuditLog
            {
                Action = "FAILED_LOGIN",
                Details = $"Failed login attempt for {email} from IP {ipAddress}.",
                Timestamp = DateTime.UtcNow
            };
            _context.AuditLogs.Add(audit);
            await _context.SaveChangesAsync();
        }

        public async Task<bool> RequireTwoFactorAuthAsync(long userId)
        {
            // In a full implementation, this triggers an SMS/Email with a 6-digit code.
            // For the API skeleton, we return true to indicate the 2FA flow is active.
            var user = await _context.Users.FindAsync(userId);
            
            // Example: Enforce 2FA strictly for Synod Admins (Role 1) and Diocese Admins (Role 2)
            if (user != null && (user.RoleId == 1 || user.RoleId == 2))
            {
                return true; 
            }
            return false;
        }
    }
}
