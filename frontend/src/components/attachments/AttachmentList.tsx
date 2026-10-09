import React, { useState } from 'react';
import type { AttachmentMetadata } from '../../types/attachment';
import { attachmentApi } from '../../api/attachmentApi';

interface AttachmentListProps {
    tableId: number;
    rowId: number;
    attachments: AttachmentMetadata[];
}

const AttachmentList: React.FC<AttachmentListProps> = ({ tableId, rowId, attachments }) => {
    const [lightboxIndex, setLightboxIndex] = useState<number | null>(null);

    if (!attachments || attachments.length === 0) return null;

    const activeAttachment = lightboxIndex !== null ? attachments[lightboxIndex] : null;

    return (
        <div>
            <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-4">
                {attachments.map((att, idx) => (
                    <div 
                        key={att.id} 
                        className="cursor-pointer overflow-hidden rounded-lg border border-gray-700 hover:border-gray-400 transition-colors bg-gray-900 aspect-square"
                        onClick={() => setLightboxIndex(idx)}
                    >
                        <img 
                            src={attachmentApi.buildAttachmentUrl(tableId, rowId, att.id)} 
                            alt={att.filename}
                            className="w-full h-full object-cover hover:scale-105 transition-transform duration-300"
                        />
                    </div>
                ))}
            </div>

            {activeAttachment && (
                <div 
                    className="fixed inset-0 z-[100] flex items-center justify-center bg-black/90 p-4"
                    onClick={() => setLightboxIndex(null)}
                >
                    <button 
                        className="absolute top-4 right-4 text-white text-4xl hover:text-gray-300"
                        onClick={() => setLightboxIndex(null)}
                    >
                        &times;
                    </button>
                    
                    <button
                        className="absolute left-4 top-1/2 -translate-y-1/2 text-white text-5xl hover:text-gray-300 p-4"
                        onClick={(e) => {
                            e.stopPropagation();
                            setLightboxIndex(prev => prev !== null && prev > 0 ? prev - 1 : attachments.length - 1);
                        }}
                    >
                        &#8249;
                    </button>

                    <img 
                        src={attachmentApi.buildAttachmentUrl(tableId, rowId, activeAttachment.id)} 
                        alt={activeAttachment.filename}
                        className="max-w-full max-h-full object-contain"
                        onClick={e => e.stopPropagation()}
                    />
                    
                    <button
                        className="absolute right-4 top-1/2 -translate-y-1/2 text-white text-5xl hover:text-gray-300 p-4"
                        onClick={(e) => {
                            e.stopPropagation();
                            setLightboxIndex(prev => prev !== null && prev < attachments.length - 1 ? prev + 1 : 0);
                        }}
                    >
                        &#8250;
                    </button>
                    
                    <div className="absolute bottom-4 left-0 right-0 text-center text-white text-sm bg-black/50 p-2">
                        {activeAttachment.filename} ({(activeAttachment.sizeBytes / 1024).toFixed(1)} KB)
                    </div>
                </div>
            )}
        </div>
    );
};

export default AttachmentList;
