import React from 'react';
import ReactDOM from 'react-dom/client';
import { BrowserRouter } from 'react-router-dom';
import { AppLayout } from './app/AppLayout';
import { ErrorBoundary } from './app/ErrorBoundary';
import { QueryClientProvider } from '@tanstack/react-query';
import { Provider } from 'react-redux';
import { legacyStore } from './shared/api/legacyStore';
import { AuthProvider } from './shared/AuthContext';
import { NotificationProvider } from './app/contexts/NotificationContext';
import { queryClient } from './shared/api/queryClient';

import 'antd/dist/reset.css';
import './shared/ui/global.css';

// MSW is disabled - using real backend API
// async function enableMocking() {
//   if (process.env.NODE_ENV !== 'development') {
//     return;
//   }
//   const { worker } = await import('./mocks/browser');
//   await worker.start({ onUnhandledRequest: 'bypass' });
// }
// enableMocking();

const root = ReactDOM.createRoot(document.getElementById('root') as HTMLElement);
root.render(
  <React.StrictMode>
    <ErrorBoundary>
      <BrowserRouter>
        <NotificationProvider>
          <AuthProvider>
            <Provider store={legacyStore}>
              <QueryClientProvider client={queryClient}>
                <AppLayout />
              </QueryClientProvider>
            </Provider>
          </AuthProvider>
        </NotificationProvider>
      </BrowserRouter>
    </ErrorBoundary>
  </React.StrictMode>
);
