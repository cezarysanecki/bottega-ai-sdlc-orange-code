import { useEffect, useState } from 'react';
import { Card, List, Typography, Select } from 'antd';
import { useNavigate } from 'react-router-dom';
import { fetchJson } from '../../shared/api/fetchClient';

interface ProviderSummary {
  providerCode: string;
  fullName: string;
  specialty: string;
  officeRoom?: string;
  preferredContact?: string;
  npi?: string;
}

const specialties = ['Internal Medicine', 'Family Medicine'];

export const ProviderList = () => {
  const navigate = useNavigate();
  const [data, setData] = useState<ProviderSummary[]>([]);
  const [selectedSpecialty, setSelectedSpecialty] = useState<string | undefined>(undefined);

  useEffect(() => {
    const params = selectedSpecialty ? `?specialty=${encodeURIComponent(selectedSpecialty)}` : '';
    fetchJson<ProviderSummary[]>(`/api/providers${params}`)
      .then(setData)
      .catch(() => setData([]));
  }, [selectedSpecialty]);

  return (
    <Card
      title="Provider Directory"
      extra={
        <Select
          placeholder="Filter by specialty"
          allowClear
          style={{ width: 220 }}
          onChange={(value) => setSelectedSpecialty(value)}
          options={specialties.map((spec) => ({ label: spec, value: spec }))}
        />
      }
    >
      <List
        dataSource={data}
        renderItem={(provider) => (
          <List.Item
            onClick={() => navigate(`/providers/${provider.providerCode}`)}
            style={{ cursor: 'pointer' }}
          >
            <List.Item.Meta
              title={provider.fullName}
              description={
                <>
                  <Typography.Text>{provider.specialty}</Typography.Text>
                  {provider.officeRoom && <div>Room: {provider.officeRoom}</div>}
                  {provider.preferredContact && <div>Contact: {provider.preferredContact}</div>}
                  {provider.npi && <div>NPI: {provider.npi}</div>}
                </>
              }
            />
          </List.Item>
        )}
      />
    </Card>
  );
};
