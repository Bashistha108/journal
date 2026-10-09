package com.journal.attachment;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class AttachmentService {

    private final AttachmentRepository attachmentRepo;

    public AttachmentService(AttachmentRepository attachmentRepo) {
        this.attachmentRepo = attachmentRepo;
    }

    public List<AttachmentMetadata> processChangeSet(Long tableId, String physicalTableName, Long rowId, AttachmentChangeSet changeSet, List<AttachmentMetadata> existingAttachments) {
        if (changeSet == null) return existingAttachments;

        // 1. apply removals
        if (changeSet.removeAttachmentIds() != null && !changeSet.removeAttachmentIds().isEmpty()) {
            for (Long idToRemove : changeSet.removeAttachmentIds()) {
                boolean found = existingAttachments.stream().anyMatch(a -> a.getId().equals(idToRemove));
                if (!found) {
                    throw new IllegalArgumentException("Cannot remove attachment " + idToRemove + " as it does not belong to this row");
                }
            }
            attachmentRepo.deleteByRowIdAndIds(physicalTableName, rowId, changeSet.removeAttachmentIds());
        }

        // 2. count remaining
        int currentCount = existingAttachments.size();
        if (changeSet.removeAttachmentIds() != null) {
            currentCount -= changeSet.removeAttachmentIds().size();
        }

        // 3. validate additions
        int newCount = 0;
        if (changeSet.additionsByColumnId() != null) {
            for (Map.Entry<Long, List<MultipartFile>> entry : changeSet.additionsByColumnId().entrySet()) {
                newCount += entry.getValue().size();
            }
        }

        if (currentCount + newCount > 10) {
            throw new IllegalArgumentException("Cannot exceed 10 attachments per row");
        }

        // 4. insert new
        List<AttachmentMetadata> allMetadata = new ArrayList<>(existingAttachments);
        if (changeSet.removeAttachmentIds() != null) {
            allMetadata.removeIf(a -> changeSet.removeAttachmentIds().contains(a.getId()));
        }

        if (changeSet.additionsByColumnId() != null) {
            for (Map.Entry<Long, List<MultipartFile>> entry : changeSet.additionsByColumnId().entrySet()) {
                Long columnId = entry.getKey();
                for (MultipartFile file : entry.getValue()) {
                    try {
                        ImageValidator.ImageInfo info = ImageValidator.validate(file);
                        AttachmentMetadata m = attachmentRepo.insert(
                                physicalTableName, tableId, rowId, columnId, info.safeFilename(), info.contentType(),
                                file.getSize(), file.getBytes());
                        allMetadata.add(m);
                    } catch (IOException e) {
                        throw new RuntimeException("Failed to read image bytes", e);
                    }
                }
            }
        }
        
        return allMetadata;
    }

    public List<AttachmentMetadata> getAttachments(String physicalTableName, Long rowId) {
        return attachmentRepo.findByRowId(physicalTableName, rowId);
    }
}
