using Microsoft.AspNetCore.Mvc;
using EOC.Finance.API.Services;
using System.Threading.Tasks;

namespace EOC.Finance.API.Controllers
{
    [ApiController]
    [Route("api/[controller]")]
    public class BackupController : ControllerBase
    {
        private readonly IBackupService _backupService;

        public BackupController(IBackupService backupService)
        {
            _backupService = backupService;
        }

        [HttpPost("create")]
        public async Task<IActionResult> TriggerManualBackup()
        {
            // Restricted to Super Admin in production
            var fileName = await _backupService.CreateBackupAsync();
            return Ok(new { message = "Backup created successfully", fileName });
        }

        [HttpGet("list")]
        public IActionResult ListBackups()
        {
            return Ok(_backupService.GetAvailableBackups());
        }

        [HttpPost("restore/{fileName}")]
        public async Task<IActionResult> Restore(string fileName)
        {
            var success = await _backupService.RestoreBackupAsync(fileName);
            if (success) return Ok(new { message = "System restored successfully to " + fileName });
            return BadRequest(new { message = "Restore failed" });
        }
    }
}
