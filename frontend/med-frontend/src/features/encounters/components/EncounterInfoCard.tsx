import { Card, Descriptions } from 'antd';
import type { Encounter } from '../../../shared/api/encounterService';

interface EncounterInfoCardProps {
  encounter: Encounter;
}

export const EncounterInfoCard = ({ encounter }: EncounterInfoCardProps) => {
  return (
    <Card title="Encounter Information">
      <Descriptions column={2} bordered>
        <Descriptions.Item label="Encounter Code">
          {encounter.encounterCode}
        </Descriptions.Item>
        <Descriptions.Item label="Appointment Code">
          {encounter.appointmentCode}
        </Descriptions.Item>
        <Descriptions.Item label="Patient">
          {encounter.patient ? `${encounter.patient.firstName} ${encounter.patient.lastName}` : '-'}
        </Descriptions.Item>
        <Descriptions.Item label="Patient Code">
          {encounter.patient?.patientCode || '-'}
        </Descriptions.Item>
        <Descriptions.Item label="Provider">
          {encounter.provider?.fullName || '-'}
        </Descriptions.Item>
        <Descriptions.Item label="Specialty">
          {encounter.provider?.specialty || '-'}
        </Descriptions.Item>
        <Descriptions.Item label="Encounter Date & Time">
          {encounter.encounterTime ? new Date(encounter.encounterTime).toLocaleString() : '-'}
        </Descriptions.Item>
        <Descriptions.Item label="Reason for Visit">
          {encounter.reason || '-'}
        </Descriptions.Item>
      </Descriptions>
    </Card>
  );
};
