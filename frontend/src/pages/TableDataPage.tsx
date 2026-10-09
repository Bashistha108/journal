import { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import { tableApi } from '../api/tables';
import type { TableDetail } from '../types/table';
import { useTableQuery } from '../hooks/useTableQuery';
import { DataTable } from '../components/rows/DataTable';
import { FilterBuilder } from '../components/filter/FilterBuilder';
import { PaginationControls } from '../components/filter/PaginationControls';
import RowFormDialog from '../components/rows/RowFormDialog';
import BackNavigation from '../components/common/BackNavigation';
import { routes } from '../app/navigation';
import { rowApi } from '../api/rowApi';

export default function TableDataPage() {
    const { tableId } = useParams<{ tableId: string }>();
    const id = Number(tableId);

    const [table, setTable] = useState<TableDetail | null>(null);
    const [isCreateOpen, setIsCreateOpen] = useState(false);

    const { query, data, loading, error, setPage, setSort, setFilters, refresh } = useTableQuery(id);

    useEffect(() => {
        if (!id) return;
        tableApi.getTable(id).then(setTable).catch(console.error);
    }, [id]);

    if (!table) return <div className="p-8 text-slate-300">Loading table...</div>;

    return (
        <div className="min-h-screen bg-slate-950 text-slate-200 p-8">
            <div className="max-w-7xl mx-auto space-y-6">
                <div className="flex justify-between items-center">
                    <div>
                        <BackNavigation to={routes.tables()} label="Back to Tables" />
                        <h1 className="text-3xl font-bold">{table.displayName}</h1>
                    </div>
                    <button
                        onClick={() => setIsCreateOpen(true)}
                        className="bg-indigo-600 hover:bg-indigo-500 px-4 py-2 rounded font-medium transition-colors"
                    >
                        + Add Record
                    </button>
                </div>

                <div className="bg-slate-900 border border-slate-700 rounded-lg overflow-hidden">
                    <FilterBuilder
                        columns={table.columns}
                        filters={query.filters || []}
                        onChange={setFilters}
                    />

                    {error && (
                        <div className="p-4 bg-red-900/50 text-red-200 border-b border-red-800">
                            {error}
                        </div>
                    )}

                    <div className="relative">
                        {loading && (
                            <div className="absolute inset-0 bg-slate-950/50 flex items-center justify-center z-10">
                                <span className="text-indigo-400">Loading...</span>
                            </div>
                        )}
                        <DataTable
                            table={table}
                            rows={data?.items || []}
                            currentSort={query.sort}
                            onSort={(columnId, direction) => setSort({ columnId, direction })}
                            onRowUpdated={refresh}
                        />
                    </div>

                    <PaginationControls
                        response={data}
                        onPageChange={setPage}
                    />
                </div>
            </div>

            {isCreateOpen && (
                <RowFormDialog
                    tableId={table.id}
                    isOpen={isCreateOpen}
                    columns={table.columns}
                    onSubmit={async (values, _ver, newAttachments, removeAttachmentIds) => {
                        await rowApi.createRow(table.id, { values, removeAttachmentIds }, newAttachments);
                        setIsCreateOpen(false);
                        refresh();
                    }}
                    onClose={() => setIsCreateOpen(false)}
                />
            )}
        </div>
    );
}
