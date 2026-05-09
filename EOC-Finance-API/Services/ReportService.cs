using EOC.Finance.API.Data;
using EOC.Finance.API.Models;
using EOC.Finance.API.DTOs;
using Microsoft.EntityFrameworkCore;
using AutoMapper;

namespace EOC.Finance.API.Services
{
    public interface IReportService
    {
        Task<ChurchFinancialReportDto> GenerateChurchReportAsync(long churchId);
        Task<DioceseFinancialReportDto> GenerateDioceseReportAsync(long dioceseId);
        Task<NationalFinancialReportDto> GenerateNationalReportAsync();
    }

    public class ReportService : IReportService
    {
        private readonly AppDbContext _context;

        public ReportService(AppDbContext context)
        {
            _context = context;
        }

        public async Task<ChurchFinancialReportDto> GenerateChurchReportAsync(long churchId)
        {
            var church = await _context.Churches.FindAsync(churchId);
            if (church == null) throw new Exception("Church not found");

            var totalIncome = await _context.IncomeRecords
                .Where(i => i.ChurchId == churchId && i.Status == "APPROVED")
                .SumAsync(i => (decimal)i.Amount);

            var totalExpenses = await _context.Expenses
                .Where(e => e.ChurchId == churchId && e.Status == "APPROVED")
                .SumAsync(e => (decimal)e.Amount);

            return new ChurchFinancialReportDto
            {
                ChurchId = church.Id,
                ChurchName = church.Name,
                TotalIncome = totalIncome,
                TotalExpenses = totalExpenses,
                NetBalance = totalIncome - totalExpenses,
                GeneratedAt = DateTime.UtcNow
            };
        }

        public async Task<DioceseFinancialReportDto> GenerateDioceseReportAsync(long dioceseId)
        {
            var diocese = await _context.Dioceses.FindAsync(dioceseId);
            if (diocese == null) throw new Exception("Diocese not found");

            var churches = await _context.Churches.Where(c => c.DioceseId == dioceseId).ToListAsync();
            var churchReports = new List<ChurchFinancialReportDto>();
            
            decimal sumIncome = 0;
            decimal sumExpenses = 0;

            foreach(var church in churches)
            {
                var report = await GenerateChurchReportAsync(church.Id);
                churchReports.Add(report);
                sumIncome += report.TotalIncome;
                sumExpenses += report.TotalExpenses;
            }

            return new DioceseFinancialReportDto
            {
                DioceseId = diocese.Id,
                DioceseName = diocese.Name,
                SumTotalIncome = sumIncome,
                SumTotalExpenses = sumExpenses,
                TotalNetBalance = sumIncome - sumExpenses,
                ChurchComparisons = churchReports.OrderByDescending(r => r.NetBalance).ToList()
            };
        }

        public async Task<NationalFinancialReportDto> GenerateNationalReportAsync()
        {
            var dioceses = await _context.Dioceses.ToListAsync();
            var dioceseReports = new List<DioceseFinancialReportDto>();

            decimal nationalIncome = 0;
            decimal nationalExpenses = 0;

            foreach(var diocese in dioceses)
            {
                var report = await GenerateDioceseReportAsync(diocese.Id);
                dioceseReports.Add(report);
                nationalIncome += report.SumTotalIncome;
                nationalExpenses += report.SumTotalExpenses;
            }

            return new NationalFinancialReportDto
            {
                AllEthiopiaTotalIncome = nationalIncome,
                AllEthiopiaTotalExpenses = nationalExpenses,
                NationalReserve = nationalIncome - nationalExpenses,
                DioceseRankings = dioceseReports.OrderByDescending(r => r.TotalNetBalance).ToList()
            };
        }
    }
}
