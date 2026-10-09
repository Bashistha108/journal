import { useState } from 'react';
import { tableApi, type CreateColumnRequest } from '../api/tables';

interface CreateTableModalProps {
  onClose: () => void;
  onSuccess: () => void;
}

const DATA_TYPES = ['TEXT', 'INTEGER', 'DECIMAL', 'BOOLEAN', 'DATE', 'DATETIME', 'SELECT', 'LINK', 'IMAGE'];

export default function CreateTableModal({ onClose, onSuccess }: CreateTableModalProps) {
  const [name, setName] = useState('');
  const [columns, setColumns] = useState<CreateColumnRequest[]>([{ name: '', dataType: 'TEXT', selectOptions: [] }]);
  const [error, setError] = useState('');
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({});
  const [submitting, setSubmitting] = useState(false);

  const addColumn = () => {
    setColumns([...columns, { name: '', dataType: 'TEXT', selectOptions: [] }]);
  };

  const removeColumn = (index: number) => {
    setColumns(columns.filter((_, i) => i !== index));
  };

  const updateColumn = (index: number, field: keyof CreateColumnRequest, value: any) => {
    const newCols = [...columns];
    newCols[index] = { ...newCols[index], [field]: value };
    // reset options if not SELECT
    if (field === 'dataType' && value !== 'SELECT') {
      newCols[index].selectOptions = [];
    }
    setColumns(newCols);
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setFieldErrors({});
    setSubmitting(true);

    try {
      await tableApi.createTable({ name, columns });
      onSuccess();
    } catch (err: any) {
      if (err.fields && err.fields.length > 0) {
        const fErrors: Record<string, string> = {};
        err.fields.forEach((f: any) => {
          fErrors[f.field] = f.message;
        });
        setFieldErrors(fErrors);
      } else {
        setError(err.message || 'Failed to create table');
      }
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal-content" onClick={e => e.stopPropagation()} style={{ maxWidth: '600px' }}>
        <h2 style={{ fontSize: '1.5rem', marginBottom: '1.5rem' }}>Create Table</h2>
        
        {error && (
          <div style={{ background: 'rgba(239, 68, 68, 0.1)', color: 'var(--danger-color)', padding: '0.75rem', borderRadius: '4px', marginBottom: '1rem', border: '1px solid var(--danger-color)' }}>
            {error}
          </div>
        )}

        <form onSubmit={handleSubmit}>
          <div style={{ marginBottom: '1.5rem' }}>
            <label style={{ display: 'block', marginBottom: '0.5rem', fontWeight: 500 }}>Table Name</label>
            <input 
              type="text" 
              value={name} 
              onChange={e => setName(e.target.value)} 
              placeholder="e.g. Daily Trades" 
              autoFocus
            />
            {fieldErrors['name'] && <div style={{ color: 'var(--danger-color)', fontSize: '0.875rem', marginTop: '0.25rem' }}>{fieldErrors['name']}</div>}
          </div>

          <div style={{ marginBottom: '1.5rem' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1rem' }}>
              <label style={{ fontWeight: 500 }}>Columns</label>
              <button type="button" onClick={addColumn} className="btn btn-secondary" style={{ padding: '0.25rem 0.75rem', fontSize: '0.875rem' }}>
                + Add Column
              </button>
            </div>
            {fieldErrors['columns'] && <div style={{ color: 'var(--danger-color)', fontSize: '0.875rem', marginBottom: '1rem' }}>{fieldErrors['columns']}</div>}
            
            <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
              {columns.map((col, idx) => (
                <div key={idx} style={{ background: 'var(--bg-color)', padding: '1rem', borderRadius: '8px', border: '1px solid var(--border-color)' }}>
                  <div style={{ display: 'flex', gap: '1rem', alignItems: 'flex-start' }}>
                    <div style={{ flex: 2 }}>
                      <input 
                        type="text" 
                        value={col.name} 
                        onChange={e => updateColumn(idx, 'name', e.target.value)} 
                        placeholder="Column Name" 
                      />
                      {fieldErrors[`columns[${idx}].name`] && <div style={{ color: 'var(--danger-color)', fontSize: '0.875rem', marginTop: '0.25rem' }}>{fieldErrors[`columns[${idx}].name`]}</div>}
                    </div>
                    <div style={{ flex: 1 }}>
                      <select value={col.dataType} onChange={e => updateColumn(idx, 'dataType', e.target.value)}>
                        {DATA_TYPES.map(t => <option key={t} value={t}>{t}</option>)}
                      </select>
                      {fieldErrors[`columns[${idx}].dataType`] && <div style={{ color: 'var(--danger-color)', fontSize: '0.875rem', marginTop: '0.25rem' }}>{fieldErrors[`columns[${idx}].dataType`]}</div>}
                    </div>
                    <button type="button" onClick={() => removeColumn(idx)} className="btn btn-danger" style={{ padding: '0.5rem', borderRadius: '4px' }} title="Remove Column">
                      X
                    </button>
                  </div>
                  
                  {col.dataType === 'SELECT' && (
                    <div style={{ marginTop: '1rem', borderTop: '1px solid var(--border-color)', paddingTop: '1rem' }}>
                      <label style={{ display: 'block', fontSize: '0.875rem', marginBottom: '0.5rem' }}>Options (comma separated)</label>
                      <input 
                        type="text" 
                        value={col.selectOptions?.join(', ') || ''} 
                        onChange={e => {
                          const opts = e.target.value.split(',').map(s => s.trim()).filter(s => s);
                          updateColumn(idx, 'selectOptions', opts);
                        }} 
                        placeholder="e.g. Option 1, Option 2" 
                      />
                      {fieldErrors[`columns[${idx}].selectOptions`] && <div style={{ color: 'var(--danger-color)', fontSize: '0.875rem', marginTop: '0.25rem' }}>{fieldErrors[`columns[${idx}].selectOptions`]}</div>}
                    </div>
                  )}
                </div>
              ))}
            </div>
          </div>

          <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '1rem', marginTop: '2rem' }}>
            <button type="button" onClick={onClose} className="btn btn-secondary" disabled={submitting}>Cancel</button>
            <button type="submit" className="btn btn-primary" disabled={submitting || columns.length === 0}>
              {submitting ? 'Creating...' : 'Create Table'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
