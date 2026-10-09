import React, { useEffect } from 'react';
import type { AttachmentMetadata } from '../../types/attachment';
import { attachmentApi } from '../../api/attachmentApi';

interface Props {
    isOpen: boolean;
    tableId: number;
    rowId: number;
    attachments: AttachmentMetadata[];
    currentIndex: number;
    onClose: () => void;
    onNext: () => void;
    onPrev: () => void;
}

export const ImagePreviewDialog: React.FC<Props> = ({
    isOpen, tableId, rowId, attachments, currentIndex, onClose, onNext, onPrev
}) => {
    useEffect(() => {
        const handleKeyDown = (e: KeyboardEvent) => {
            if (!isOpen) return;
            if (e.key === 'Escape') onClose();
            if (e.key === 'ArrowRight') onNext();
            if (e.key === 'ArrowLeft') onPrev();
        };
        window.addEventListener('keydown', handleKeyDown);
        return () => window.removeEventListener('keydown', handleKeyDown);
    }, [isOpen, onClose, onNext, onPrev]);

    if (!isOpen || !attachments.length || currentIndex < 0 || currentIndex >= attachments.length) return null;

    const currentAtt = attachments[currentIndex];
    const url = attachmentApi.buildAttachmentUrl(tableId, rowId, currentAtt.id);

    return (
        <div 
            className="fixed inset-0 z-[100] flex items-center justify-center bg-black/90 p-4"
            onClick={onClose}
        >
            <button 
                className="absolute top-4 right-4 text-slate-300 text-4xl hover:text-white"
                onClick={onClose}
                title="Close (Esc)"
            >
                &times;
            </button>
            
            <button
                className="absolute left-4 top-1/2 -translate-y-1/2 text-slate-400 text-5xl hover:text-white p-4"
                onClick={(e) => { e.stopPropagation(); onPrev(); }}
                title="Previous (Left Arrow)"
            >
                &#8249;
            </button>

            <img 
                src={url} 
                alt={currentAtt.filename}
                className="max-w-full max-h-[90vh] object-contain shadow-2xl"
                onClick={e => e.stopPropagation()}
            />
            
            <button
                className="absolute right-4 top-1/2 -translate-y-1/2 text-slate-400 text-5xl hover:text-white p-4"
                onClick={(e) => { e.stopPropagation(); onNext(); }}
                title="Next (Right Arrow)"
            >
                &#8250;
            </button>
            
            <div className="absolute bottom-6 bg-slate-900/80 px-4 py-2 rounded-lg text-slate-300 text-sm border border-slate-700 backdrop-blur-sm">
                {currentIndex + 1} / {attachments.length} - {currentAtt.filename} ({(currentAtt.sizeBytes / 1024).toFixed(1)} KB)
            </div>
        </div>
    );
};
