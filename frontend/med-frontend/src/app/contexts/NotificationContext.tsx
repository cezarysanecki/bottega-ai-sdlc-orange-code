import { createContext, useContext, ReactNode, useCallback } from 'react';
import { message } from 'antd';

export type NotificationType = 'success' | 'error' | 'info' | 'warning';

interface NotificationContextType {
  showNotification: (type: NotificationType, content: string, duration?: number) => void;
}

const NotificationContext = createContext<NotificationContextType | undefined>(undefined);

export const useNotification = () => {
  const context = useContext(NotificationContext);
  if (!context) {
    throw new Error('useNotification must be used within NotificationProvider');
  }
  return context;
};

interface NotificationProviderProps {
  children: ReactNode;
}

export const NotificationProvider = ({ children }: NotificationProviderProps) => {
  const [messageApi, contextHolder] = message.useMessage();

  const showNotification = useCallback(
    (type: NotificationType, content: string, duration: number = 3) => {
      messageApi[type](content, duration);
    },
    [messageApi]
  );

  return (
    <NotificationContext.Provider value={{ showNotification }}>
      {contextHolder}
      {children}
    </NotificationContext.Provider>
  );
};
