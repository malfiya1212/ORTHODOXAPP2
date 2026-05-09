using Microsoft.AspNetCore.SignalR;
using System.Threading.Tasks;

namespace EOC.Finance.API.Hubs
{
    public class LiveDashboardHub : Hub
    {
        // Clients connect to this Hub to receive live national financial updates.
        
        public async Task JoinDioceseGroup(string dioceseId)
        {
            // Allows Diocese Admins to subscribe only to live events within their specific region
            await Groups.AddToGroupAsync(Context.ConnectionId, $"Diocese_{dioceseId}");
        }

        public async Task JoinNationalGroup()
        {
            // Super Admins (Synod) subscribe to the national firehose of financial data
            await Groups.AddToGroupAsync(Context.ConnectionId, "National_Synod");
        }
    }

    public interface ILiveDashboardService
    {
        Task BroadcastLiveIncomeAsync(long dioceseId, string churchName, decimal amount);
        Task BroadcastLargeTransactionAlertAsync(string churchName, decimal amount, string category);
    }

    public class LiveDashboardService : ILiveDashboardService
    {
        private readonly IHubContext<LiveDashboardHub> _hubContext;

        public LiveDashboardService(IHubContext<LiveDashboardHub> hubContext)
        {
            _hubContext = hubContext;
        }

        public async Task BroadcastLiveIncomeAsync(long dioceseId, string churchName, decimal amount)
        {
            var message = $"{churchName} just recorded a tithe of {amount} ETB.";
            
            // Broadcast to the specific Diocese
            await _hubContext.Clients.Group($"Diocese_{dioceseId}").SendAsync("ReceiveLiveUpdate", message);
            
            // Broadcast to the Holy Synod global dashboard
            await _hubContext.Clients.Group("National_Synod").SendAsync("ReceiveLiveUpdate", message);
        }

        public async Task BroadcastLargeTransactionAlertAsync(string churchName, decimal amount, string category)
        {
            // If a transaction is massive (e.g., building fund withdrawal > 500,000 ETB), sound the alarm to the Synod
            if (amount >= 500000)
            {
                var alert = $"🚨 CRITICAL ALERT: Large {category} transaction of {amount} ETB recorded at {churchName}.";
                await _hubContext.Clients.Group("National_Synod").SendAsync("ReceiveHighValueAlert", alert);
            }
        }
    }
}
