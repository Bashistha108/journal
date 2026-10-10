import { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import { tableApi } from '../api/tables';
import { rowApi } from '../api/rowApi';
import type { TableDetail } from '../types/table';
import type { RowDetail } from '../types/row';
import BackNavigation from '../components/common/BackNavigation';
import { RowDetailsView } from '../components/rows/RowDetailsView';
import RowFormDialog from '../components/rows/RowFormDialog';
import { Edit2 } from 'lucide-react';
import type { AttachmentUpload } from '../types/attachment';

export default function RowDetailsPage() {
    const { tableId, rowId } = useParams<{ tableId: string, rowId: string }>();
    const tId = Number(tableId);
    const rId = Number(rowId);

    const [table, setTable] = useState<TableDetail | null>(null);
    const [row, setRow] = useState<RowDetail | null>(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);
    const [isEditModalOpen, setIsEditModalOpen] = useState(false);

    const loadData = () => {
        if (!tId || !rId) return;

        let mounted = true;
        setLoading(true);

        Promise.all([
            tableApi.getTable(tId),
            rowApi.getRow(tId, rId)
        ]).then(([tableData, rowData]) => {
            if (mounted) {
                setTable(tableData);
                setRow(rowData);
                setError(null);
            }
        }).catch(err => {
            if (mounted) {
                console.error('Failed to load row details:', err);
                setError(err.message || 'Failed to load details');
            }
        }).finally(() => {
            if (mounted) {
                setLoading(false);
            }
        });

        return () => { mounted = false; };
    };

    useEffect(() => {
        const cleanup = loadData();
        return cleanup;
    }, [tId, rId]);

    const handleEditSubmit = async (values: Record<number, any>, expectedVersion?: number, newAttachments?: AttachmentUpload[], removeAttachmentIds?: number[]) => {
        if (!table || !row) return;
        
        await rowApi.updateRow(table.id, row.id, {
            values,
            expectedVersion,
            newAttachments: newAttachments?.map(a => ({ clientRef: a.clientRef, columnId: a.columnId })),
            removeAttachmentIds
        }, newAttachments);
        
        // Reload data after successful edit
        loadData();
    };

    if (loading) {
        return <div className="p-8 text-slate-300">Loading details...</div>;
    }

    if (error || !table || !row) {
        return (
            <div className="p-8 min-h-screen bg-[#0a0a0a] text-neutral-200">
                <BackNavigation to={`/tables/${tId}`} label="Back to Data Grid" />
                <div className="bg-red-900/50 text-red-200 border border-red-800/50 p-4 rounded-md mt-4">
                    {error || 'Row not found'}
                </div>
            </div>
        );
    }

    return (
        <div className="min-h-screen bg-[#121212] text-neutral-200 p-8 pt-12">
            <div className="max-w-7xl mx-auto">
                <BackNavigation to={`/tables/${table.id}`} label={`Back to ${table.displayName}`} />
                <div className="mb-8 mt-6 flex justify-between items-center">
                    <div className="flex items-center gap-3">
                        <h1 className="text-3xl font-bold tracking-tight text-white">
                            {table.displayName} <span className="text-neutral-500 font-light">/</span> <span className="text-neutral-400 font-medium">Row {row.id}</span>
                        </h1>
                        <span className="text-xs font-medium text-neutral-400 bg-neutral-800/50 border border-neutral-700/50 px-2.5 py-1 rounded-full mt-1">
                            Version {row.version}
                        </span>
                    </div>
                    <button 
                        onClick={() => setIsEditModalOpen(true)}
                        className="btn-secondary flex items-center gap-2"
                    >
                        <Edit2 className="w-4 h-4" />
                        Edit Record
                    </button>
                </div>

                <RowDetailsView table={table} row={row} />
                
                <RowFormDialog
                    tableId={table.id}
                    columns={table.columns}
                    initialData={row}
                    isOpen={isEditModalOpen}
                    onClose={() => setIsEditModalOpen(false)}
                    onSubmit={handleEditSubmit}
                />
            </div>
        </div>
    );
}
