export type FilterOperator = 'CONTAINS' | 'EQUALS' | 'IS_EMPTY' | 'IS_NOT_EMPTY' | 'GREATER_THAN' | 'LESS_THAN' | 'BETWEEN' | 'ON' | 'BEFORE' | 'AFTER' | 'IS_TRUE' | 'IS_FALSE' | 'MATCHES_ANY' | 'HAS_ATTACHMENTS' | 'HAS_NO_ATTACHMENTS';

export interface FilterRequest {
    columnId: number;
    operator: FilterOperator;
    value?: any;
    values?: any[];
    from?: any;
    to?: any;
}

export interface SortRequest {
    columnId: number;
    direction: 'ASC' | 'DESC';
}

export interface QueryRequest {
    page: number;
    sort?: SortRequest;
    filters?: FilterRequest[];
}

export interface PageResponse<T> {
    page: number;
    pageSize: number;
    totalItems: number;
    totalPages: number;
    items: T[];
}
