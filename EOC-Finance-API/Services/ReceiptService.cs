using EOC.Finance.API.Models;
using EOC.Finance.API.DTOs;
using System.Text;

namespace EOC.Finance.API.Services
{
    public interface IReceiptService
    {
        string GenerateHtmlReceipt(PaymentReceiptDto receipt);
        byte[] GeneratePdfReceipt(PaymentReceiptDto receipt); // Placeholder for PDF binary
    }

    public class ReceiptService : IReceiptService
    {
        public string GenerateHtmlReceipt(PaymentReceiptDto receipt)
        {
            var sb = new StringBuilder();
            sb.Append("<html><head><style>");
            sb.Append("body { font-family: 'Arial', sans-serif; padding: 50px; color: #0F172A; }");
            sb.Append(".receipt-box { border: 2px solid #D4AF37; padding: 30px; border-radius: 10px; max-width: 600px; margin: auto; }");
            sb.Append(".header { text-align: center; border-bottom: 2px solid #eee; padding-bottom: 20px; }");
            sb.Append(".amount { font-size: 32px; font-weight: bold; text-align: center; margin: 20px 0; color: #0F172A; }");
            sb.Append(".details { margin-top: 20px; }");
            sb.Append(".footer { margin-top: 30px; font-size: 12px; color: gray; text-align: center; }");
            sb.Append("</style></head><body>");

            sb.Append("<div class='receipt-box'>");
            sb.Append("<div class='header'>");
            sb.Append($"<h1>{receipt.ChurchName}</h1>");
            sb.Append("<p>Official Ecclesiastical Receipt</p>");
            sb.Append("</div>");

            sb.Append($"<div class='amount'>{receipt.Amount:N2} ETB</div>");

            sb.Append("<div class='details'>");
            sb.Append($"<p><strong>Receipt No:</strong> {receipt.ReceiptNumber}</p>");
            sb.Append($"<p><strong>Date:</strong> {receipt.PaymentDate:f}</p>");
            sb.Append($"<p><strong>Payment Type:</strong> {receipt.PaymentType}</p>");
            sb.Append($"<p><strong>Method:</strong> {receipt.PaymentMethod}</p>");
            if (!string.IsNullOrEmpty(receipt.MemberName))
                sb.Append($"<p><strong>Member:</strong> {receipt.MemberName}</p>");
            sb.Append("</div>");

            sb.Append("<div class='footer'>");
            sb.Append($"<p>Verification Code: {receipt.PaymentId}</p>");
            sb.Append($"<p>Scan the QR code on the mobile app to verify authenticity.</p>");
            sb.Append("</div>");
            sb.Append("</div></body></html>");

            return sb.ToString();
        }

        public byte[] GeneratePdfReceipt(PaymentReceiptDto receipt)
        {
            // In production, this uses an HTML-to-PDF library (e.g., PuppeteerSharp or WkHtmlToPdf)
            // For now, we return the byte representation of the HTML.
            return Encoding.UTF8.GetBytes(GenerateHtmlReceipt(receipt));
        }
    }
}
