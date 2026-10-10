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
            className="px-6 py-4 text-left font-medium text-neutral-400 cursor-pointer hover:text-white transition-colors group"
            onClick={handleClick}
        >
            <div className="flex items-center gap-1.5">
                <span>{column.displayName}</span>
                <span className="text-neutral-600 group-hover:text-neutral-400 transition-colors w-4 flex items-center justify-center">
                    {isSorted ? (isAsc ? '↑' : '↓') : ''}
                </span>
            </div>
        </th>
    );
};
