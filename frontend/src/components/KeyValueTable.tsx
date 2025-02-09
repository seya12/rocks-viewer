import { useMemo, useState } from 'react'
import {
  createColumnHelper,
  flexRender,
  getCoreRowModel,
  getSortedRowModel,
  getFilteredRowModel,
  getPaginationRowModel,
  SortingState,
  useReactTable,
} from '@tanstack/react-table'
import { ChevronUpIcon, ChevronDownIcon, ClipboardIcon, MagnifyingGlassIcon, ChevronLeftIcon, ChevronRightIcon, ChevronDoubleLeftIcon, ChevronDoubleRightIcon } from '@heroicons/react/20/solid'
import { CheckIcon } from '@heroicons/react/24/outline'

type KeyValuePair = {
  key: string
  value: string
}

type KeyValueTableProps = {
  data: Record<string, string>
}

const columnHelper = createColumnHelper<KeyValuePair>()

export function KeyValueTable({ data }: KeyValueTableProps) {
  const [sorting, setSorting] = useState<SortingState>([])
  const [copiedValue, setCopiedValue] = useState<string | null>(null)
  const [globalFilter, setGlobalFilter] = useState('')

  const tableData = useMemo(() => 
    Object.entries(data).map(([key, value]) => ({ key, value }))
  , [data])

  const columns = useMemo(() => [
    columnHelper.accessor('key', {
      header: 'Key',
      cell: info => (
        <div className="max-w-md truncate" title={info.getValue()}>
          {info.getValue()}
        </div>
      ),
      size: 200,
    }),
    columnHelper.accessor('value', {
      header: 'Value',
      cell: info => {
        const value = info.getValue()
        return (
          <div className="flex items-center gap-2 group">
            <div className="flex-1 max-w-xl truncate" title={value}>
              {value}
            </div>
            <button
              onClick={() => {
                navigator.clipboard.writeText(value)
                setCopiedValue(value)
                setTimeout(() => setCopiedValue(null), 2000)
              }}
              className="invisible group-hover:visible p-1 hover:bg-gray-100 rounded"
              title="Copy value"
            >
              {copiedValue === value ? (
                <CheckIcon className="h-4 w-4 text-green-500" />
              ) : (
                <ClipboardIcon className="h-4 w-4 text-gray-400" />
              )}
            </button>
          </div>
        )
      },
    }),
  ], [copiedValue])

  const table = useReactTable({
    data: tableData,
    columns,
    state: {
      sorting,
      globalFilter,
    },
    onSortingChange: setSorting,
    onGlobalFilterChange: setGlobalFilter,
    getCoreRowModel: getCoreRowModel(),
    getSortedRowModel: getSortedRowModel(),
    getFilteredRowModel: getFilteredRowModel(),
    getPaginationRowModel: getPaginationRowModel(),
    initialState: {
      pagination: {
        pageSize: 25,
      },
    },
    globalFilterFn: (row, columnId, filterValue) => {
      const value = row.getValue(columnId) as string
      return value.toLowerCase().includes(filterValue.toLowerCase())
    },
  })

  return (
    <div className="mt-4 space-y-4">
      <div className="flex items-center justify-between">
        <div className="relative w-72">
          <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
            <MagnifyingGlassIcon className="h-5 w-5 text-gray-400" />
          </div>
          <input
            type="text"
            value={globalFilter}
            onChange={e => setGlobalFilter(e.target.value)}
            placeholder="Search keys and values..."
            className="block w-full pl-10 pr-3 py-2 border border-gray-300 rounded-md leading-5 bg-white placeholder-gray-500 focus:outline-none focus:ring-1 focus:ring-blue-500 focus:border-blue-500 sm:text-sm"
          />
        </div>
        <div className="flex items-center gap-2 text-sm text-gray-500">
          <span>
            {table.getFilteredRowModel().rows.length} entries
          </span>
          <select
            value={table.getState().pagination.pageSize}
            onChange={e => table.setPageSize(Number(e.target.value))}
            className="border border-gray-300 rounded px-2 py-1"
          >
            {[10, 25, 50, 100].map(pageSize => (
              <option key={pageSize} value={pageSize}>
                Show {pageSize}
              </option>
            ))}
          </select>
        </div>
      </div>

      <div className="overflow-x-auto rounded-lg border border-gray-200">
        <table className="w-full divide-y divide-gray-300">
          <thead className="bg-gray-50">
            {table.getHeaderGroups().map(headerGroup => (
              <tr key={headerGroup.id}>
                {headerGroup.headers.map(header => (
                  <th
                    key={header.id}
                    className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider cursor-pointer select-none hover:bg-gray-100"
                    onClick={header.column.getToggleSortingHandler()}
                  >
                    <div className="flex items-center gap-2">
                      {flexRender(header.column.columnDef.header, header.getContext())}
                      {{
                        asc: <ChevronUpIcon className="h-4 w-4" />,
                        desc: <ChevronDownIcon className="h-4 w-4" />,
                      }[header.column.getIsSorted() as string] ?? null}
                    </div>
                  </th>
                ))}
              </tr>
            ))}
          </thead>
          <tbody className="bg-white divide-y divide-gray-200">
            {table.getRowModel().rows.map(row => (
              <tr 
                key={row.id}
                className="hover:bg-gray-50"
              >
                {row.getVisibleCells().map(cell => (
                  <td key={cell.id} className="px-6 py-4 text-sm text-gray-900">
                    {flexRender(cell.column.columnDef.cell, cell.getContext())}
                  </td>
                ))}
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      <div className="flex items-center justify-between">
        <div className="flex items-center gap-2">
          <button
            onClick={() => table.setPageIndex(0)}
            disabled={!table.getCanPreviousPage()}
            className="p-1 border rounded disabled:opacity-50"
          >
            <ChevronDoubleLeftIcon className="h-4 w-4" />
          </button>
          <button
            onClick={() => table.previousPage()}
            disabled={!table.getCanPreviousPage()}
            className="p-1 border rounded disabled:opacity-50"
          >
            <ChevronLeftIcon className="h-4 w-4" />
          </button>
          <span className="text-sm text-gray-700">
            Page{' '}
            <strong>
              {table.getState().pagination.pageIndex + 1} of{' '}
              {table.getPageCount()}
            </strong>
          </span>
          <button
            onClick={() => table.nextPage()}
            disabled={!table.getCanNextPage()}
            className="p-1 border rounded disabled:opacity-50"
          >
            <ChevronRightIcon className="h-4 w-4" />
          </button>
          <button
            onClick={() => table.setPageIndex(table.getPageCount() - 1)}
            disabled={!table.getCanNextPage()}
            className="p-1 border rounded disabled:opacity-50"
          >
            <ChevronDoubleRightIcon className="h-4 w-4" />
          </button>
        </div>
        <div className="text-sm text-gray-500">
          Showing {table.getRowModel().rows.length} of{' '}
          {table.getFilteredRowModel().rows.length} results
        </div>
      </div>

      {table.getRowModel().rows.length === 0 && (
        <div className="text-center py-4 text-sm text-gray-500">
          No results found for "{globalFilter}"
        </div>
      )}
    </div>
  )
} 