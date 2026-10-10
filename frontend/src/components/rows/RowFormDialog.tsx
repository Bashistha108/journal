import React, { useState } from 'react';
import type { ColumnDetail } from '../../types/table';
import type { RowDetail } from '../../types/row';
import type { ApiError } from '../../types/api';
import DynamicRowForm from '../forms/DynamicRowForm';
import { motion, AnimatePresence } from 'framer-motion';
import { X, AlertCircle } from 'lucide-react';

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
        <AnimatePresence>
            {isOpen && (
                <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
                    <motion.div 
                        initial={{ opacity: 0 }} 
                        animate={{ opacity: 1 }} 
                        exit={{ opacity: 0 }}
                        className="absolute inset-0 bg-black/80 backdrop-blur-sm" 
                        onClick={onClose}
                    />
                    <motion.div 
                        initial={{ opacity: 0, scale: 0.95, y: 20 }}
                        animate={{ opacity: 1, scale: 1, y: 0 }}
                        exit={{ opacity: 0, scale: 0.95, y: 20 }}
                        className="bg-[#1c1c1c] w-full max-w-2xl border border-[#2e2e2e] rounded-xl flex flex-col max-h-[90vh] relative z-10 shadow-2xl overflow-hidden" 
                        onClick={e => e.stopPropagation()}
                    >
                        <div className="p-6 border-b border-[#2e2e2e] flex justify-between items-center">
                            <h2 className="text-[17px] font-semibold tracking-tight text-white">
                                {initialData ? 'Edit Record' : 'Add Record'}
                            </h2>
                            <button onClick={onClose} className="text-neutral-500 hover:text-white hover:bg-[#2a2a2a] transition-colors p-1.5 rounded-lg">
                                <X className="w-5 h-5" />
                            </button>
                        </div>
                        
                        <AnimatePresence>
                            {apiError?.code === 'ROW_VERSION_CONFLICT' && (
                                <motion.div 
                                    initial={{ opacity: 0, height: 0 }}
                                    animate={{ opacity: 1, height: 'auto' }}
                                    exit={{ opacity: 0, height: 0 }}
                                    className="p-4 bg-yellow-950/50 border-b border-yellow-900/50 text-yellow-200 text-sm flex gap-3"
                                >
                                    <AlertCircle className="w-5 h-5 shrink-0 mt-0.5" />
                                    <div>
                                        <p className="font-semibold mb-1 tracking-wide">Update Conflict</p>
                                        <p className="text-yellow-200/80">This record was modified by someone else since you opened it. Please cancel and reload to see the latest changes.</p>
                                    </div>
                                </motion.div>
                            )}
                        </AnimatePresence>
                        
                        <div className="p-8 overflow-y-auto">
                            <DynamicRowForm
                                tableId={tableId}
                                columns={columns}
                                initialData={initialData}
                                onSubmit={handleSubmit}
                                onCancel={onClose}
                                apiError={apiError}
                            />
                        </div>
                    </motion.div>
                </div>
            )}
        </AnimatePresence>
    );
};

export default RowFormDialog;
