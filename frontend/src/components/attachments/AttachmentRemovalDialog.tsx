import React from 'react';

interface AttachmentRemovalDialogProps {
    isOpen: boolean;
    onConfirm: () => void;
    onCancel: () => void;
}

const AttachmentRemovalDialog: React.FC<AttachmentRemovalDialogProps> = ({ isOpen, onConfirm, onCancel }) => {
    if (!isOpen) return null;

    return (
        <div className="fixed inset-0 z-[60] flex items-center justify-center p-4 bg-black/80 backdrop-blur-sm">
            <div className="bg-[#1a1a1a] rounded-lg shadow-2xl w-full max-w-sm border border-red-900 overflow-hidden">
                <div className="p-5 border-b border-gray-800">
                    <h3 className="text-lg font-semibold text-white">Remove Attachment?</h3>
                </div>
                <div className="p-5">
                    <p className="text-gray-300 text-sm">
                        This image will be staged for removal. It will be permanently deleted when you save the row.
                    </p>
                </div>
                <div className="p-4 bg-[#111] border-t border-gray-800 flex justify-end gap-3">
                    <button
                        type="button"
                        onClick={onCancel}
                        className="px-4 py-2 text-sm text-gray-300 hover:text-white transition-colors"
                    >
                        Cancel
                    </button>
                    <button
                        type="button"
                        onClick={onConfirm}
                        className="px-4 py-2 text-sm bg-red-600 text-white rounded hover:bg-red-500 transition-colors"
                    >
                        Stage Removal
                    </button>
                </div>
            </div>
        </div>
    );
};

export default AttachmentRemovalDialog;
