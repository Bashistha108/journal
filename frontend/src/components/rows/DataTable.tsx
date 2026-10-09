import React from 'react';
import type { TableDetail } from '../../types/table';
import type { RowListItem } from '../../types/row';
import type { SortRequest } from '../../types/query';
import { SortableColumnHeader } from './SortableColumnHeader';
import RowActions from './RowActions';

interface Props {
    table: TableDetail;
    rows: RowListItem[];
    currentSort?: SortRequest;
    onSort: (columnId: number, direction: 'ASC' | 'DESC') => void;
    onRowUpdated: () => void;
}

export const DataTable: React.FC<Props> = ({ table, rows, currentSort, onSort, onRowUpdated }) => {
    return (
        <div className="overflow-x-auto border border-slate-700 rounded bg-slate-900">
            <table className="w-full text-sm">
                <thead>
                    <tr className="bg-slate-800">
                        <th className="p-3 text-left font-medium text-slate-300 border-b border-slate-700 w-16">ID</th>
                        {table.columns.map(col => (
                            <SortableColumnHeader 
                                key={col.id} 
                                column={col} 
                                currentSort={currentSort} 
                                onSort={onSort} 
                            />
                        ))}
                        <th className="p-3 text-right font-medium text-slate-300 border-b border-slate-700 w-24">Actions</th>
                    </tr>
                </thead>
                <tbody className="divide-y divide-slate-800">
                    {rows.map(row => (
                        <tr key={row.id} className="hover:bg-slate-800/50 transition-colors group">
                            <td className="p-3 text-slate-400 font-mono">{row.id}</td>
                            {table.columns.map(col => {
                                const val = row.values[col.id];
                                return (
                                    <td key={col.id} className="p-3 text-slate-300 whitespace-nowrap overflow-hidden text-ellipsis max-w-xs">
                                        {col.dataType === 'IMAGE' ? (
                                            <span className="text-slate-500 text-xs">
                                                {row.attachmentCounts?.[col.id] || 0} images
                                            </span>
                                        ) : col.dataType === 'BOOLEAN' ? (
                                            val ? 'Yes' : 'No'
                                        ) : (
                                            val?.toString() ?? <span className="text-slate-600">-</span>
                                        )}
                                    </td>
                                );
                            })}
                            <td className="p-3 text-right opacity-0 group-hover:opacity-100 transition-opacity">
                                <RowActions 
                                    tableId={table.id}
                                    row={row}
                                    columns={table.columns}
                                    onRowUpdated={onRowUpdated}
                                />
                            </td>
                        </tr>
                    ))}
                    {rows.length === 0 && (
                        <tr>
                            <td colSpan={table.columns.length + 2} className="p-8 text-center text-slate-500 italic">
                                No records found.
                            </td>
                        </tr>
                    )}
                </tbody>
            </table>
        </div>
    );
};
