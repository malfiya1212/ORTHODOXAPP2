using EOC.Finance.API.Data;
using EOC.Finance.API.Models;
using Microsoft.EntityFrameworkCore;
using System;
using System.Linq;
using System.Threading.Tasks;
using System.Collections.Generic;
using EOC.Finance.API.Constants;

namespace EOC.Finance.API.Services
{
    public interface INotificationService
    {
        Task CreateNotificationAsync(long userId, string title, string message, string type, long? churchId = null);
        Task<List<Notification>> GetUserUnreadNotificationsAsync(long userId);
        Task MarkAsReadAsync(Guid notificationId);
        Task CheckForLowFundsWarningAsync(long churchId); // Advanced proactive trigger
    }

    public class NotificationService : INotificationService
    {
        private readonly AppDbContext _context;

        public NotificationService(AppDbContext context)
        {
            _context = context;
        }

        public async Task CreateNotificationAsync(long userId, string title, string message, string type, long? churchId = null)
        {
            var notification = new Notification
            {
                UserId = userId,
                Title = title,
                Message = message,
                Type = type,
                ChurchId = churchId,
                SentViaInApp = true
            };

            _context.Notifications.Add(notification);
            await _context.SaveChangesAsync();

            // In a full production system, we would trigger an external Email/SMS service here.
        }

        public async Task<List<Notification>> GetUserUnreadNotificationsAsync(long userId)
        {
            return await _context.Notifications
                .Where(n => n.UserId == userId && !n.IsRead)
                .OrderByDescending(n => n.CreatedAt)
                .ToListAsync();
        }

        public async Task MarkAsReadAsync(Guid notificationId)
        {
            var notif = await _context.Notifications.FindAsync(notificationId);
            if (notif != null)
            {
                notif.IsRead = true;
                await _context.SaveChangesAsync();
            }
        }

        public async Task CheckForLowFundsWarningAsync(long churchId)
        {
            // Calculate current net balance
            var totalIncome = await _context.IncomeRecords
                .Where(i => i.ChurchId == churchId && i.Status == "APPROVED")
                .SumAsync(i => (decimal)i.Amount);

            var totalExpenses = await _context.Expenses
                .Where(e => e.ChurchId == churchId && e.Status == "FINAL_APPROVED")
                .SumAsync(e => (decimal)e.Amount);

            var netBalance = totalIncome - totalExpenses;

            // If balance drops below a threshold (e.g., 5000 ETB), alert the Church Admin
            if (netBalance < 5000)
            {
                var admin = await _context.Users
                    .FirstOrDefaultAsync(u => u.ChurchId == churchId && u.RoleId == 3); // 3 = CHURCH_ADMIN

                if (admin != null)
                {
                    await CreateNotificationAsync(
                        admin.Id,
                        LocalizationKeys.MSG_LOW_FUNDS, // Use Key instead of String
                        "5000", // Send the raw value as the message, UI will format it with the key
                        "LOW_FUNDS",
                        churchId
                    );
                }
            }
        }
    }
}
