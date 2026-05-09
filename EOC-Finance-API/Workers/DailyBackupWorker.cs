using Microsoft.Extensions.Hosting;
using System;
using System.Threading;
using System.Threading.Tasks;
using Microsoft.Extensions.DependencyInjection;
using EOC.Finance.API.Services;

namespace EOC.Finance.API.Workers
{
    public class DailyBackupWorker : BackgroundService
    {
        private readonly IServiceProvider _services;

        public DailyBackupWorker(IServiceProvider services)
        {
            _services = services;
        }

        protected override async Task ExecuteAsync(CancellationToken stoppingToken)
        {
            while (!stoppingToken.IsCancellationRequested)
            {
                // Calculate time until next midnight
                var now = DateTime.Now;
                var nextRunTime = now.Date.AddDays(1);
                var delay = nextRunTime - now;

                await Task.Delay(delay, stoppingToken);

                // Perform the daily backup
                using (var scope = _services.CreateScope())
                {
                    var backupService = scope.ServiceProvider.GetRequiredService<IBackupService>();
                    await backupService.CreateBackupAsync();
                }
            }
        }
    }
}
