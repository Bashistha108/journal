import { fetchClient } from './httpClient';
import type { RowSaveRequest, RowDetail } from '../types/row';

export const rowApi = {
    createRow: (tableId: number, data: RowSaveRequest) => 
        fetchClient<RowDetail>(`/api/v1/tables/${tableId}/rows`, {
            method: 'POST',
            body: JSON.stringify(data)
        }),
        
    getRow: (tableId: number, rowId: number) =>
        fetchClient<RowDetail>(`/api/v1/tables/${tableId}/rows/${rowId}`),
        
    updateRow: (tableId: number, rowId: number, data: RowSaveRequest) =>
        fetchClient<RowDetail>(`/api/v1/tables/${tableId}/rows/${rowId}`, {
            method: 'PATCH',
            body: JSON.stringify(data)
        }),
        
    deleteRow: (tableId: number, rowId: number) =>
        fetchClient<void>(`/api/v1/tables/${tableId}/rows/${rowId}`, {
            method: 'DELETE'
        })
};
