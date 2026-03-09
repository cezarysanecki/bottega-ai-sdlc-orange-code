export const queryKeys = {
  appointments: {
    all: ['appointments'] as const,
    list: (page?: number, size?: number, status?: string) => [...queryKeys.appointments.all, 'list', { page, size, status }] as const,
    detail: (id: string) => [...queryKeys.appointments.all, 'detail', id] as const,
    digest: () => [...queryKeys.appointments.all, 'digest'] as const,
  },
  patients: {
    all: ['patients'] as const,
    list: () => [...queryKeys.patients.all, 'list'] as const,
    detail: (code: string) => [...queryKeys.patients.all, 'detail', code] as const,
  },
  providers: {
    all: ['providers'] as const,
    list: () => [...queryKeys.providers.all, 'list'] as const,
    detail: (code: string) => [...queryKeys.providers.all, 'detail', code] as const,
  },
  invoices: {
    all: ['invoices'] as const,
    list: (status?: string) => [...queryKeys.invoices.all, 'list', { status }] as const,
    detail: (invoiceNumber: string) => [...queryKeys.invoices.all, 'detail', invoiceNumber] as const,
    outstanding: () => [...queryKeys.invoices.all, 'outstanding'] as const,
  },
  telehealth: {
    all: ['telehealth'] as const,
    sessions: () => [...queryKeys.telehealth.all, 'sessions'] as const,
    session: (vendorSessionId: string) => [...queryKeys.telehealth.all, 'session', vendorSessionId] as const,
  },
  dashboard: {
    stats: () => ['dashboard', 'stats'] as const,
  },
  insurance: {
    eligibility: (patientCode: string) => ['insurance', 'eligibility', patientCode] as const,
  },
  encounters: {
    all: ['encounters'] as const,
    list: (params: { page?: number; size?: number }) => [...queryKeys.encounters.all, 'list', params] as const,
    detail: (encounterCode: string) => [...queryKeys.encounters.all, 'detail', encounterCode] as const,
  },
  procedures: {
    all: ['procedures'] as const,
    list: () => [...queryKeys.procedures.all, 'list'] as const,
  },
} as const;
