import { http, HttpResponse } from 'msw';

export const handlers = [
  http.get('/api/providers', () => {
    return HttpResponse.json([
      {
        providerCode: 'MOCK-001',
        fullName: 'Dr. Mock Example',
        specialty: 'Internal Medicine',
        officeRoom: 'M100',
        preferredContact: 'mock@example.com',
        npi: '0000000000'
      }
    ]);
  })
];
