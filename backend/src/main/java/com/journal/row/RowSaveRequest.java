package com.journal.row;

import java.util.List;
import java.util.Map;

public class RowSaveRequest {
    private Map<Long, Object> values;
    private List<NewAttachmentDto> newAttachments;
    private Long expectedVersion;
    private List<Long> removeAttachmentIds;

    public Map<Long, Object> getValues() {
        return values;
    }

    public void setValues(Map<Long, Object> values) {
        this.values = values;
    }

    public List<NewAttachmentDto> getNewAttachments() {
        return newAttachments;
    }

    public void setNewAttachments(List<NewAttachmentDto> newAttachments) {
        this.newAttachments = newAttachments;
    }

    public Long getExpectedVersion() {
        return expectedVersion;
    }

    public void setExpectedVersion(Long expectedVersion) {
        this.expectedVersion = expectedVersion;
    }

    public List<Long> getRemoveAttachmentIds() {
        return removeAttachmentIds;
    }

    public void setRemoveAttachmentIds(List<Long> removeAttachmentIds) {
        this.removeAttachmentIds = removeAttachmentIds;
    }

    public static class NewAttachmentDto {
        private String clientRef;
        private Long columnId;

        public String getClientRef() {
            return clientRef;
        }

        public void setClientRef(String clientRef) {
            this.clientRef = clientRef;
        }

        public Long getColumnId() {
            return columnId;
        }

        public void setColumnId(Long columnId) {
            this.columnId = columnId;
        }
    }
}
