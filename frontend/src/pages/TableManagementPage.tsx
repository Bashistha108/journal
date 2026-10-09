import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { tableApi, type TableDetail } from '../api/tables';
import CreateTableModal from '../components/CreateTableModal';

export default function TableManagementPage() {
  const [tables, setTables] = useState<TableDetail[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [isModalOpen, setIsModalOpen] = useState(false);

  const fetchTables = async () => {
    try {
      setLoading(true);
      const data = await tableApi.listTables();
      setTables(data);
      setError('');
    } catch (err: any) {
      setError(err.message || 'Failed to fetch tables');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchTables();
  }, []);

  return (
    <div style={{ padding: '2rem', maxWidth: '1200px', margin: '0 auto' }}>
      <header style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '2rem' }}>
        <h1 style={{ fontSize: '2rem', fontWeight: 600 }}>Tables</h1>
        <button className="btn btn-primary" onClick={() => setIsModalOpen(true)}>
          + Create Table
        </button>
      </header>

      {error && (
        <div style={{ background: 'var(--danger-color)', color: '#fff', padding: '1rem', borderRadius: '4px', marginBottom: '1rem' }}>
          {error}
        </div>
      )}

      {loading ? (
        <div style={{ color: 'var(--text-muted)' }}>Loading...</div>
      ) : tables.length === 0 ? (
        <div style={{ background: 'var(--card-bg)', border: '1px solid var(--border-color)', borderRadius: '8px', padding: '3rem', textAlign: 'center' }}>
          <p style={{ color: 'var(--text-muted)', marginBottom: '1rem' }}>No tables found.</p>
        </div>
      ) : (
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(300px, 1fr))', gap: '1rem' }}>
          {tables.map(table => (
            <Link to={`/tables/${table.id}`} key={table.id} style={{ textDecoration: 'none', color: 'inherit' }}>
              <div style={{ background: 'var(--card-bg)', border: '1px solid var(--border-color)', borderRadius: '8px', padding: '1.5rem', cursor: 'pointer', transition: 'border-color 0.2s' }}
                   onMouseOver={e => e.currentTarget.style.borderColor = 'var(--primary-color)'}
                   onMouseOut={e => e.currentTarget.style.borderColor = 'var(--border-color)'}>
                <h2 style={{ fontSize: '1.25rem', marginBottom: '0.5rem' }}>{table.displayName}</h2>
                <p style={{ color: 'var(--text-muted)', fontSize: '0.875rem' }}>
                  {table.columns.length} columns
                </p>
              </div>
            </Link>
          ))}
        </div>
      )}

      {isModalOpen && (
        <CreateTableModal 
          onClose={() => setIsModalOpen(false)} 
          onSuccess={() => {
            setIsModalOpen(false);
            fetchTables();
          }} 
        />
      )}
    </div>
  );
}
