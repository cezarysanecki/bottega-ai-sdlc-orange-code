import { Card, Descriptions } from 'antd';

interface Patient {
  patientCode: string;
  firstName: string;
  lastName: string;
  dateOfBirth: string;
  insurancePlanCode?: string;
  insuranceProvider?: string;
}

interface PatientInfoCardProps {
  patient: Patient;
}

export const PatientInfoCard = ({ patient }: PatientInfoCardProps) => {
  return (
    <Card title="Patient Information">
      <Descriptions column={1} bordered>
        <Descriptions.Item label="Patient Code">{patient.patientCode}</Descriptions.Item>
        <Descriptions.Item label="First Name">{patient.firstName}</Descriptions.Item>
        <Descriptions.Item label="Last Name">{patient.lastName}</Descriptions.Item>
        <Descriptions.Item label="Date of Birth">{patient.dateOfBirth}</Descriptions.Item>
        <Descriptions.Item label="Insurance Provider">
          {patient.insuranceProvider || 'None'}
        </Descriptions.Item>
        <Descriptions.Item label="Insurance Plan Code">
          {patient.insurancePlanCode || 'None'}
        </Descriptions.Item>
      </Descriptions>
    </Card>
  );
};
