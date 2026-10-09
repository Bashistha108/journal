import React, { useRef, useState, useEffect } from 'react';
import type { AttachmentMetadata, AttachmentUpload } from '../../types/attachment';
import { attachmentApi } from '../../api/attachmentApi';
import AttachmentRemovalDialog from './AttachmentRemovalDialog';

interface AttachmentPickerProps {
    tableId: number;
    rowId?: number;
    columnId: number;
    existingAttachments: AttachmentMetadata[];
    newAttachments: AttachmentUpload[];
    removeAttachmentIds: number[];
    onAddAttachments: (uploads: AttachmentUpload[]) => void;
    onRemoveNewAttachment: (clientRef: string) => void;
    onStageRemoval: (attachmentId: number) => void;
    onUnstageRemoval: (attachmentId: number) => void;
}

const AttachmentPicker: React.FC<AttachmentPickerProps> = ({
    tableId,
    rowId,
    columnId,
    existingAttachments,
    newAttachments,
    removeAttachmentIds,
    onAddAttachments,
    onRemoveNewAttachment,
    onStageRemoval,
    onUnstageRemoval
}) => {
    const fileInputRef = useRef<HTMLInputElement>(null);
    const [previewUrls, setPreviewUrls] = useState<Record<string, string>>({});
    const [error, setError] = useState<string | null>(null);
    const [removalConfirmId, setRemovalConfirmId] = useState<number | null>(null);

    const activeExisting = existingAttachments.filter(a => !removeAttachmentIds.includes(a.id));
    const totalCount = activeExisting.length + newAttachments.length;
    const canAdd = totalCount < 10;

    useEffect(() => {
        const urls: Record<string, string> = {};
        newAttachments.forEach(upload => {
            urls[upload.clientRef] = URL.createObjectURL(upload.file);
        });
        setPreviewUrls(urls);
        return () => {
            Object.values(urls).forEach(url => URL.revokeObjectURL(url));
        };
    }, [newAttachments]);

    const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        setError(null);
        if (!e.target.files) return;
        
        const files = Array.from(e.target.files);
        if (totalCount + files.length > 10) {
            setError(`Cannot exceed 10 attachments per row. You selected ${files.length} but only have space for ${10 - totalCount}.`);
            if (fileInputRef.current) fileInputRef.current.value = '';
            return;
        }

        const validUploads: AttachmentUpload[] = [];
        for (const file of files) {
            if (file.size > 10 * 1024 * 1024) {
                setError(`File ${file.name} exceeds 10MB limit.`);
                if (fileInputRef.current) fileInputRef.current.value = '';
                return;
            }
            if (!file.type.startsWith('image/')) {
                setError(`File ${file.name} is not an image.`);
                if (fileInputRef.current) fileInputRef.current.value = '';
                return;
            }
            validUploads.push({
                file,
                clientRef: `upload_${Date.now()}_${Math.random().toString(36).substring(7)}`,
                columnId
            });
        }
        
        onAddAttachments(validUploads);
        if (fileInputRef.current) fileInputRef.current.value = '';
    };

    return (
        <div className="flex flex-col gap-3">
            {error && <div className="text-red-500 text-sm p-2 bg-red-900/30 rounded border border-red-800">{error}</div>}
            
            <div className="flex flex-wrap gap-4">
                {activeExisting.map(att => (
                    <div key={att.id} className="relative group w-24 h-24 rounded overflow-hidden bg-gray-800 border border-gray-700">
                        {rowId && (
                            <img 
                                src={attachmentApi.buildAttachmentUrl(tableId, rowId, att.id)} 
                                alt={att.filename}
                                className="w-full h-full object-cover"
                            />
                        )}
                        <button
                            type="button"
                            onClick={() => setRemovalConfirmId(att.id)}
                            className="absolute top-1 right-1 bg-black/60 text-white rounded-full w-6 h-6 flex items-center justify-center opacity-0 group-hover:opacity-100 hover:bg-red-600 transition-all"
                        >
                            &times;
                        </button>
                    </div>
                ))}

                {newAttachments.map(upload => (
                    <div key={upload.clientRef} className="relative group w-24 h-24 rounded overflow-hidden bg-gray-800 border border-blue-700 shadow-[0_0_8px_rgba(59,130,246,0.5)]">
                        <img 
                            src={previewUrls[upload.clientRef]} 
                            alt={upload.file.name}
                            className="w-full h-full object-cover"
                        />
                        <div className="absolute bottom-0 left-0 right-0 bg-blue-600/80 text-[10px] text-white px-1 truncate text-center">
                            New
                        </div>
                        <button
                            type="button"
                            onClick={() => onRemoveNewAttachment(upload.clientRef)}
                            className="absolute top-1 right-1 bg-black/60 text-white rounded-full w-6 h-6 flex items-center justify-center opacity-0 group-hover:opacity-100 hover:bg-red-600 transition-all"
                        >
                            &times;
                        </button>
                    </div>
                ))}

                {removeAttachmentIds.map(id => {
                    const att = existingAttachments.find(a => a.id === id);
                    if (!att) return null;
                    return (
                        <div key={id} className="relative w-24 h-24 rounded overflow-hidden bg-gray-800 border border-red-700 opacity-50 grayscale">
                            {rowId && (
                                <img 
                                    src={attachmentApi.buildAttachmentUrl(tableId, rowId, att.id)} 
                                    alt={att.filename}
                                    className="w-full h-full object-cover"
                                />
                            )}
                            <div className="absolute inset-0 flex items-center justify-center">
                                <span className="bg-red-600 text-white text-xs font-bold px-2 py-1 rounded shadow-lg transform -rotate-12">
                                    REMOVED
                                </span>
                            </div>
                            <button
                                type="button"
                                onClick={() => onUnstageRemoval(id)}
                                className="absolute top-1 right-1 bg-black/80 text-white text-xs px-2 py-1 rounded hover:bg-gray-700 transition-all z-10"
                            >
                                Undo
                            </button>
                        </div>
                    );
                })}

                {canAdd && (
                    <button
                        type="button"
                        onClick={() => fileInputRef.current?.click()}
                        className="w-24 h-24 rounded flex flex-col items-center justify-center bg-gray-800 border border-dashed border-gray-600 hover:border-gray-400 hover:bg-gray-700 transition-colors cursor-pointer text-gray-400 hover:text-white"
                    >
                        <span className="text-2xl font-light mb-1">+</span>
                        <span className="text-xs">Add Image</span>
                    </button>
                )}
            </div>

            <input
                type="file"
                ref={fileInputRef}
                onChange={handleFileChange}
                accept="image/png, image/jpeg, image/webp"
                multiple
                className="hidden"
            />
            
            {removalConfirmId && (
                <AttachmentRemovalDialog
                    isOpen={true}
                    onConfirm={() => {
                        onStageRemoval(removalConfirmId);
                        setRemovalConfirmId(null);
                    }}
                    onCancel={() => setRemovalConfirmId(null)}
                />
            )}
        </div>
    );
};

export default AttachmentPicker;
