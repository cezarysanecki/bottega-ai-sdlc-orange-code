import { Card, Typography } from 'antd';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../shared/AuthContext';
import { LoginForm } from './components/LoginForm';
import { useEffect } from 'react';

const { Title } = Typography;

export const LoginPage = () => {
  const navigate = useNavigate();
  const { isAuthenticated } = useAuth();

  useEffect(() => {
    if (isAuthenticated) {
      navigate('/');
    }
  }, [isAuthenticated, navigate]);

  return (
    <div
      style={{
        display: 'flex',
        justifyContent: 'center',
        alignItems: 'center',
        minHeight: '100vh',
        backgroundColor: '#f0f2f5',
      }}
    >
      <Card style={{ width: 400, boxShadow: '0 2px 8px rgba(0,0,0,0.1)' }}>
        <div style={{ textAlign: 'center', marginBottom: 24 }}>
          <Title level={2}>MedLink Clinics</Title>
          <Title level={4} type="secondary">
            Sign In
          </Title>
        </div>
        <LoginForm />
      </Card>
    </div>
  );
};
