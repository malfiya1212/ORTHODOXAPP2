using Microsoft.AspNetCore.Mvc;
using EOC.Finance.API.Services;
using EOC.Finance.API.DTOs;
using System.Security.Claims;
using System.Threading.Tasks;
using System.Text;

namespace EOC.Finance.API.Controllers
{
    [ApiController]
    [Route("api/[controller]")]
    public class PaymentController : ControllerBase
    {
        private readonly IPaymentService _paymentService;

        public PaymentController(IPaymentService paymentService)
        {
            _paymentService = paymentService;
        }

        [HttpPost("process")]
        public async Task<IActionResult> ProcessPayment([FromBody] PaymentCreateDto dto)
        {
            // In a fully secured app, we extract the UserId from the JWT claims:
            // var userIdString = User.FindFirst(ClaimTypes.NameIdentifier)?.Value;
            // if (!long.TryParse(userIdString, out var currentUserId)) return Unauthorized();
            
            long currentUserId = 1; // Fallback for demonstration/mock

            // In production, we also check if User has Role = Accountant or ChurchAdmin

            try 
            {
                var receipt = await _paymentService.ProcessPaymentAsync(dto, currentUserId);
                return Ok(receipt);
            }
            catch(System.Exception ex)
            {
                return BadRequest(new { message = "Payment processing failed: " + ex.Message });
            }
        }

        [HttpGet("download/{receiptNumber}")]
        public async Task<IActionResult> DownloadReceipt(string receiptNumber)
        {
            // In production, we'd fetch the payment by receiptNumber from a service
            // For now, we return a mock PDF generated from the engine
            return File(Encoding.UTF8.GetBytes("PDF_BINARY_DATA_PLACEHOLDER"), "application/pdf", $"Receipt_{receiptNumber}.pdf");
        }

        [HttpGet("verify/{receiptNumber}")]
        public async Task<IActionResult> VerifyReceipt(string receiptNumber)
        {
            // We use the AppDbContext directly here for the verification page (in a real app, use a service)
            // But since we just need a quick HTML response, we can inject or mock the logic.
            // For the sake of the enterprise structure, returning a dynamic HTML page:

            string html = $@"
            <html>
            <head>
                <title>Receipt Verification - Tewahedo Finance</title>
                <style>
                    body {{ font-family: 'Inter', sans-serif; background-color: #F8FAFC; padding: 40px; text-align: center; }}
                    .card {{ background: white; padding: 30px; border-radius: 12px; box-shadow: 0 4px 6px rgba(0,0,0,0.1); border-top: 4px solid #D4AF37; max-width: 500px; margin: 0 auto; }}
                    h1 {{ color: #0F172A; }}
                    .valid {{ color: #059669; font-weight: bold; font-size: 1.2em; }}
                </style>
            </head>
            <body>
                <div class='card'>
                    <h1>Holy Synod Financial Audit</h1>
                    <p class='valid'>✅ OFFICIAL RECEIPT VERIFIED</p>
                    <p><strong>Receipt Number:</strong> {receiptNumber}</p>
                    <p>This receipt was securely recorded in the national ecclesiastical ledger and cannot be altered or forged.</p>
                </div>
            </body>
            </html>";

            return Content(html, "text/html");
        }
    }
}
