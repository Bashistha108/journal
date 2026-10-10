import React, { useState } from 'react';
import type { TableDetail, ColumnDetail } from '../../types/table';
import type { RowDetail } from '../../types/row';
import type { AttachmentMetadata } from '../../types/attachment';
import { attachmentApi } from '../../api/attachmentApi';

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

const isImageUrl = (val: string) => {
    if (!isValidUrl(val)) return false;
    return /\.(jpeg|jpg|gif|png|webp|svg)(\?.*)?$/i.test(val);
};

export const RowDetailsView: React.FC<Props> = ({ table, row }) => {
    // Group attachments by column
    const attachmentsByColumn = row.attachments?.reduce((acc, att) => {
        if (!acc[att.columnId]) acc[att.columnId] = [];
        acc[att.columnId].push(att);
        return acc;
    }, {} as Record<number, typeof row.attachments>) || {};

    // Separate columns into images and data
    const imageColumns = table.columns.filter(c => c.dataType === 'IMAGE');
    const dataColumns = table.columns.filter(c => c.dataType !== 'IMAGE');

    const renderFieldValue = (col: ColumnDetail, val: any) => {
        if (val === null || val === undefined || val === '') {
            return null; // Don't render empty fields
        }

        if (col.dataType === 'BOOLEAN') {
            return <span className="text-neutral-300">{val ? 'True' : 'False'}</span>;
        }

        if (col.dataType === 'TEXT' || col.dataType === 'LINK') {
            if (typeof val === 'string' && isValidUrl(val)) {
                return (
                    <a 
                        href={val} 
                        target="_blank" 
                        rel="noopener noreferrer"
                        className="text-blue-400 hover:text-blue-300 hover:underline break-all transition-colors"
                    >
                        {val}
                    </a>
                );
            }
            return <span className="text-neutral-100 break-words leading-relaxed whitespace-pre-wrap">{String(val)}</span>;
        }

        return <span className="text-neutral-100 break-words">{String(val)}</span>;
    };

    // Flatten all attachments from all image columns for the gallery
    const allImageAttachments = imageColumns.flatMap(col => {
        const atts = attachmentsByColumn[col.id] || [];
        return atts.map(att => ({
            ...att,
            columnName: col.displayName
        }));
    });

    const [activeImageIdx, setActiveImageIdx] = useState(0);
    const activeImage = allImageAttachments[activeImageIdx];

    return (
        <div className="space-y-12 pb-12 w-full max-w-[1200px] mx-auto">
            {allImageAttachments.length > 0 && (
                <div className="bg-[#1c1c1c] border border-[#2e2e2e] rounded-xl overflow-hidden shadow-lg">
                    <div className="p-4 border-b border-[#2e2e2e] flex justify-between items-center bg-[#252525]">
                        <h3 className="text-sm font-semibold text-neutral-200">{activeImage.columnName}</h3>
                        <span className="text-xs text-neutral-400 bg-[#151515] px-2 py-1 rounded border border-[#2e2e2e]">{activeImageIdx + 1} of {allImageAttachments.length}</span>
                    </div>
                    
                    {/* Main Hero Image */}
                    <div className="bg-[#121212] p-4 md:p-8 flex justify-center items-center relative min-h-[300px]">
                        <img 
                            src={attachmentApi.buildAttachmentUrl(table.id, row.id, activeImage.id)} 
                            alt={activeImage.filename}
                            loading="lazy"
                            className="max-h-[600px] w-auto object-contain rounded"
                        />
                    </div>
                    
                    {/* Hero Footer */}
                    <div className="p-3 border-t border-[#2e2e2e] bg-[#252525] flex justify-between items-center text-xs text-neutral-400">
                        <span>{activeImage.filename}</span>
                        <span>{(activeImage.sizeBytes / 1024).toFixed(1)} KB</span>
                    </div>

                    {/* Thumbnails */}
                    {allImageAttachments.length > 1 && (
                        <div className="p-4 border-t border-[#2e2e2e] bg-[#1c1c1c] flex gap-4 overflow-x-auto">
                            {allImageAttachments.map((att, idx) => (
                                <button
                                    key={`${att.columnName}-${att.id}`}
                                    onClick={() => setActiveImageIdx(idx)}
                                    className={`relative flex flex-col items-center gap-2 shrink-0 group ${idx === activeImageIdx ? 'opacity-100' : 'opacity-60 hover:opacity-100 transition-opacity'}`}
                                >
                                    <div className={`w-24 h-20 rounded-lg overflow-hidden border-2 ${idx === activeImageIdx ? 'border-yellow-500' : 'border-[#2e2e2e] group-hover:border-neutral-500'} bg-[#121212]`}>
                                        <img 
                                            src={attachmentApi.buildAttachmentUrl(table.id, row.id, att.id)} 
                                            alt={att.filename}
                                            className="w-full h-full object-cover"
                                        />
                                    </div>
                                    <span className="text-[11px] text-neutral-400 font-medium truncate w-24 text-center">
                                        {att.columnName}
                                    </span>
                                </button>
                            ))}
                        </div>
                    )}
                </div>
            )}

            <div className="bg-[#1c1c1c] border border-[#2e2e2e] rounded-xl shadow-lg">
                <dl className="grid grid-cols-1 lg:grid-cols-2 divide-y lg:divide-y-0 lg:gap-px bg-[#2e2e2e]">
                    {dataColumns.map((col, idx) => {
                        const val = row.values[col.id];
                        if (val === null || val === undefined || val === '') return null;
                        
                        return (
                            <div 
                                key={col.id} 
                                className="flex flex-col sm:flex-row sm:items-start p-5 sm:p-6 bg-[#1c1c1c]"
                            >
                                <dt className="w-full sm:w-48 shrink-0 text-sm font-medium text-neutral-500 capitalize mb-1 sm:mb-0">
                                    {col.displayName.replace(/_/g, ' ')}
                                </dt>
                                <dd className="flex-1 text-[13px] text-neutral-200">
                                    {renderFieldValue(col, val)}
                                </dd>
                            </div>
                        );
                    })}
                </dl>
            </div>
        </div>
    );
};
