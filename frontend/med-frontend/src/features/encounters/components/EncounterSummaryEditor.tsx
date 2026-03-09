import { useState } from 'react';
import { Card, Input, Button, Space, message } from 'antd';
import { SaveOutlined } from '@ant-design/icons';
import { useUpdateSummary } from '../hooks/useUpdateSummary';
import type { Encounter } from '../../../shared/api/encounterService';

const { TextArea } = Input;

interface EncounterSummaryEditorProps {
  encounter: Encounter;
}

export const EncounterSummaryEditor = ({ encounter }: EncounterSummaryEditorProps) => {
  const [summary, setSummary] = useState(encounter.summary || '');
  const [isDirty, setIsDirty] = useState(false);
  const updateSummaryMutation = useUpdateSummary();

  const handleChange = (value: string) => {
    setSummary(value);
    setIsDirty(true);
  };

  const handleSave = async () => {
    // Validate summary length
    if (summary.length > 5000) {
      message.error('Summary must be 5000 characters or less');
      return;
    }

    try {
      await updateSummaryMutation.mutateAsync({
        encounterCode: encounter.encounterCode,
        summary: summary
      });
      message.success('Clinical summary updated successfully');
      setIsDirty(false);
    } catch (error: any) {
      message.error(error.response?.data?.message || 'Failed to update summary');
    }
  };

  return (
    <Card
      title="Clinical Summary"
      extra={
        <Button
          type="primary"
          icon={<SaveOutlined />}
          onClick={handleSave}
          loading={updateSummaryMutation.isPending}
          disabled={!isDirty}
        >
          Update Summary
        </Button>
      }
    >
      <Space direction="vertical" style={{ width: '100%' }} size="middle">
        <TextArea
          rows={10}
          value={summary}
          onChange={(e) => handleChange(e.target.value)}
          placeholder="Enter clinical notes, observations, diagnosis, and treatment plan..."
          showCount
          maxLength={5000}
        />
      </Space>
    </Card>
  );
};
