package com.journal.row;

import com.journal.attachment.AttachmentMetadata;
import java.util.List;
import java.util.Map;

public class RowDetail {
    private Long id;
    private Long version;
    private Map<Long, Object> values;
    private List<AttachmentMetadata> attachments;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public Map<Long, Object> getValues() {
        return values;
    }

    public void setValues(Map<Long, Object> values) {
        this.values = values;
    }

    public List<AttachmentMetadata> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<AttachmentMetadata> attachments) {
        this.attachments = attachments;
    }
}
