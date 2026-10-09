import React from 'react';
import type { TableDetail, ColumnDetail } from '../../types/table';
import type { RowDetail } from '../../types/row';
import { ImageGallery } from '../attachments/ImageGallery';

interface Props {
    table: TableDetail;
    row: RowDetail;
}

const isValidUrl = (val: string) => {
    try {
        const url = new URL(val);
        return url.protocol === 'http:' || url.protocol === 'https:';
    } catch {
        return false;
    }
};

export const RowDetailsView: React.FC<Props> = ({ table, row }) => {
    // Group attachments by column
    const attachmentsByColumn = row.attachments?.reduce((acc, att) => {
        if (!acc[att.columnId]) acc[att.columnId] = [];
        acc[att.columnId].push(att);
        return acc;
    }, {} as Record<number, typeof row.attachments>) || {};

    const renderFieldValue = (col: ColumnDetail, val: any) => {
        if (val === null || val === undefined) {
            return <span className="text-slate-600">—</span>; // Em dash
        }

        if (col.dataType === 'IMAGE') {
            const atts = attachmentsByColumn[col.id] || [];
            if (atts.length === 0) return <span className="text-slate-600">—</span>;
            return <ImageGallery tableId={table.id} rowId={row.id} attachments={atts} />;
        }

        if (col.dataType === 'BOOLEAN') {
            return <span className="text-slate-300">{val ? 'Yes' : 'No'}</span>;
        }

        if (col.dataType === 'TEXT' && typeof val === 'string' && isValidUrl(val)) {
            return (
                <a 
                    href={val} 
                    target="_blank" 
                    rel="noopener noreferrer"
                    className="text-indigo-400 hover:text-indigo-300 hover:underline break-all"
                >
                    {val}
                </a>
            );
        }

        return <span className="text-slate-300 break-words">{String(val)}</span>;
    };

    return (
        <div className="bg-slate-900 border border-slate-700 rounded-lg overflow-hidden">
            <div className="p-6 border-b border-slate-700 bg-slate-800">
                <h2 className="text-xl font-semibold text-slate-200">
                    Row ID: {row.id}
                </h2>
                <div className="text-sm text-slate-400 mt-1">
                    Version: {row.version}
                </div>
            </div>
            <div className="p-6">
                <dl className="divide-y divide-slate-800">
                    {table.columns.map(col => (
                        <div key={col.id} className="py-4 grid grid-cols-1 md:grid-cols-3 gap-4">
                            <dt className="text-sm font-medium text-slate-400">
                                {col.displayName}
                            </dt>
                            <dd className="md:col-span-2 text-sm">
                                {renderFieldValue(col, row.values[col.id])}
                            </dd>
                        </div>
                    ))}
                </dl>
            </div>
        </div>
    );
};
