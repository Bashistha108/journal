import React, { useState } from 'react';
import type { AttachmentMetadata } from '../../types/attachment';
import { attachmentApi } from '../../api/attachmentApi';
import { ImagePreviewDialog } from './ImagePreviewDialog';

interface Props {
    tableId: number;
    rowId: number;
    attachments: AttachmentMetadata[];
}

export const ImageGallery: React.FC<Props> = ({ tableId, rowId, attachments }) => {
    const [selectedIndex, setSelectedIndex] = useState<number | null>(null);

    if (!attachments || attachments.length === 0) return null;

    return (
        <div>
            <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 gap-4">
                {attachments.map((att, idx) => (
                    <div 
                        key={att.id} 
                        className="cursor-pointer overflow-hidden rounded-lg border border-slate-700 hover:border-indigo-500 transition-colors bg-slate-800 aspect-square group"
                        onClick={() => setSelectedIndex(idx)}
                    >
                        <img 
                            src={attachmentApi.buildAttachmentUrl(tableId, rowId, att.id)} 
                            alt={att.filename}
                            loading="lazy"
                            className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-300"
                        />
                    </div>
                ))}
            </div>

            <ImagePreviewDialog
                isOpen={selectedIndex !== null}
                tableId={tableId}
                rowId={rowId}
                attachments={attachments}
                currentIndex={selectedIndex ?? -1}
                onClose={() => setSelectedIndex(null)}
                onNext={() => setSelectedIndex(prev => prev !== null && prev < attachments.length - 1 ? prev + 1 : 0)}
                onPrev={() => setSelectedIndex(prev => prev !== null && prev > 0 ? prev - 1 : attachments.length - 1)}
            />
        </div>
    );
};
