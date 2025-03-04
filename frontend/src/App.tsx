import { Layout } from './components/Layout'
import { FileUpload } from './components/FileUpload'

function App() {
  return (
    <Layout>
      <div className="bg-white shadow sm:rounded-lg">
        <div className="px-4 py-5 sm:p-6">
          <FileUpload />
        </div>
      </div>
    </Layout>
  )
}

export default App
