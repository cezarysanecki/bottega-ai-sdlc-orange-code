import { Result, Button } from 'antd';

interface ErrorFallbackProps {
  error: Error | null;
}

export const ErrorFallback = ({ error }: ErrorFallbackProps) => {
  const handleReload = () => {
    window.location.href = '/';
  };

  return (
    <div
      style={{
        display: 'flex',
        justifyContent: 'center',
        alignItems: 'center',
        minHeight: '100vh',
        padding: '20px',
      }}
    >
      <Result
        status="500"
        title="Something went wrong"
        subTitle={error?.message || 'An unexpected error occurred'}
        extra={
          <Button type="primary" onClick={handleReload}>
            Go Home
          </Button>
        }
      />
    </div>
  );
};
