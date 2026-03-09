import { lazy, Suspense } from 'react';
import { Routes, Route } from 'react-router-dom';
import { ProtectedRoute } from './ProtectedRoute';
import { LoadingSkeleton } from '../shared/components/LoadingSkeleton';

// Eager load critical routes
import { LoginPage } from '../features/auth/LoginPage';
import { DashboardPage } from '../features/dashboard/DashboardPage';

// Lazy load feature routes for code splitting
const AppointmentListPage = lazy(() => import('../features/appointments/AppointmentListPage').then(m => ({ default: m.AppointmentListPage })));
const AppointmentDetailPage = lazy(() => import('../features/appointments/AppointmentDetailPage').then(m => ({ default: m.AppointmentDetailPage })));
const BookAppointmentPage = lazy(() => import('../features/appointments/BookAppointmentPage').then(m => ({ default: m.BookAppointmentPage })));
const BillingSummary = lazy(() => import('../features/billing/BillingSummary').then(m => ({ default: m.BillingSummary })));
const InvoiceListPage = lazy(() => import('../features/billing/InvoiceListPage').then(m => ({ default: m.InvoiceListPage })));
const InvoiceDetailPage = lazy(() => import('../features/billing/InvoiceDetailPage').then(m => ({ default: m.InvoiceDetailPage })));
const PatientListPage = lazy(() => import('../features/patients/PatientListPage').then(m => ({ default: m.PatientListPage })));
const PatientDetailPage = lazy(() => import('../features/patients/PatientDetailPage').then(m => ({ default: m.PatientDetailPage })));
const PatientRegistrationPage = lazy(() => import('../features/patients/PatientRegistrationPage').then(m => ({ default: m.PatientRegistrationPage })));
const ProviderList = lazy(() => import('../features/providers/ProviderList').then(m => ({ default: m.ProviderList })));
const ProviderDetailPage = lazy(() => import('../features/providers/ProviderDetailPage').then(m => ({ default: m.ProviderDetailPage })));
const TelehealthListPage = lazy(() => import('../features/telehealth/TelehealthListPage').then(m => ({ default: m.TelehealthListPage })));
const TelehealthDetailPage = lazy(() => import('../features/telehealth/TelehealthDetailPage').then(m => ({ default: m.TelehealthDetailPage })));
const EncounterListPage = lazy(() => import('../features/encounters/EncounterListPage').then(m => ({ default: m.EncounterListPage })));
const EncounterDetailPage = lazy(() => import('../features/encounters/EncounterDetailPage').then(m => ({ default: m.EncounterDetailPage })));
const LegacySchedulerPanel = lazy(() => import('../legacy/LegacySchedulerPanel').then(m => ({ default: m.LegacySchedulerPanel })));

export const AppRoutes = () => {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route
        path="/"
        element={
          <ProtectedRoute>
            <DashboardPage />
          </ProtectedRoute>
        }
      />
      <Route
        path="/appointments"
        element={
          <ProtectedRoute>
            <Suspense fallback={<LoadingSkeleton />}>
              <AppointmentListPage />
            </Suspense>
          </ProtectedRoute>
        }
      />
      <Route
        path="/appointments/new"
        element={
          <ProtectedRoute>
            <BookAppointmentPage />
          </ProtectedRoute>
        }
      />
      <Route
        path="/appointments/:code"
        element={
          <ProtectedRoute>
            <Suspense fallback={<LoadingSkeleton />}>
              <AppointmentDetailPage />
            </Suspense>
          </ProtectedRoute>
        }
      />
      <Route
        path="/billing"
        element={
          <ProtectedRoute>
            <BillingSummary />
          </ProtectedRoute>
        }
      />
      <Route
        path="/billing/invoices"
        element={
          <ProtectedRoute>
            <InvoiceListPage />
          </ProtectedRoute>
        }
      />
      <Route
        path="/billing/invoices/:invoiceNumber"
        element={
          <ProtectedRoute>
            <InvoiceDetailPage />
          </ProtectedRoute>
        }
      />
      <Route
        path="/patients"
        element={
          <ProtectedRoute>
            <PatientListPage />
          </ProtectedRoute>
        }
      />
      <Route
        path="/patients/register"
        element={
          <ProtectedRoute>
            <Suspense fallback={<LoadingSkeleton />}>
              <PatientRegistrationPage />
            </Suspense>
          </ProtectedRoute>
        }
      />
      <Route
        path="/patients/:code"
        element={
          <ProtectedRoute>
            <PatientDetailPage />
          </ProtectedRoute>
        }
      />
      <Route
        path="/providers"
        element={
          <ProtectedRoute>
            <ProviderList />
          </ProtectedRoute>
        }
      />
      <Route
        path="/providers/:code"
        element={
          <ProtectedRoute>
            <ProviderDetailPage />
          </ProtectedRoute>
        }
      />
      <Route
        path="/telehealth"
        element={
          <ProtectedRoute>
            <TelehealthListPage />
          </ProtectedRoute>
        }
      />
      <Route
        path="/telehealth/:vendorSessionId"
        element={
          <ProtectedRoute>
            <TelehealthDetailPage />
          </ProtectedRoute>
        }
      />
      <Route
        path="/encounters"
        element={
          <ProtectedRoute>
            <Suspense fallback={<LoadingSkeleton />}>
              <EncounterListPage />
            </Suspense>
          </ProtectedRoute>
        }
      />
      <Route
        path="/encounters/:encounterCode"
        element={
          <ProtectedRoute>
            <Suspense fallback={<LoadingSkeleton />}>
              <EncounterDetailPage />
            </Suspense>
          </ProtectedRoute>
        }
      />
      <Route
        path="/legacy"
        element={
          <ProtectedRoute>
            <LegacySchedulerPanel />
          </ProtectedRoute>
        }
      />
    </Routes>
  );
};
