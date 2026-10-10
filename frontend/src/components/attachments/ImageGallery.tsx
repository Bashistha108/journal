import React, { useState } from 'react';
import type { AttachmentMetadata } from '../../types/attachment';
import { attachmentApi } from '../../api/attachmentApi';
import { ImagePreviewDialog } from './ImagePreviewDialog';
import { motion } from 'framer-motion';

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
            <div className="flex flex-wrap gap-4 mt-2">
                {attachments.map((att, idx) => (
                    <motion.div 
                        initial={{ opacity: 0, scale: 0.8 }}
                        animate={{ opacity: 1, scale: 1 }}
                        transition={{ duration: 0.3, delay: idx * 0.1 }}
                        key={att.id} 
                        className="cursor-pointer overflow-hidden rounded-xl border border-white/10 hover:border-indigo-500/50 shadow-lg hover:shadow-indigo-500/20 transition-all bg-black/40 aspect-square group w-32 h-32 relative"
                        onClick={() => setSelectedIndex(idx)}
                    >
                        <div className="absolute inset-0 bg-gradient-to-t from-black/60 to-transparent opacity-0 group-hover:opacity-100 transition-opacity z-10 pointer-events-none" />
                        <img 
                            src={attachmentApi.buildAttachmentUrl(tableId, rowId, att.id)} 
                            alt={att.filename}
                            loading="lazy"
                            className="w-full h-full object-cover group-hover:scale-110 transition-transform duration-500"
                        />
                    </motion.div>
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
