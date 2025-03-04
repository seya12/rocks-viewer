# RocksDB Explorer

## Overview
RocksDB Explorer is a modern web-based tool designed to explore and analyze RocksDB database files. It provides an intuitive user interface for viewing and querying RocksDB instances, making it easy to inspect and manage database content. The application is built with a React frontend and Spring Boot backend, offering a seamless experience for database exploration.

## Features
- 📁 Drag-and-drop interface for uploading RocksDB database files (ZIP format)
- 🔍 Advanced search functionality for both keys and values
- 📊 Interactive data table with:
  - Sortable columns
  - Client-side pagination (25 entries per page)
  - Copy to clipboard functionality
  - Truncated value display with tooltips
- ⚡ High performance handling of large databases (tested with 100k+ entries)
- 🛡️ Robust error handling and validation
- 📱 Responsive design for various screen sizes

## Tech Stack
- Frontend:
  - React with TypeScript
  - Vite build tool
  - TailwindCSS for styling
  - React Query for data fetching
  - React Table for data display
- Backend:
  - Spring Boot 3.2.3
  - RocksDB for database operations
  - Gradle with Kotlin DSL

## Prerequisites
- Java 11 or higher
- Node.js and npm
- Gradle
- RocksDB native libraries

## Installation and Setup

### Backend Setup
1. Navigate to the backend directory:
   ```bash
   cd backend
   ```
2. Build the project using Gradle:
   ```bash
   ./gradlew build
   ```

### Frontend Setup
1. Navigate to the frontend directory:
   ```bash
   cd frontend
   ```
2. Install dependencies:
   ```bash
   npm install
   ```
3. Start the development server:
   ```bash
   npm run dev
   ```

## Development

### Running the Application
1. Start the backend server:
   ```bash
   cd backend
   ./gradlew bootRun
   ```
2. In a separate terminal, start the frontend development server:
   ```bash
   cd frontend
   npm run dev
   ```
3. Access the application at `http://localhost:5173`

## Usage
1. Launch the application in your web browser
2. Use the drag-and-drop zone to upload a zipped RocksDB database file
3. Once uploaded, the database content will be displayed in a paginated table
4. Use the search functionality to filter keys or values
5. Click on column headers to sort the data
6. Hover over truncated values to see the full content
7. Use the copy button to copy values to clipboard

## Performance Considerations
- The application is optimized to handle RocksDB files up to 50MB (configurable)
- Response time is optimized to be under 2 seconds for listing operations
- Client-side pagination ensures smooth browsing of large datasets
- Temporary files are automatically cleaned up after processing

## Creating Test Files
To create test RocksDB files for development and testing, use the provided `TestDatabaseCreator utility:

This will create three sample databases with realistic test data:
- `test-rocksdb-small.zip` (5 entries)
- `test-rocksdb-medium.zip` (~10,000 entries, ~2MB)
- `test-rocksdb-large.zip` (~100,000 entries, ~20MB)

The test data includes a mix of:
- User records (20% of entries)
- Metrics data (40% of entries)
- Log entries (40% of entries)

Each type contains realistic JSON data with various fields and nested structures, making them ideal for testing different aspects of the application.

## Security Features
- Input validation for uploaded files
- Automatic cleanup of temporary files
- Protection against malicious uploads

## Contributing
Contributions are welcome! Please feel free to submit pull requests.

## License
This project is licensed under the MIT License - see the LICENSE file for details.