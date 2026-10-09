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
