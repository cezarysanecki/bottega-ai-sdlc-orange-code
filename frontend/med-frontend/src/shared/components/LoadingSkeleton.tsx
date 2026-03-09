import { Skeleton, Card } from 'antd';

interface LoadingSkeletonProps {
  rows?: number;
  avatar?: boolean;
  title?: boolean;
}

export const LoadingSkeleton = ({ rows = 3, avatar = false, title = true }: LoadingSkeletonProps) => {
  return (
    <Card>
      <Skeleton
        active
        avatar={avatar}
        title={title}
        paragraph={{ rows }}
      />
    </Card>
  );
};
