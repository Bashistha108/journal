import { describe, it, expect } from 'vitest';
import { render, screen } from './test/test-utils';
import App from './App';

describe('App Smoke Test', () => {
  it('renders the application correctly', () => {
    render(<App />);
    expect(screen.getByText('Trading Journal')).toBeInTheDocument();
  });
});
