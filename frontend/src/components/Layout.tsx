import { PropsWithChildren } from 'react'

export function Layout({ children }: PropsWithChildren) {
  return (
    <div className="min-h-screen bg-gray-50">
      <header className="bg-white shadow">
        <div className="max-w-3xl mx-auto px-4 py-6">
          <h1 className="text-3xl font-bold text-gray-900">RocksDB Explorer</h1>
        </div>
      </header>
      <main>
        <div className="max-w-3xl mx-auto py-6 px-4">
          {children}
        </div>
      </main>
    </div>
  )
} 