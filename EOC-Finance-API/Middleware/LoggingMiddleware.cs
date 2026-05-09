using Microsoft.AspNetCore.Http;
using System.Diagnostics;
using System.Threading.Tasks;
using System;
using EOC.Finance.API.Data;
using EOC.Finance.API.Models;
using Microsoft.Extensions.DependencyInjection;

namespace EOC.Finance.API.Middleware
{
    public class LoggingMiddleware
    {
        private readonly RequestDelegate _next;

        public LoggingMiddleware(RequestDelegate next)
        {
            _next = next;
        }

        public async Task InvokeAsync(HttpContext context)
        {
            var sw = Stopwatch.StartNew();
            var timestamp = DateTime.UtcNow;

            try
            {
                await _next(context);
                sw.Stop();

                // Log every API request and its performance
                await LogToDatabase(context, "INFO", "API", sw.ElapsedMilliseconds);
            }
            catch (Exception ex)
            {
                sw.Stop();
                // Log critical system errors/crashes
                await LogToDatabase(context, "ERROR", "API_EXCEPTION", sw.ElapsedMilliseconds, ex);
                throw; // Rethrow to allow standard error handling
            }
        }

        private async Task LogToDatabase(HttpContext context, string level, string category, long elapsedMs, Exception? ex = null)
        {
            var dbContext = context.RequestServices.GetRequiredService<AppDbContext>();
            
            var log = new SystemLog
            {
                Timestamp = DateTime.UtcNow,
                Level = level,
                Category = category,
                RequestPath = context.Request.Path,
                IpAddress = context.Connection.RemoteIpAddress?.ToString(),
                ElapsedMilliseconds = elapsedMs,
                Message = ex == null ? $"Request processed in {elapsedMs}ms" : ex.Message,
                Exception = ex?.StackTrace
            };

            dbContext.SystemLogs.Add(log);
            await dbContext.SaveChangesAsync();
        }
    }
}
