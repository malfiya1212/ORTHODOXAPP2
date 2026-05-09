using EOC.Finance.API.Data;
using EOC.Finance.API.Models;
using Microsoft.EntityFrameworkCore;
using System;
using System.Linq;
using System.Threading.Tasks;
using System.Collections.Generic;

namespace EOC.Finance.API.Services
{
    public class BudgetComparisonDto
    {
        public string Category { get; set; } = string.Empty;
        public decimal BudgetedAmount { get; set; }
        public decimal ActualSpent { get; set; }
        public decimal RemainingBalance => BudgetedAmount - ActualSpent;
        public decimal UtilizationPercentage => BudgetedAmount > 0 ? (ActualSpent / BudgetedAmount) * 100 : 0;
        public bool IsOverBudget => ActualSpent > BudgetedAmount;
    }

    public interface IBudgetService
    {
        Task<Budget> SetBudgetAsync(Budget budget);
        Task<List<BudgetComparisonDto>> GetBudgetVsActualAsync(long churchId, DateTime startDate, DateTime endDate);
    }

    public class BudgetService : IBudgetService
    {
        private readonly AppDbContext _context;

        public BudgetService(AppDbContext context)
        {
            _context = context;
        }

        public async Task<Budget> SetBudgetAsync(Budget budget)
        {
            _context.Budgets.Add(budget);
            await _context.SaveChangesAsync();
            return budget;
        }

        public async Task<List<BudgetComparisonDto>> GetBudgetVsActualAsync(long churchId, DateTime startDate, DateTime endDate)
        {
            // 1. Get all budgets for the period
            var budgets = await _context.Budgets
                .Where(b => b.ChurchId == churchId && b.StartDate >= startDate && b.EndDate <= endDate)
                .ToListAsync();

            // 2. Get all actual expenses for the same period
            var expenses = await _context.Expenses
                .Where(e => e.ChurchId == churchId && e.Date >= startDate && e.Date <= endDate && e.Status == "FINAL_APPROVED")
                .ToListAsync();

            var comparison = new List<BudgetComparisonDto>();

            // 3. Aggregate Actuals vs Budgets per Category
            foreach (var budget in budgets)
            {
                var actualSpent = expenses
                    .Where(e => e.Category == budget.Category)
                    .Sum(e => (decimal)e.Amount);

                comparison.Add(new BudgetComparisonDto
                {
                    Category = budget.Category,
                    BudgetedAmount = budget.Amount,
                    ActualSpent = actualSpent
                });
            }

            return comparison;
        }
    }
}
