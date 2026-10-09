import type { AttachmentMetadata } from './attachment';

export interface RowSaveRequest {
    values: Record<number, any>;
    newAttachments?: { clientRef: string; columnId: number }[];
    expectedVersion?: number;
    removeAttachmentIds?: number[];
}

export interface RowDetail {
    id: number;
    version: number;
    values: Record<number, any>;
    attachments: AttachmentMetadata[];
}

export interface RowListItem {
    id: number;
    version: number;
    values: Record<number, any>;
    attachmentCounts: Record<number, number>;
}
