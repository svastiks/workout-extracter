# Workout Extracter (in-progress/not-deployed yet)

A full-stack application that extracts workout routines from YouTube fitness videos using AI.

## Project Flow (diagram in progress)

<img width="611" height="364" alt="image" src="https://github.com/user-attachments/assets/eb341061-2bea-413f-bcda-5a1c74cf6dbd" />

## Architecture

- **Frontend**: Next.js with TypeScript and Tailwind CSS
- **Backend**: Spring Boot with Java
- **Database**: PostgreSQL
- **AI**: Google Gemini API for workout extraction
- **Video Processing**: yt-dlp for YouTube data extraction

## API Endpoints

- POST /workouts/extract

- GET /workouts/extract/status/{jobId}
- GET /workouts/{youtubeVideoId}

- GET /creators
- GET /creators/{id}
- GET /creators/{creatorId}/videos

## Quick Start

### Prerequisites
- Java 17+
- Node.js 18+
- PostgreSQL
- Google API Key

### Backend Setup
1. Navigate to the server directory:
   ```bash
   cd server
   ```

2. Create a `.env` file with your configuration:
   ```bash
   GOOGLE_API_KEY=your_google_api_key_here
   DATABASE_URL=jdbc:postgresql://localhost:5433/workout_extract_db
   DB_USER=postgres
   DB_PASSWORD=postgres
   ```

3. Start the backend:
   ```bash
   mvn spring-boot:run
   ```

### Frontend Setup
1. Navigate to the client directory:
   ```bash
   cd client
   ```

2. Install dependencies:
   ```bash
   npm install
   ```

3. Start the frontend:
   ```bash
   npm run dev
   ```

## Features

- **YouTube Video Processing**: Extract metadata, transcripts, and comments
- **AI-Powered Extraction**: Use LLM to parse workout routines
- **Structured Data**: Convert videos into JSON workout plans
- **Real-time Processing**: Async job processing with progress tracking
- **Modern UI**: Clean, responsive interface built with Next.js

## Development

See individual README files in `server/` and `client/` directories for detailed setup instructions.
