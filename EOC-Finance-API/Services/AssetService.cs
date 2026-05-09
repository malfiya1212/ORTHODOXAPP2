using EOC.Finance.API.Data;
using EOC.Finance.API.Models;
using Microsoft.EntityFrameworkCore;
using System;
using System.Linq;
using System.Threading.Tasks;
using System.Collections.Generic;

namespace EOC.Finance.API.Services
{
    public interface IAssetService
    {
        Task<ChurchAsset> RegisterAssetAsync(ChurchAsset asset);
        Task<List<ChurchAsset>> GetChurchAssetsAsync(long churchId);
        Task<decimal> CalculateTotalAssetValueAsync(long churchId);
        Task ApplyDepreciationAsync(long churchId); // Advanced feature
    }

    public class AssetService : IAssetService
    {
        private readonly AppDbContext _context;

        public AssetService(AppDbContext context)
        {
            _context = context;
        }

        public async Task<ChurchAsset> RegisterAssetAsync(ChurchAsset asset)
        {
            _context.ChurchAssets.Add(asset);
            await _context.SaveChangesAsync();
            return asset;
        }

        public async Task<List<ChurchAsset>> GetChurchAssetsAsync(long churchId)
        {
            return await _context.ChurchAssets
                .Where(a => a.ChurchId == churchId && a.Status == "ACTIVE")
                .ToListAsync();
        }

        public async Task<decimal> CalculateTotalAssetValueAsync(long churchId)
        {
            return await _context.ChurchAssets
                .Where(a => a.ChurchId == churchId && a.Status == "ACTIVE")
                .SumAsync(a => a.CurrentEstimatedValue);
        }

        public async Task ApplyDepreciationAsync(long churchId)
        {
            var assets = await _context.ChurchAssets
                .Where(a => a.ChurchId == churchId && a.Status == "ACTIVE")
                .ToListAsync();

            foreach (var asset in assets)
            {
                // Standard linear depreciation simulation (5% per year)
                // For Vehicles and Equipment, not Land.
                if (asset.AssetType == "Vehicle" || asset.AssetType == "Equipment")
                {
                    decimal depreciationAmount = asset.PurchaseValue * 0.05m;
                    asset.CurrentEstimatedValue -= depreciationAmount;

                    if (asset.CurrentEstimatedValue < 0) asset.CurrentEstimatedValue = 0;
                }
            }

            await _context.SaveChangesAsync();
        }
    }
}
