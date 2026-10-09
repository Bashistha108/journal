import { fetchClient } from './httpClient';
import type { TableDetail, CreateTableRequest, CreateColumnRequest, ColumnDetail, SelectOption } from '../types/table';

export type { TableDetail, CreateTableRequest, CreateColumnRequest, ColumnDetail, SelectOption };

export const tableApi = {
    listTables: () => fetchClient<TableDetail[]>('/tables', { method: 'GET' }),
    createTable: (data: CreateTableRequest) => fetchClient<TableDetail>('/tables', { method: 'POST', body: JSON.stringify(data) }),
    getTable: (id: number) => fetchClient<TableDetail>(`/tables/${id}`, { method: 'GET' }),
    addColumn: (tableId: number, data: CreateColumnRequest) => fetchClient<ColumnDetail>(`/tables/${tableId}/columns`, { method: 'POST', body: JSON.stringify(data) }),
    addSelectOptions: (tableId: number, columnId: number, values: string[]) => fetchClient<void>(`/tables/${tableId}/columns/${columnId}/select-options`, { method: 'POST', body: JSON.stringify({ values }) }),
    deleteSelectOption: (tableId: number, columnId: number, optionId: number) => fetchClient<void>(`/tables/${tableId}/columns/${columnId}/select-options/${optionId}`, { method: 'DELETE' })
};
