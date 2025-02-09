import { useCallback, useState } from 'react'
import { useDropzone } from 'react-dropzone'
import axios from 'axios'
import clsx from 'clsx'
import { KeyValueTable } from './KeyValueTable'
import { ArrowUpTrayIcon } from '@heroicons/react/24/outline'

export function FileUpload() {
  const [data, setData] = useState<Record<string, string> | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [isLoading, setIsLoading] = useState(false)

  const onDrop = useCallback(async (acceptedFiles: File[]) => {
    const file = acceptedFiles[0]
    if (!file) return

    const formData = new FormData()
    formData.append('file', file)
    setError(null)
    setIsLoading(true)
    setData(null)

    try {
      const { data: responseData } = await axios.post('http://localhost:8080/api/upload', formData, {
        headers: {
          'Content-Type': 'multipart/form-data',
        },
      })
      setData(responseData)
    } catch (error) {
      console.error('Upload failed:', error)
      setError('Failed to process database')
    } finally {
      setIsLoading(false)
    }
  }, [])

  const { getRootProps, getInputProps, isDragActive } = useDropzone({
    onDrop,
    accept: {
      'application/zip': ['.zip'],
    },
    maxFiles: 1,
    disabled: isLoading,
  })

  return (
    <div>
      <div
        {...getRootProps()}
        className={clsx(
          'flex flex-col items-center justify-center p-8 border-2 border-dashed rounded-lg transition-colors',
          isDragActive ? 'border-blue-500 bg-blue-50' : 'border-gray-300 hover:border-gray-400',
          isLoading && 'opacity-50 cursor-not-allowed'
        )}
      >
        <input {...getInputProps()} />
        <ArrowUpTrayIcon className={clsx(
          'w-10 h-10 mb-4',
          isDragActive ? 'text-blue-500' : 'text-gray-400'
        )} />
        <p className="text-center text-gray-600 text-sm">
          {isLoading 
            ? 'Processing database...'
            : isDragActive
              ? 'Drop the RocksDB zip file here...'
              : 'Drag and drop a RocksDB zip file here, or click to select'}
        </p>
        <p className="mt-2 text-xs text-gray-500">Only .zip files are accepted</p>
      </div>

      {error && (
        <div className="mt-4 p-4 bg-red-50 border border-red-200 rounded-md">
          <p className="text-sm font-medium text-red-800">{error}</p>
        </div>
      )}

      {data && (
        <div className="mt-6">
          <div className="pb-5 border-b border-gray-200">
            <h3 className="text-lg font-medium leading-6 text-gray-900">Database Contents</h3>
          </div>
          <KeyValueTable data={data} />
        </div>
      )}
    </div>
  )
} 