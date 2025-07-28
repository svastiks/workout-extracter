# Workout Extracter Server

## Environment Setup

### Required Environment Variables

The application uses a `.env` file for environment variables. Create a `.env` file in the server directory:

```bash
# server/.env
GOOGLE_API_KEY=your_google_api_key_here
DATABASE_URL=jdbc:postgresql://localhost:5433/workout_extract_db
DB_USER=postgres
DB_PASSWORD=postgres
```

### Configuration

1. **Create the .env file** (see above)
2. **Update application.properties** if needed for your database settings
3. **Install dependencies:**
   ```bash
   mvn install
   ```

### Running the Application

```bash
mvn spring-boot:run
```

## API Key Setup

The application automatically reads the `GOOGLE_API_KEY` from your `.env` file.