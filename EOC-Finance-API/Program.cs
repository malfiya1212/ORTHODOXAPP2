using Microsoft.EntityFrameworkCore;
using Microsoft.AspNetCore.Authentication.JwtBearer;
using Microsoft.IdentityModel.Tokens;
using System.Text;
using EOC.Finance.API.Data;
using EOC.Finance.API.Services;
using EOC.Finance.API.Helpers;
using EOC.Finance.API.Hubs;
using EOC.Finance.API.Middleware;
using EOC.Finance.API.Workers;

var builder = WebApplication.CreateBuilder(args);

// Add services to the container.
builder.Services.AddControllers();
builder.Services.AddEndpointsApiExplorer();
builder.Services.AddSwaggerGen();

// Configure DB Connection
builder.Services.AddDbContext<AppDbContext>(options =>
    options.UseSqlServer(builder.Configuration.GetConnectionString("DefaultConnection")));

// Configure AutoMapper
builder.Services.AddAutoMapper(typeof(MappingProfile));

// Register all 17 Ecclesiastical Services
builder.Services.AddScoped<IAuthService, AuthService>();
builder.Services.AddScoped<IPaymentService, PaymentService>();
builder.Services.AddScoped<IAdvancedAnalyticsService, AdvancedAnalyticsService>();
builder.Services.AddScoped<IAdvancedSecurityService, AdvancedSecurityService>();
builder.Services.AddScoped<IApprovalWorkflowService, ApprovalWorkflowService>();
builder.Services.AddScoped<IAssetService, AssetService>();
builder.Services.AddScoped<IBackupService, BackupService>();
builder.Services.AddScoped<IBudgetService, BudgetService>();
builder.Services.AddScoped<ICurrencyService, CurrencyService>();
builder.Services.AddScoped<IDocumentService, DocumentService>();
builder.Services.AddScoped<IDonorService, DonorService>();
builder.Services.AddScoped<ILegalComplianceService, LegalComplianceService>();
builder.Services.AddScoped<INotificationService, NotificationService>();
builder.Services.AddScoped<IReceiptService, ReceiptService>();
builder.Services.AddScoped<IReportService, ReportService>();
builder.Services.AddScoped<ISmartAnalyticsService, SmartAnalyticsService>();
builder.Services.AddScoped<ISyncConflictService, SyncConflictService>();

// Register Live SignalR Hubs
builder.Services.AddSignalR();

// Register Background Backup Workers
builder.Services.AddHostedService<DailyBackupWorker>();

// Configure JWT Authentication
var jwtKey = builder.Configuration["Jwt:Key"] ?? "SUPER_SECRET_KEY_MIN_32_CHARS_LONG";
var key = Encoding.ASCII.GetBytes(jwtKey);
builder.Services.AddAuthentication(options =>
{
    options.DefaultAuthenticateScheme = JwtBearerDefaults.AuthenticationScheme;
    options.DefaultChallengeScheme = JwtBearerDefaults.AuthenticationScheme;
})
.AddJwtBearer(options =>
{
    options.TokenValidationParameters = new TokenValidationParameters
    {
        ValidateIssuerSigningKey = true,
        IssuerSigningKey = new SymmetricSecurityKey(key),
        ValidateIssuer = true,
        ValidateAudience = true,
        ValidIssuer = builder.Configuration["Jwt:Issuer"],
        ValidAudience = builder.Configuration["Jwt:Audience"],
        ClockSkew = TimeSpan.Zero
    };
});

var app = builder.Build();

// Configure the HTTP request pipeline.
if (app.Environment.IsDevelopment())
{
    app.UseSwagger();
    app.UseSwaggerUI();
}

app.UseHttpsRedirection();

// Custom database-backed Logging Middleware
app.UseMiddleware<LoggingMiddleware>();

app.UseAuthentication();
app.UseAuthorization();

// Route Controllers and SignalR Hubs
app.MapControllers();
app.MapHub<LiveDashboardHub>("/liveDashboardHub");

app.Run();
