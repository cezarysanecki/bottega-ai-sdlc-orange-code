import { useEffect, ReactNode } from 'react';
import { useAuth } from './AuthContext';
import { Spin } from 'antd';

interface AutoLoginProps {
  children: ReactNode;
}

export const AutoLogin = ({ children }: AutoLoginProps) => {
  const { isAuthenticated, login } = useAuth();

  useEffect(() => {
    const autoLogin = async () => {
      if (!isAuthenticated) {
        try {
          // Auto-login as demo-user for development
          const response = await fetch('http://localhost:8080/api/auth/token', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ username: 'demo-user', password: 'password' }),
          });

          if (response.ok) {
            const data = await response.json();
            login(data.token, data.user);
          } else {
            console.error('Auto-login failed:', response.status);
          }
        } catch (error) {
          console.error('Auto-login error:', error);
        }
      }
    };

    autoLogin();
  }, [isAuthenticated, login]);

  if (!isAuthenticated) {
    return (
      <Spin size="large" tip="Authenticating..." spinning={true}>
        <div style={{ height: '100vh' }} />
      </Spin>
    );
  }

  return <>{children}</>;
};
