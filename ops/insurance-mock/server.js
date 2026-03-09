const express = require('express');
const app = express();
app.use(express.json());

const plans = {
  'PAT-1001': {
    patientCode: 'PAT-1001',
    planCode: 'PPO-BETA',
    status: 'ACTIVE',
    copay: 20,
    deductibleRemaining: 450,
    coverageNotes: 'Telehealth covered with prior auth.'
  },
  'PAT-1002': {
    patientCode: 'PAT-1002',
    planCode: 'HMO-ALPHA',
    status: 'REVIEW',
    copay: 35,
    deductibleRemaining: 600,
    coverageNotes: 'Out-of-network specialist requires approval.'
  }
};

app.post('/eligibility', (req, res) => {
  const { patientCode } = req.body || {};
  if (!patientCode) {
    return res.status(400).json({ error: 'patientCode missing' });
  }

  if (patientCode.endsWith('9')) {
    return res.status(500).json({ error: 'Eligibility service unavailable' });
  }

  const payload = plans[patientCode] || {
    patientCode,
    planCode: 'UNKNOWN',
    status: 'UNKNOWN',
    copay: 0,
    deductibleRemaining: 0,
    coverageNotes: 'No record in mock service.'
  };

  res.json({ ...payload, fetchedAt: new Date().toISOString() });
});

app.listen(8081, () => {
  console.log('Insurance mock listening on 8081');
});
