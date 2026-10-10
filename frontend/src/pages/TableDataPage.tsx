import { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import { tableApi } from '../api/tables';
import type { TableDetail } from '../types/table';
import { useTableQuery } from '../hooks/useTableQuery';
import { DataTable } from '../components/rows/DataTable';
import { FilterBuilder } from '../components/filter/FilterBuilder';
import { PaginationControls } from '../components/filter/PaginationControls';
import RowFormDialog from '../components/rows/RowFormDialog';
import { rowApi } from '../api/rowApi';
import { Plus, Loader2, AlertCircle } from 'lucide-react';

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

    if (!table) return (
        <div className="flex h-full items-center justify-center text-neutral-400 bg-[#121212]">
            <Loader2 className="w-8 h-8 animate-spin" />
        </div>
    );

    return (
        <div className="min-h-full bg-[#121212] text-neutral-200">
            <div className="p-8">
                <div className="flex justify-between items-start mb-8">
                    <div className="flex flex-col">
                        <h1 className="text-[22px] font-bold text-white tracking-tight">{table.displayName}</h1>
                        <p className="text-[13px] text-neutral-400 mt-1">View and manage table records</p>
                    </div>
                    <div className="flex items-center gap-3">
                        <button
                            onClick={() => setIsCreateOpen(true)}
                            className="btn-primary flex items-center gap-2"
                        >
                            <Plus className="w-4 h-4" />
                            Add Row
                        </button>
                    </div>
                </div>

                <div className="bg-[#1c1c1c] rounded-xl border border-[#2e2e2e] flex flex-col min-h-[calc(100vh-10rem)]">
                    <FilterBuilder
                        columns={table.columns}
                        filters={query.filters || []}
                        onChange={setFilters}
                    />

                    {error && (
                        <div className="px-6 py-4 bg-red-950/50 border-y border-red-900/50 flex items-center gap-3 text-red-200 text-sm">
                            <AlertCircle className="w-4 h-4" />
                            <span>{error}</span>
                        </div>
                    )}

                    <div className="relative flex-1 flex flex-col min-h-0">
                        {loading && (
                            <div className="absolute inset-0 bg-[#1c1c1c]/50 backdrop-blur-[2px] flex items-center justify-center z-10">
                                <Loader2 className="animate-spin h-6 w-6 text-yellow-400" />
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

                    <div className="border-t border-[#2e2e2e] bg-[#1c1c1c] rounded-b-xl mt-auto">
                        <PaginationControls
                            response={data}
                            onPageChange={setPage}
                        />
                    </div>
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
