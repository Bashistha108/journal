package com.journal.attachment;

import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.Map;

public record AttachmentChangeSet(
        List<Long> removeAttachmentIds,
        Map<Long, List<MultipartFile>> additionsByColumnId
) {}
