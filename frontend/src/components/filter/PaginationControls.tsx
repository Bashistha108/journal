import React from 'react';
import type { PageResponse } from '../../types/query';

interface Props {
    response: PageResponse<any> | null;
    onPageChange: (page: number) => void;
}

export const PaginationControls: React.FC<Props> = ({ response, onPageChange }) => {
    if (!response) return null;

    const { page, totalPages, totalItems } = response;
    const maxPages = Math.max(1, totalPages);

    const getVisiblePages = () => {
        const pages: (number | string)[] = [];
        if (maxPages <= 5) {
            for (let i = 1; i <= maxPages; i++) pages.push(i);
        } else {
            if (page <= 3) {
                pages.push(1, 2, 3, 4, '...', maxPages);
            } else if (page >= maxPages - 2) {
                pages.push(1, '...', maxPages - 3, maxPages - 2, maxPages - 1, maxPages);
            } else {
                pages.push(1, '...', page - 1, page, page + 1, '...', maxPages);
            }
        }
        return pages;
    };

    return (
        <div className="flex items-center justify-between p-4 bg-[#1c1c1c] text-sm text-neutral-400 rounded-b-xl">
            <div>
                Total Rows: {totalItems}
            </div>
            <div className="flex items-center gap-1">
                <button
                    className="flex items-center justify-center px-3 h-8 rounded-md hover:bg-[#2e2e2e] hover:text-white disabled:opacity-50 transition-colors"
                    disabled={page <= 1}
                    onClick={() => onPageChange(page - 1)}
                >
                    &lt; Prev
                </button>
                
                {getVisiblePages().map((p, idx) => (
                    typeof p === 'number' ? (
                        <button
                            key={idx}
                            onClick={() => onPageChange(p)}
                            className={`flex items-center justify-center min-w-[32px] h-8 px-2 rounded-md text-sm transition-colors ${page === p ? 'bg-[#333] text-white border border-[#444]' : 'hover:bg-[#2e2e2e] hover:text-white'}`}
                        >
                            {p}
                        </button>
                    ) : (
                        <span key={idx} className="flex items-center justify-center min-w-[32px] h-8 text-neutral-500">
                            {p}
                        </span>
                    )
                ))}

                <button
                    className="flex items-center justify-center px-3 h-8 rounded-md hover:bg-[#2e2e2e] hover:text-white disabled:opacity-50 transition-colors"
                    disabled={page >= maxPages}
                    onClick={() => onPageChange(page + 1)}
                >
                    Next &gt;
                </button>
            </div>
        </div>
    );
};
