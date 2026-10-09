import React from 'react';
import type { PageResponse } from '../../types/query';

interface Props {
    response: PageResponse<any> | null;
    onPageChange: (page: number) => void;
}

export const PaginationControls: React.FC<Props> = ({ response, onPageChange }) => {
    if (!response) return null;

    const { page, totalPages, totalItems, pageSize } = response;
    const start = (page - 1) * pageSize + 1;
    const end = Math.min(page * pageSize, totalItems);

    return (
        <div className="flex items-center justify-between p-4 border-t border-slate-700 bg-slate-900 text-sm text-slate-300">
            <div>
                Showing {totalItems === 0 ? 0 : start} to {end} of {totalItems} items
            </div>
            <div className="flex items-center gap-2">
                <button
                    className="px-3 py-1 rounded bg-slate-800 hover:bg-slate-700 disabled:opacity-50"
                    disabled={page <= 1}
                    onClick={() => onPageChange(page - 1)}
                >
                    Previous
                </button>
                <span>Page {page} of {totalPages === 0 ? 1 : totalPages}</span>
                <button
                    className="px-3 py-1 rounded bg-slate-800 hover:bg-slate-700 disabled:opacity-50"
                    disabled={page >= totalPages}
                    onClick={() => onPageChange(page + 1)}
                >
                    Next
                </button>
            </div>
        </div>
    );
};
