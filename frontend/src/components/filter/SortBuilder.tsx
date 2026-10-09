import React from 'react';
import type { SortRequest } from '../../types/query';
import type { ColumnDetail } from '../../types/table';

interface Props {
    columns: ColumnDetail[];
    currentSort?: SortRequest;
    onChange: (sort?: SortRequest) => void;
}

export const SortBuilder: React.FC<Props> = ({ columns, currentSort, onChange }) => {
    const handleSortChange = (columnId: number, direction: 'ASC' | 'DESC') => {
        onChange({ columnId, direction });
    };

    const clearSort = () => onChange(undefined);

    return (
        <div className="flex items-center gap-2 text-sm bg-slate-800 p-2 rounded">
            <span className="text-slate-400">Sort:</span>
            <select
                className="bg-slate-900 border border-slate-700 rounded p-1 text-slate-200"
                value={currentSort?.columnId || ''}
                onChange={e => {
                    const val = e.target.value;
                    if (val) handleSortChange(Number(val), currentSort?.direction || 'ASC');
                    else clearSort();
                }}
            >
                <option value="">Default (ID)</option>
                {columns.map(c => (
                    <option key={c.id} value={c.id}>{c.displayName}</option>
                ))}
            </select>
            {currentSort && (
                <button
                    className="p-1 rounded bg-slate-700 hover:bg-slate-600 text-slate-300"
                    onClick={() => handleSortChange(currentSort.columnId, currentSort.direction === 'ASC' ? 'DESC' : 'ASC')}
                >
                    {currentSort.direction}
                </button>
            )}
        </div>
    );
};
