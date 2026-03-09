const express = require('express');
const app = express();
app.use(express.json());
app.use(express.text({ type: ['text/xml', 'application/xml', 'application/soap+xml'] }));

app.post('/api/session/status', (req, res) => {
  const { sessionId } = req.body || {};
  if (!sessionId) {
    return res.status(400).json({ status: 'ERROR', message: 'Missing sessionId' });
  }
  if (sessionId.endsWith('3')) {
    return res.json({ status: 'FAILED', reason: 'Mock handshake failure' });
  }
  res.json({ status: 'READY', sessionId });
});

app.post('/soap/session', (req, res) => {
  const body = req.body || '';
  const match = body.match(/<sessionId>([^<]+)<\/sessionId>/i);
  const sessionId = match ? match[1] : null;
  if (!sessionId) {
    return res.status(400).type('text/xml').send(`<?xml version="1.0" encoding="UTF-8"?>
<Envelope><Body><SessionResponse><status>ERROR</status><message>sessionId missing</message></SessionResponse></Body></Envelope>`);
  }

  const status = sessionId.endsWith('3') ? 'FAILED' : 'READY';
  const failureReason = status === 'FAILED' ? 'handshake-timeout' : '';

  const responseXml = `<?xml version="1.0" encoding="UTF-8"?>
<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:th="http://telehealth.example.com/schema">
  <soapenv:Body>
    <th:SessionStatusResponse>
      <th:sessionId>${sessionId}</th:sessionId>
      <th:sessionStatus>${status}</th:sessionStatus>
      <th:failureReason>${failureReason}</th:failureReason>
    </th:SessionStatusResponse>
  </soapenv:Body>
</soapenv:Envelope>`;

  res.type('text/xml').send(responseXml);
});

app.get('/health', (_req, res) => {
  res.json({ status: 'ok', timestamp: Date.now() });
});

app.listen(8080, () => {
  console.log('Telehealth mock listening on 8080');
});
