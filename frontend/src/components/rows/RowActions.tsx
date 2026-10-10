import React, { useState } from 'react';
import { Link } from 'react-router-dom';
import type { RowListItem } from '../../types/row';
import type { ColumnDetail } from '../../types/table';
import { rowApi } from '../../api/rowApi';
import RowFormDialog from './RowFormDialog';
import DeleteRowDialog from './DeleteRowDialog';
import { Eye, Edit2, Trash2 } from 'lucide-react';

interface RowActionsProps {
    tableId: number;
    row: RowListItem;
    columns: ColumnDetail[];
    onRowUpdated: () => void;
}

const RowActions: React.FC<RowActionsProps> = ({ tableId, row, columns, onRowUpdated }) => {
    const [isEditOpen, setIsEditOpen] = useState(false);
    const [isDeleteOpen, setIsDeleteOpen] = useState(false);
    const [detail, setDetail] = useState<any>(null);

    const handleEditClick = async () => {
        try {
            const data = await rowApi.getRow(tableId, row.id);
            setDetail(data);
            setIsEditOpen(true);
        } catch (e) {
            console.error('Failed to fetch row detail', e);
        }
    };

    const handleEditSubmit = async (values: Record<number, any>, expectedVersion?: number, newAttachments?: import('../../types/attachment').AttachmentUpload[], removeAttachmentIds?: number[]) => {
        await rowApi.updateRow(tableId, row.id, { values, expectedVersion, removeAttachmentIds }, newAttachments);
        setIsEditOpen(false);
        onRowUpdated();
    };

    const handleDeleteConfirm = async () => {
        await rowApi.deleteRow(tableId, row.id);
        setIsDeleteOpen(false);
        onRowUpdated();
    };

    return (
        <div className="flex items-center justify-end gap-4 text-neutral-400">
            <Link
                to={`/tables/${tableId}/rows/${row.id}`}
                className="hover:text-white transition-colors"
                title="View Details"
            >
                <Eye className="w-4 h-4" />
            </Link>
            <button
                onClick={handleEditClick}
                className="hover:text-white transition-colors"
                title="Edit Row"
            >
                <Edit2 className="w-4 h-4" />
            </button>
            <button
                onClick={() => setIsDeleteOpen(true)}
                className="hover:text-red-400 transition-colors"
                title="Delete Row"
            >
                <Trash2 className="w-4 h-4" />
            </button>

            {isEditOpen && detail && (
                <RowFormDialog
                    tableId={tableId}
                    columns={columns}
                    initialData={detail}
                    onSubmit={handleEditSubmit}
                    onClose={() => setIsEditOpen(false)}
                    isOpen={isEditOpen}
                />
            )}

            <DeleteRowDialog
                isOpen={isDeleteOpen}
                onConfirm={handleDeleteConfirm}
                onCancel={() => setIsDeleteOpen(false)}
            />
        </div>
    );
};

export default RowActions;
