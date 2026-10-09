export const attachmentApi = {
    buildAttachmentUrl: (tableId: number, rowId: number, attachmentId: number) => {
        return `/api/v1/tables/${tableId}/rows/${rowId}/attachments/${attachmentId}/content`;
    }
};
