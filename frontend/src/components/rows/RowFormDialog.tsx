import React, { useState } from 'react';
import type { ColumnDetail } from '../../types/table';
import type { RowDetail } from '../../types/row';
import type { ApiError } from '../../types/api';
import DynamicRowForm from '../forms/DynamicRowForm';

import type { AttachmentUpload } from '../../types/attachment';

interface RowFormDialogProps {
    tableId: number;
    columns: ColumnDetail[];
    initialData?: RowDetail | null;
    onSubmit: (values: Record<number, any>, expectedVersion?: number, newAttachments?: AttachmentUpload[], removeAttachmentIds?: number[]) => Promise<void>;
    onClose: () => void;
    isOpen: boolean;
}

const RowFormDialog: React.FC<RowFormDialogProps> = ({ tableId, columns, initialData, onSubmit, onClose, isOpen }) => {
    const [apiError, setApiError] = useState<ApiError | null>(null);

    if (!isOpen) return null;

    const handleSubmit = async (values: Record<number, any>, expectedVersion?: number, newAttachments?: AttachmentUpload[], removeAttachmentIds?: number[]) => {
        setApiError(null);
        try {
            await onSubmit(values, expectedVersion, newAttachments, removeAttachmentIds);
            onClose();
        } catch (err: any) {
            setApiError(err);
        }
    };

    return (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/70 backdrop-blur-sm">
            <div className="bg-[#111] rounded-lg shadow-2xl w-full max-w-lg border border-gray-800 flex flex-col max-h-full">
                <div className="p-6 border-b border-gray-800 flex justify-between items-center shrink-0">
                    <h2 className="text-xl font-semibold text-white">
                        {initialData ? 'Edit Row' : 'Add Row'}
                    </h2>
                    <button onClick={onClose} className="text-gray-400 hover:text-white text-2xl leading-none">&times;</button>
                </div>
                
                {apiError?.code === 'ROW_VERSION_CONFLICT' && (
                    <div className="p-4 bg-yellow-900/30 border-b border-yellow-700/50 text-yellow-200 text-sm">
                        <p className="font-semibold mb-1">Update Conflict</p>
                        <p>This row was modified by someone else since you opened it. Please cancel and reload to see the latest changes.</p>
                    </div>
                )}
                
                <div className="p-6 overflow-y-auto">
                    <DynamicRowForm
                        tableId={tableId}
                        columns={columns}
                        initialData={initialData}
                        onSubmit={handleSubmit}
                        onCancel={onClose}
                        apiError={apiError}
                    />
                </div>
            </div>
        </div>
    );
};

export default RowFormDialog;
