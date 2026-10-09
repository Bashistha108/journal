import React from 'react';
import type { ColumnDetail } from '../../types/table';
import type { SortRequest } from '../../types/query';

interface Props {
    column: ColumnDetail;
    currentSort?: SortRequest;
    onSort: (columnId: number, direction: 'ASC' | 'DESC') => void;
}

export const SortableColumnHeader: React.FC<Props> = ({ column, currentSort, onSort }) => {
    const isSorted = currentSort?.columnId === column.id;
    const isAsc = isSorted && currentSort?.direction === 'ASC';

    const handleClick = () => {
        onSort(column.id, isAsc ? 'DESC' : 'ASC');
    };

    return (
        <th 
            className="p-3 text-left font-medium text-slate-300 border-b border-slate-700 cursor-pointer hover:bg-slate-800 transition-colors"
            onClick={handleClick}
        >
            <div className="flex items-center gap-1">
                {column.displayName}
                <span className="text-slate-500 w-4">
                    {isSorted ? (isAsc ? '↑' : '↓') : '↕'}
                </span>
            </div>
        </th>
    );
};
