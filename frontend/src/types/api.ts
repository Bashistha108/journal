export interface FieldError {
    field: string;
    code: string;
    message: string;
}

export interface ApiError {
    status: number;
    code: string;
    message: string;
    requestId: string;
    fieldErrors?: FieldError[];
}
