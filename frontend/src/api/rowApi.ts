import { fetchClient } from './httpClient';
import type { RowSaveRequest, RowDetail, RowListItem } from '../types/row';
import type { QueryRequest, PageResponse } from '../types/query';

import type { AttachmentUpload } from '../types/attachment';

export const rowApi = {
    createRow: (tableId: number, data: RowSaveRequest, files?: AttachmentUpload[]) => {
        if (files && files.length > 0) {
            const formData = new FormData();
            formData.append('row', new Blob([JSON.stringify(data)], { type: 'application/json' }));
            files.forEach(f => formData.append(f.clientRef, f.file));
            return fetchClient<RowDetail>(`/api/v1/tables/${tableId}/rows`, { method: 'POST', body: formData });
        }
        return fetchClient<RowDetail>(`/api/v1/tables/${tableId}/rows`, {
            method: 'POST',
            body: JSON.stringify(data)
        });
    },
        
    getRow: (tableId: number, rowId: number) =>
        fetchClient<RowDetail>(`/api/v1/tables/${tableId}/rows/${rowId}`),
        
    updateRow: (tableId: number, rowId: number, data: RowSaveRequest, files?: AttachmentUpload[]) => {
        if (files && files.length > 0) {
            const formData = new FormData();
            formData.append('row', new Blob([JSON.stringify(data)], { type: 'application/json' }));
            files.forEach(f => formData.append(f.clientRef, f.file));
            return fetchClient<RowDetail>(`/api/v1/tables/${tableId}/rows/${rowId}`, { method: 'PATCH', body: formData });
        }
        return fetchClient<RowDetail>(`/api/v1/tables/${tableId}/rows/${rowId}`, {
            method: 'PATCH',
            body: JSON.stringify(data)
        });
    },
        
    deleteRow: (tableId: number, rowId: number) =>
        fetchClient<void>(`/api/v1/tables/${tableId}/rows/${rowId}`, {
            method: 'DELETE'
        }),

    queryRows: (tableId: number, query: QueryRequest) =>
        fetchClient<PageResponse<RowListItem>>(`/api/v1/tables/${tableId}/rows/query`, {
            method: 'POST',
            body: JSON.stringify(query)
        })
};
