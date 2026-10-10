import React from 'react';
import type { TableDetail } from '../../types/table';
import type { RowListItem } from '../../types/row';
import type { SortRequest } from '../../types/query';
import { SortableColumnHeader } from './SortableColumnHeader';
import RowActions from './RowActions';
import { Image as ImageIcon } from 'lucide-react';

interface Props {
    table: TableDetail;
    rows: RowListItem[];
    currentSort?: SortRequest;
    onSort: (columnId: number, direction: 'ASC' | 'DESC') => void;
    onRowUpdated: () => void;
}

export const DataTable: React.FC<Props> = ({ table, rows, currentSort, onSort, onRowUpdated }) => {
    return (
        <div className="overflow-auto flex-1 min-h-0">
            <table className="w-full text-sm text-left">
                <thead className="sticky top-0 bg-[#1c1c1c] z-10">
                    <tr className="border-b border-[#2e2e2e]">
                        <th className="px-6 py-4 font-medium text-neutral-400 w-16">
                            ID
                        </th>
                        {table.columns.map(col => (
                            <SortableColumnHeader 
                                key={col.id} 
                                column={col} 
                                currentSort={currentSort} 
                                onSort={onSort} 
                            />
                        ))}
                        <th className="px-6 py-4 font-medium text-neutral-400 text-right w-24">Actions</th>
                    </tr>
                </thead>
                <tbody className="divide-y divide-[#2e2e2e]">
                    {rows.map(row => (
                        <tr key={row.id} className="hover:bg-[#2a2a2a] transition-colors group">
                            <td className="px-6 py-4 text-neutral-200">{row.id}</td>
                            {table.columns.map(col => {
                                const val = row.values[col.id];
                                return (
                                    <td key={col.id} className="px-6 py-4 text-neutral-200 whitespace-nowrap overflow-hidden text-ellipsis max-w-xs">
                                        {col.dataType === 'IMAGE' ? (
                                            (row.attachmentCounts?.[col.id] || 0) > 0 ? (
                                                <span className="text-yellow-500 text-[13px] font-medium flex items-center gap-1.5 border border-yellow-500/30 bg-yellow-500/10 px-2 py-1 rounded w-fit">
                                                    <ImageIcon className="w-3.5 h-3.5" />
                                                    [IMAGE]
                                                </span>
                                            ) : (
                                                <span className="text-neutral-600">-</span>
                                            )
                                        ) : col.dataType === 'BOOLEAN' ? (
                                            <span className={`text-xs font-bold px-2 py-1 rounded-md ${val ? 'bg-green-500/10 text-green-400' : 'bg-red-500/10 text-red-400'}`}>
                                                {val ? 'Yes' : 'No'}
                                            </span>
                                        ) : col.dataType === 'SELECT' ? (
                                            val ? <span className="text-[13px] font-medium">{val.toString()}</span> : <span className="text-neutral-600">-</span>
                                        ) : (
                                            val?.toString() ?? <span className="text-neutral-600">-</span>
                                        )}
                                    </td>
                                );
                            })}
                            <td className="px-6 py-4 text-right">
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
                            <td colSpan={table.columns.length + 2} className="px-6 py-16 text-center text-[#555] text-[13px]">
                                No records found.
                            </td>
                        </tr>
                    )}
                </tbody>
            </table>
        </div>
    );
};
