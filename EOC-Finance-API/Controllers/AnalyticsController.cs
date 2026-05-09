using Microsoft.AspNetCore.Mvc;
using EOC.Finance.API.Services;
using System.Threading.Tasks;

namespace EOC.Finance.API.Controllers
{
    [ApiController]
    [Route("api/[controller]")]
    public class AnalyticsController : ControllerBase
    {
        private readonly IAdvancedAnalyticsService _analyticsService;

        public AnalyticsController(IAdvancedAnalyticsService analyticsService)
        {
            _analyticsService = analyticsService;
        }

        [HttpGet("national-report/{year}")]
        public async Task<IActionResult> GetNationalAnalytics(int year)
        {
            // Restricted to Super Admin / Synod in production
            var report = await _analyticsService.GetNationalFinancialReportAsync(year);
            return Ok(report);
        }
    }
}
