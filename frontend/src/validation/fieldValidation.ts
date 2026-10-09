import type { ColumnDetail } from '../types/table';

export const validateField = (value: any, column: ColumnDetail): string | null => {
    if (value === null || value === undefined || value === '') {
        return null;
    }
    
    switch (column.dataType) {
        case 'TEXT':
            if (new Blob([value]).size > 1048576) {
                return 'Text exceeds maximum size of 1MB';
            }
            break;
        case 'INTEGER':
            if (isNaN(Number(value)) || !Number.isInteger(Number(value))) {
                return 'Must be a whole number';
            }
            break;
        case 'DECIMAL':
            if (isNaN(Number(value))) {
                return 'Must be a valid decimal';
            }
            if (value.toString().toUpperCase().includes('E')) {
                return 'Exponents are not allowed';
            }
            if (value.toString().includes('.')) {
                const parts = value.toString().split('.');
                if (parts[1].length > 6) {
                    return 'Maximum 6 fraction digits allowed';
                }
                if (parts[0].length > 12) {
                    return 'Maximum 12 integer digits allowed';
                }
            } else if (value.toString().length > 12) {
                return 'Maximum 12 integer digits allowed';
            }
            break;
        case 'LINK':
            if (value.length > 2048) {
                return 'Link exceeds 2048 characters';
            }
            if (/\s/.test(value)) {
                return 'Link cannot contain whitespace';
            }
            if (!value.startsWith('http://') && !value.startsWith('https://')) {
                return 'Link must use http or https scheme';
            }
            break;
    }
    return null;
};
