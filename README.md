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

## Design

<img width="456" height="248" alt="image" src="https://github.com/user-attachments/assets/0026840c-bbe1-4a2a-8df0-0a188641aa8f" />

<img width="452" height="246" alt="image" src="https://github.com/user-attachments/assets/bb228396-af01-4309-9373-60cdcc91ddc7" />

<img width="453" height="246" alt="image" src="https://github.com/user-attachments/assets/9c57e87d-9fa1-49d8-b9b5-5821c852a64a" />

<img width="453" height="245" alt="image" src="https://github.com/user-attachments/assets/dfe529f4-0b86-479a-bde6-c58453cc527b" />

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
