package com.journal.attachment;

import com.journal.row.RowService;
import com.journal.metadata.TableRegistry;
import com.journal.metadata.TableRegistryRepository;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/tables/{tableId}/rows/{rowId}/attachments/{attachmentId}/content")
public class AttachmentContentController {

    private final AttachmentRepository attachmentRepo;
    private final RowService rowService;
    private final TableRegistryRepository tableRepo;

    public AttachmentContentController(AttachmentRepository attachmentRepo, RowService rowService, TableRegistryRepository tableRepo) {
        this.attachmentRepo = attachmentRepo;
        this.rowService = rowService;
        this.tableRepo = tableRepo;
    }

    @GetMapping
    public ResponseEntity<byte[]> getContent(
            @PathVariable Long tableId,
            @PathVariable Long rowId,
            @PathVariable Long attachmentId) {
        
        // Verify row belongs to table
        if (!rowService.exists(tableId, rowId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Row not found");
        }
        
        TableRegistry tr = tableRepo.findById(tableId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Table not found"));

        AttachmentMetadata metadata = attachmentRepo.findById(tr.physicalName(), rowId, attachmentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Attachment not found"));

        byte[] content = attachmentRepo.getContent(tr.physicalName(), rowId, attachmentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Content not found"));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(metadata.getContentType()));
        headers.setContentDispositionFormData("inline", metadata.getFilename());

        return new ResponseEntity<>(content, headers, HttpStatus.OK);
    }
}
