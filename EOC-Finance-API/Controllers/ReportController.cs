using Microsoft.AspNetCore.Mvc;
using EOC.Finance.API.Services;
using EOC.Finance.API.DTOs;

namespace EOC.Finance.API.Controllers
{
    [ApiController]
    [Route("api/[controller]")]
    public class ReportController : ControllerBase
    {
        private readonly IReportService _reportService;

        public ReportController(IReportService reportService)
        {
            _reportService = reportService;
        }

        [HttpGet("church/{churchId}")]
        public async Task<IActionResult> GetByChurch(long churchId)
        {
            var reports = await _reportService.GetReportsByChurchAsync(churchId);
            return Ok(reports);
        }

        [HttpPost("generate")]
        public async Task<IActionResult> Generate([FromBody] GenerateReportRequest request)
        {
            // In production, get userId from JWT claims
            var report = await _reportService.GenerateReportAsync(request.ChurchId, request.Type, 1L);
            return Ok(report);
        }
    }

    public class GenerateReportRequest
    {
        public long ChurchId { get; set; }
        public string Type { get; set; } = "ANNUAL";
    }
}
