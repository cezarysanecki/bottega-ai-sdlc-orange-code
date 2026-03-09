import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import { AppLayout } from '../app/AppLayout';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { Provider } from 'react-redux';
import { legacyStore } from '../shared/api/legacyStore';
import { AuthProvider } from '../shared/AuthContext';
import { NotificationProvider } from '../app/contexts/NotificationContext';

const renderWithProviders = () => {
  const client = new QueryClient();
  vi.spyOn(window, 'fetch').mockResolvedValue({
    ok: true,
    json: async () => []
  } as Response);

  return render(
    <BrowserRouter>
      <NotificationProvider>
        <AuthProvider>
          <Provider store={legacyStore}>
            <QueryClientProvider client={client}>
              <AppLayout />
            </QueryClientProvider>
          </Provider>
        </AuthProvider>
      </NotificationProvider>
    </BrowserRouter>
  );
};

beforeEach(() => {
  // Set up auth tokens so the app starts authenticated
  localStorage.setItem('auth_token', 'test-token');
  localStorage.setItem('auth_user', 'test-user');
});

afterEach(() => {
  vi.restoreAllMocks();
  localStorage.clear();
});

describe('AppLayout', () => {
  it('renders navigation shell', () => {
    renderWithProviders();
    expect(screen.getByText('MedLink Clinics')).toBeInTheDocument();
    // Check that navigation menu items are present
    expect(screen.getByText('Dashboard')).toBeInTheDocument();
    expect(screen.getByText('Appointments')).toBeInTheDocument();
    expect(screen.getByText('Patients')).toBeInTheDocument();
  });
});
