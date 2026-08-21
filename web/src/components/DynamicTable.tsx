'use client';

import { Loader2 } from 'lucide-react';

export type TableColumn<T extends Record<string, unknown> = Record<string, unknown>> = {
  key: string;
  label: string;
  className?: string;
  textAlign?: 'left' | 'right' | 'center';
  render?: (value: unknown, row: T) => React.ReactNode;
  sortable?: boolean;
};

export type TableRowAction<T extends Record<string, unknown> = Record<string, unknown>> = {
  label?: string;
  icon: React.ReactNode;
  onClick: (row: T) => void;
  className?: string;
  title?: string;
};

interface DynamicTableProps<T extends Record<string, unknown>> {
  columns: TableColumn<T>[];
  rows: T[];
  isLoading: boolean;
  emptyMessage: string;
  rowKey?: string;
  rowActions?: TableRowAction<T>[];
  onRowClick?: (row: T) => void;
  sortField?: string;
  sortDirection?: 'asc' | 'desc';
  onSort?: (field: string) => void;
}

export default function DynamicTable<T extends Record<string, unknown>>({
  columns,
  rows,
  isLoading,
  emptyMessage,
  rowKey = 'id',
  rowActions,
  onRowClick,
  sortField,
  sortDirection,
  onSort
}: DynamicTableProps<T>) {
  const getTextAlignClass = (align?: 'left' | 'right' | 'center') => {
    switch (align) {
      case 'right':
        return 'text-right';
      case 'center':
        return 'text-center';
      default:
        return 'text-left';
    }
  };

  return (
    <div className="overflow-x-auto">
      <table className="w-full text-left text-sm text-gray-600 dark:text-gray-300">
        <thead className="bg-gray-50/50 dark:bg-gray-900/50 text-gray-500 dark:text-gray-400 text-xs uppercase tracking-wider border-b border-gray-100 dark:border-gray-800">
          <tr>
            {columns.map((column, index) => (
              <th
                key={`${column.key}-${index}`}
                className={`px-6 py-4 font-medium ${getTextAlignClass(column.textAlign)} ${column.className || ''}`}
              >
                {column.sortable && onSort ? (
                  <button
                    type="button"
                    onClick={() => onSort(column.key)}
                    className="inline-flex items-center gap-2 text-left"
                  >
                    <span>{column.label}</span>
                    <span className="text-xs text-gray-400">
                      {sortField === column.key ? (sortDirection === 'asc' ? '▲' : '▼') : '↕'}
                    </span>
                  </button>
                ) : (
                  column.label
                )}
              </th>
            ))}
            {rowActions && rowActions.length > 0 && (
              <th className="px-6 py-4 font-medium text-right">Actions</th>
            )}
          </tr>
        </thead>
        <tbody className="divide-y divide-gray-50 dark:divide-gray-800">
          {isLoading ? (
            <tr>
              <td colSpan={columns.length + (rowActions?.length ? 1 : 0)} className="px-6 py-12 text-center">
                <div className="flex items-center justify-center space-x-2 text-gray-400 dark:text-gray-500">
                  <Loader2 size={20} className="animate-spin" />
                  <span className="text-sm">Loading...</span>
                </div>
              </td>
            </tr>
          ) : rows.length === 0 ? (
            <tr>
              <td colSpan={columns.length + (rowActions?.length ? 1 : 0)} className="px-6 py-8 text-center text-gray-500 dark:text-gray-400">
                {emptyMessage}
              </td>
            </tr>
          ) : (
            rows.map((row) => (
              <tr
                key={String(row[rowKey])}
                className="hover:bg-gray-50/50 dark:hover:bg-gray-800/50 transition-colors cursor-pointer"
                onClick={() => onRowClick?.(row)}
              >
                {columns.map((column, index) => (
                  <td
                    key={`${row[rowKey]}-${column.key}-${index}`}
                    className={`px-6 py-4 ${getTextAlignClass(column.textAlign)} ${column.className || ''}`}
                  >
                    {column.render
                      ? column.render(row[column.key], row)
                      : String(row[column.key])}
                  </td>
                ))}
                {rowActions && rowActions.length > 0 && (
                  <td className="px-6 py-4 text-right space-x-3 flex justify-end">
                    {rowActions.map((action, idx) => (
                      <button
                        key={idx}
                        onClick={(e) => {
                          e.stopPropagation();
                          action.onClick(row);
                        }}
                        className={action.className || 'text-gray-600 hover:text-gray-900 dark:text-gray-400 dark:hover:text-gray-200 transition-colors'}
                        title={action.title || action.label}
                      >
                        {action.icon}
                      </button>
                    ))}
                  </td>
                )}
              </tr>
            ))
          )}
        </tbody>
      </table>
    </div>
  );
}
