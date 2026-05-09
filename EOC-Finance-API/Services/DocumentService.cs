using EOC.Finance.API.Data;
using EOC.Finance.API.Models;
using Microsoft.EntityFrameworkCore;
using System;
using System.Linq;
using System.Threading.Tasks;
using System.Collections.Generic;

namespace EOC.Finance.API.Services
{
    public interface IDocumentService
    {
        Task<Document> ArchiveDocumentAsync(long churchId, long uploaderId, string fileName, string fileType, string blobUrl, string? referenceType = null, Guid? referenceId = null);
        Task<List<Document>> GetChurchArchivesAsync(long churchId);
    }

    public class DocumentService : IDocumentService
    {
        private readonly AppDbContext _context;

        public DocumentService(AppDbContext context)
        {
            _context = context;
        }

        public async Task<Document> ArchiveDocumentAsync(long churchId, long uploaderId, string fileName, string fileType, string blobUrl, string? referenceType = null, Guid? referenceId = null)
        {
            // Handle Version Control if a document with the same name/type exists for this church
            var existingDocs = await _context.Documents
                .Where(d => d.ChurchId == churchId && d.FileName == fileName && d.FileType == fileType)
                .OrderByDescending(d => d.Version)
                .FirstOrDefaultAsync();

            int nextVersion = existingDocs != null ? existingDocs.Version + 1 : 1;

            var document = new Document
            {
                Id = Guid.NewGuid(),
                ChurchId = churchId,
                UploadedBy = uploaderId,
                FileName = fileName,
                FileType = fileType,
                BlobStorageUrl = blobUrl,
                Version = nextVersion,
                ReferenceType = referenceType,
                ReferenceId = referenceId,
                UploadedAt = DateTime.UtcNow
            };

            _context.Documents.Add(document);
            await _context.SaveChangesAsync();

            return document;
        }

        public async Task<List<Document>> GetChurchArchivesAsync(long churchId)
        {
            return await _context.Documents
                .Where(d => d.ChurchId == churchId)
                .OrderByDescending(d => d.UploadedAt)
                .ToListAsync();
        }
    }
}
