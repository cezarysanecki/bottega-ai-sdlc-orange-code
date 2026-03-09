import { Card } from 'antd';

const LEGACY_PATH = '/legacy/scheduler.html';

export const LegacySchedulerPanel = () => {
  return (
    <Card title="Legacy Scheduler">
      <iframe
        title="legacy-scheduler"
        src={LEGACY_PATH}
        style={{ width: '100%', minHeight: '480px', border: '1px solid #ccc' }}
      />
    </Card>
  );
};
