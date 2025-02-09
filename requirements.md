# RocksDB Explorer Requirements

## Overview
A web-based tool for exploring and analyzing RocksDB database files with a React frontend and Spring Boot backend.

## Technical Stack
- Frontend: React
- Backend: Spring Boot
- Database Viewer: RocksDB
- Build Tools: Gradle (Kotlin DSL), npm
- Packaging: Single deployable JAR containing both frontend and backend

## Iterations

### Iteration 1: Basic Setup and File Upload
- [x] Project structure setup
- [x] Spring Boot backend setup with RocksDB dependency
- [x] React frontend setup
- [x] Basic UI with drag-and-drop zone for zip files
- [x] File upload endpoint in backend
- [x] Basic error handling for file uploads

### Iteration 2: RocksDB Integration
- [x] Unzip uploaded database files
- [x] RocksDB connection and reading functionality
- [x] API endpoint to fetch all key-value pairs
- [x] Display key-value pairs in frontend table/list
- [x] Sortable columns
- [x] Loading states while processing
- [x] Better error messages for invalid databases

### Iteration 3: Enhanced Features
- [x] Search/filter functionality
  - [x] Search functionality for keys
  - [x] Search functionality for values
- [ ] Export functionality (CSV/JSON)
- [x] Better error handling and validation
- [x] Loading states and progress indicators

### Iteration 4: Polish and Optimization
- [x] Improved UI/UX design
  - [x] Truncated long values with tooltips
  - [x] Copy to clipboard functionality
  - [x] Row hover effects
  - [x] Search with icon
- [x] Performance optimization for large databases
  - [x] Client-side pagination (25 entries/page)
  - [x] Tested with 100k entries (~3.5MB zipped)
  - [x] Smooth UI with search/sort
- [x] Cleanup of temporary files
- [ ] Unit tests
- [ ] Integration tests

## Non-functional Requirements
1. Performance
   - Handle RocksDB files up to 50MB (configurable)
   - Response time < 2 seconds for listing operations
   - Smooth pagination through large datasets

2. Security
   - Validate uploaded files
   - Clean up temporary files
   - Protect against malicious uploads

3. Usability
   - Intuitive drag-and-drop interface
   - Clear error messages
   - Responsive design 