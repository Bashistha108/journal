export interface AttachmentMetadata {
    id: number;
    columnId: number;
    filename: string;
    contentType: string;
    sizeBytes: number;
    createdAt: string;
}

export interface AttachmentUpload {
    file: File;
    clientRef: string;
    columnId: number;
}
