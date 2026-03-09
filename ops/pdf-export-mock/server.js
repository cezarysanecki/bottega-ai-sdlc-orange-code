const express = require('express');
const app = express();
app.use(express.json());

app.post('/export', (req, res) => {
  const { invoiceNumber } = req.body || {};
  if (!invoiceNumber) {
    return res.status(400).json({ error: 'invoiceNumber required' });
  }
  const token = Buffer.from(`PDF-${invoiceNumber}-${Date.now()}`).toString('base64');
  res.json({ downloadUrl: `https://mock-pdf.example.com/${token}.pdf`, token });
});

app.listen(8082, () => {
  console.log('PDF export mock listening on 8082');
});
