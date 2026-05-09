using EOC.Finance.API.Data;
using EOC.Finance.API.Models;
using Microsoft.EntityFrameworkCore;
using System;
using System.Linq;
using System.Threading.Tasks;
using System.Collections.Generic;

namespace EOC.Finance.API.Services
{
    public interface IDonorService
    {
        Task<Donor> RegisterOrUpdateDonorAsync(long churchId, string name, decimal donationAmount, bool isAnonymous = false, string? email = null);
        Task<List<Donor>> GetTopDonorsAsync(long churchId, int count = 10);
        Task<List<Donor>> GetAnonymousDonorsReportAsync(long churchId);
    }

    public class DonorService : IDonorService
    {
        private readonly AppDbContext _context;

        public DonorService(AppDbContext context)
        {
            _context = context;
        }

        public async Task<Donor> RegisterOrUpdateDonorAsync(long churchId, string name, decimal donationAmount, bool isAnonymous = false, string? email = null)
        {
            var donorName = isAnonymous ? "Anonymous" : name;

            // If not anonymous, try to find an existing donor by email or name within the church
            Donor? donor = null;
            if (!isAnonymous)
            {
                donor = await _context.Donors.FirstOrDefaultAsync(d => d.ChurchId == churchId && (d.Email == email || d.Name == name) && !d.IsAnonymous);
            }

            if (donor == null)
            {
                donor = new Donor
                {
                    Id = Guid.NewGuid(),
                    Name = donorName,
                    Email = isAnonymous ? null : email,
                    IsAnonymous = isAnonymous,
                    ChurchId = churchId,
                    TotalDonatedAmount = donationAmount,
                    FirstDonationDate = DateTime.UtcNow,
                    LastDonationDate = DateTime.UtcNow
                };
                _context.Donors.Add(donor);
            }
            else
            {
                donor.TotalDonatedAmount += donationAmount;
                donor.LastDonationDate = DateTime.UtcNow;
            }

            // Update Tier Logic
            if (donor.TotalDonatedAmount >= 100000) donor.DonorTier = "Platinum";
            else if (donor.TotalDonatedAmount >= 50000) donor.DonorTier = "Gold";
            else if (donor.TotalDonatedAmount >= 10000) donor.DonorTier = "Silver";
            else donor.DonorTier = "Standard";

            await _context.SaveChangesAsync();
            return donor;
        }

        public async Task<List<Donor>> GetTopDonorsAsync(long churchId, int count = 10)
        {
            return await _context.Donors
                .Where(d => d.ChurchId == churchId && !d.IsAnonymous)
                .OrderByDescending(d => d.TotalDonatedAmount)
                .Take(count)
                .ToListAsync();
        }

        public async Task<List<Donor>> GetAnonymousDonorsReportAsync(long churchId)
        {
            return await _context.Donors
                .Where(d => d.ChurchId == churchId && d.IsAnonymous)
                .OrderByDescending(d => d.LastDonationDate)
                .ToListAsync();
        }
    }
}
