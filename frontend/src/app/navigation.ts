export const routes = {
    tables: () => '/tables',
    tableData: (tableId: number) => `/tables/${tableId}`,
    rowDetails: (tableId: number, rowId: number) => `/tables/${tableId}/rows/${rowId}`,
};
