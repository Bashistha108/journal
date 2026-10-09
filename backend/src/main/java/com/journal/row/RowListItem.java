package com.journal.row;

import java.util.Map;

public class RowListItem {
    private Long id;
    private Long version;
    private Map<Long, Object> values;
    private Map<Long, Integer> attachmentCounts;

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

    public Map<Long, Integer> getAttachmentCounts() {
        return attachmentCounts;
    }

    public void setAttachmentCounts(Map<Long, Integer> attachmentCounts) {
        this.attachmentCounts = attachmentCounts;
    }
}
