import { fetchClient } from './httpClient';

export interface SelectOption {
    id: number;
    value: string;
}

export interface ColumnDetail {
    id: number;
    displayName: string;
    dataType: string;
    selectOptions: SelectOption[];
}

export interface TableDetail {
    id: number;
    displayName: string;
    columns: ColumnDetail[];
}

export interface CreateColumnRequest {
    name: string;
    dataType: string;
    selectOptions?: string[];
}

export interface CreateTableRequest {
    name: string;
    columns: CreateColumnRequest[];
}

export const tableApi = {
    listTables: () => fetchClient<TableDetail[]>('/tables', { method: 'GET' }),
    createTable: (data: CreateTableRequest) => fetchClient<TableDetail>('/tables', { method: 'POST', body: JSON.stringify(data) }),
    getTable: (id: number) => fetchClient<TableDetail>(`/tables/${id}`, { method: 'GET' }),
    addColumn: (tableId: number, data: CreateColumnRequest) => fetchClient<ColumnDetail>(`/tables/${tableId}/columns`, { method: 'POST', body: JSON.stringify(data) }),
    addSelectOptions: (tableId: number, columnId: number, values: string[]) => fetchClient<void>(`/tables/${tableId}/columns/${columnId}/select-options`, { method: 'POST', body: JSON.stringify({ values }) }),
    deleteSelectOption: (tableId: number, columnId: number, optionId: number) => fetchClient<void>(`/tables/${tableId}/columns/${columnId}/select-options/${optionId}`, { method: 'DELETE' })
};
