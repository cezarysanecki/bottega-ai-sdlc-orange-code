import { Card, Table } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import type { EncounterProcedure } from '../../../shared/api/encounterService';

interface EncounterProceduresTableProps {
  procedures: EncounterProcedure[];
}

export const EncounterProceduresTable = ({ procedures }: EncounterProceduresTableProps) => {
  const columns: ColumnsType<EncounterProcedure> = [
    {
      title: 'Procedure Code',
      dataIndex: 'procedureCode',
      key: 'procedureCode',
      width: 150,
    },
    {
      title: 'Description',
      dataIndex: 'description',
      key: 'description',
    },
    {
      title: 'Quantity',
      dataIndex: 'quantity',
      key: 'quantity',
      width: 100,
      align: 'center',
    },
  ];

  return (
    <Card title="Procedures Performed">
      <Table
        columns={columns}
        dataSource={procedures}
        rowKey="procedureCode"
        pagination={false}
        size="small"
      />
    </Card>
  );
};
