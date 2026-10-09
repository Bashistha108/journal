import React, { useState } from 'react';
import type { ApiError } from '../../types/api';

interface DeleteRowDialogProps {
    isOpen: boolean;
    onConfirm: () => Promise<void>;
    onCancel: () => void;
}

const DeleteRowDialog: React.FC<DeleteRowDialogProps> = ({ isOpen, onConfirm, onCancel }) => {
    const [isDeleting, setIsDeleting] = useState(false);
    const [error, setError] = useState<ApiError | null>(null);

    if (!isOpen) return null;

    const handleConfirm = async () => {
        setIsDeleting(true);
        setError(null);
        try {
            await onConfirm();
        } catch (err: any) {
            setError(err);
            setIsDeleting(false);
        }
    };

    return (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/70 backdrop-blur-sm">
            <div className="bg-[#111] rounded-lg shadow-2xl w-full max-w-md border border-gray-800">
                <div className="p-6 border-b border-gray-800">
                    <h2 className="text-xl font-semibold text-white">Delete Row</h2>
                </div>
                
                <div className="p-6">
                    <p className="text-gray-300 mb-4">
                        Are you sure you want to permanently delete this row? This action cannot be undone.
                    </p>
                    
                    {error && (
                        <div className="mb-4 p-3 bg-red-900/50 text-red-200 border border-red-700 rounded text-sm">
                            {error.message}
                        </div>
                    )}
                    
                    <div className="flex justify-end gap-3 mt-6">
                        <button
                            type="button"
                            onClick={onCancel}
                            className="px-4 py-2 text-gray-300 hover:text-white transition-colors"
                            disabled={isDeleting}
                        >
                            Cancel
                        </button>
                        <button
                            type="button"
                            onClick={handleConfirm}
                            className="px-4 py-2 bg-red-600 text-white font-medium rounded hover:bg-red-700 transition-colors disabled:opacity-50"
                            disabled={isDeleting}
                        >
                            {isDeleting ? 'Deleting...' : 'Delete Permanently'}
                        </button>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default DeleteRowDialog;
