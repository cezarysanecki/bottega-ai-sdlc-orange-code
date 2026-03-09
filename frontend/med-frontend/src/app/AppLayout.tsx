import { Layout, Menu, Typography, theme, Button, Space } from 'antd';
import { useLocation, useNavigate } from 'react-router-dom';
import { LogoutOutlined } from '@ant-design/icons';
import { AutoLogin } from '../shared/AutoLogin';
import { AppRoutes } from './routes';
import { NotificationBell } from '../features/notifications/NotificationBell';
import { useAuth } from '../shared/AuthContext';

const { Header, Content, Sider } = Layout;

const NAV_ITEMS = [
  { key: '/', label: 'Dashboard' },
  { key: '/appointments', label: 'Appointments' },
  { key: '/encounters', label: 'Encounters' },
  { key: '/billing/invoices', label: 'Invoices' },
  { key: '/patients', label: 'Patients' },
  { key: '/providers', label: 'Providers' },
  { key: '/telehealth', label: 'Telehealth' },
  { key: '/legacy', label: 'Legacy Scheduler' }
];

export const AppLayout = () => {
  const location = useLocation();
  const navigate = useNavigate();
  const { logout, user } = useAuth();
  const {
    token: { colorBgContainer }
  } = theme.useToken();

  const currentPath = location.pathname;

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <AutoLogin>
      <Layout style={{ minHeight: '100vh' }}>
        <Header style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <Typography.Title style={{ color: 'white', margin: 0 }} level={3}>
            MedLink Clinics
          </Typography.Title>
          <Space>
            {user && (
              <Typography.Text style={{ color: 'white', marginRight: 16 }}>
                {user}
              </Typography.Text>
            )}
            <NotificationBell />
            <Button
              type="text"
              icon={<LogoutOutlined style={{ color: 'white' }} />}
              onClick={handleLogout}
            />
          </Space>
        </Header>
        <Layout>
          <Sider width={220} style={{ background: colorBgContainer }}>
            <Menu
              mode="inline"
              selectedKeys={[currentPath]}
              onSelect={(info) => navigate(info.key)}
              items={NAV_ITEMS}
            />
          </Sider>
          <Content style={{ padding: '24px', background: colorBgContainer }}>
            <AppRoutes />
          </Content>
        </Layout>
      </Layout>
    </AutoLogin>
  );
};
