import { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import { tableApi } from '../api/tables';
import { rowApi } from '../api/rowApi';
import type { TableDetail } from '../types/table';
import type { RowDetail } from '../types/row';
import BackNavigation from '../components/common/BackNavigation';
import { RowDetailsView } from '../components/rows/RowDetailsView';

export default function RowDetailsPage() {
    const { tableId, rowId } = useParams<{ tableId: string, rowId: string }>();
    const tId = Number(tableId);
    const rId = Number(rowId);

    const [table, setTable] = useState<TableDetail | null>(null);
    const [row, setRow] = useState<RowDetail | null>(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
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
    }, [tId, rId]);

    if (loading) {
        return <div className="p-8 text-slate-300">Loading details...</div>;
    }

    if (error || !table || !row) {
        return (
            <div className="p-8 min-h-screen bg-slate-950 text-slate-200">
                <BackNavigation to={`/tables/${tId}`} label="Back to Data Grid" />
                <div className="bg-red-900/50 text-red-200 border border-red-800 p-4 rounded mt-4">
                    {error || 'Row not found'}
                </div>
            </div>
        );
    }

    return (
        <div className="min-h-screen bg-slate-950 text-slate-200 p-8">
            <div className="max-w-4xl mx-auto">
                <BackNavigation to={`/tables/${table.id}`} label={`Back to ${table.displayName}`} />
                <div className="mb-6">
                    <h1 className="text-3xl font-bold">{table.displayName} - Row {row.id}</h1>
                </div>

                <RowDetailsView table={table} row={row} />
            </div>
        </div>
    );
}
