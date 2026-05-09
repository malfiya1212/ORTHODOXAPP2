using Microsoft.EntityFrameworkCore;
using EOC.Finance.API.Models;

namespace EOC.Finance.API.Data
{
    public class AppDbContext : DbContext
    {
        public AppDbContext(DbContextOptions<AppDbContext> options) : base(options) { }

        public DbSet<User> Users { get; set; }
        public DbSet<Diocese> Dioceses { get; set; }
        public DbSet<Church> Churches { get; set; }
        public DbSet<Income> IncomeRecords { get; set; }
        public DbSet<Expense> Expenses { get; set; }
        public DbSet<AuditLog> AuditLogs { get; set; }
        public DbSet<Payment> Payments { get; set; }
        public DbSet<Budget> Budgets { get; set; }
        public DbSet<Document> Documents { get; set; }
        public DbSet<ChurchAsset> ChurchAssets { get; set; }
        public DbSet<Notification> Notifications { get; set; }
        public DbSet<Donor> Donors { get; set; }
        public DbSet<SystemLog> SystemLogs { get; set; }

        protected override void OnModelCreating(ModelBuilder modelBuilder)
        {
            base.OnModelCreating(modelBuilder);

            // Configure SQL Server specific constraints
            modelBuilder.Entity<User>()
                .HasIndex(u => u.Email)
                .IsUnique();

            // Set up hierarchy relationships
            modelBuilder.Entity<Church>()
                .HasOne<Diocese>()
                .WithMany()
                .HasForeignKey(c => c.DioceseId);
        }
    }
}
