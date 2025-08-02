// client/src/services/api.ts

export interface ExtractionJob {
  id: string;
  youtubeVideoId: string;
  status: string;
  progress: number;
  resultVideoId?: number;
  resultYoutubeVideoId?: string;
  errorMessage?: string;
}

export interface Workout {
  id: number;
  youtubeVideoId: string;
  title: string;
  thumbnailUrl: string;
  workoutData: {
    equipment: string[];
    exercises: Array<{
      name: string;
      reps: string | null;
      rest: string | null;
      sets: string | null;
      emoji: string;
      notes: string;
      difficulty: string;
      reps_transparency?: string;
      sets_transparency?: string;
      rest_transparency?: string;
    }>;
    workoutType: string | null;
    targetMuscles: string[];
    llmAdjusted?: boolean;
    adjustmentReason?: string;
  };
  creator: {
    id: number;
    name: string;
    youtubeChannelId: string;
    profileImageUrl: string;
  };
  // Optional properties that might be added later
  duration?: string;
  difficulty?: string;
  totalTime?: string;
}

// --- Creators API ---

export interface Creator {
  id: number;
  name: string;
  youtubeChannelId: string;
  profileImageUrl: string;
  videoCount?: number; // Add video count property
  // Add other fields as needed (subscribers, specialty, etc.)
}

const API_BASE_URL = process.env.NEXT_PUBLIC_API_BASE_URL;

export async function initiateExtraction(url: string) {
  const res = await fetch(`${API_BASE_URL}/workouts/extract`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ url }),
  });
  if (!res.ok) throw new Error("Extraction failed");
  return res.json(); // { jobId }
}

export async function getExtractionStatus(jobId: string): Promise<ExtractionJob> {
  const res = await fetch(`${API_BASE_URL}/workouts/extract/status/${jobId}`);
  if (!res.ok) throw new Error("Failed to get extraction status");
  const data = await res.json();
  console.log("[API] getExtractionStatus response:", data);
  return data;
};



export async function getWorkoutByYoutubeId(youtubeVideoId: string): Promise<Workout> {
  const res = await fetch(`${API_BASE_URL}/workouts/${youtubeVideoId}`);
  if (!res.ok) throw new Error("Workout not found");
  return res.json();
};

export async function getCreators(): Promise<Creator[]> {
  const res = await fetch(`${API_BASE_URL}/creators`);
  if (!res.ok) throw new Error("Failed to fetch creators");
  return res.json();
};

export async function getCreatorById(id: number): Promise<Creator> {
  const res = await fetch(`${API_BASE_URL}/creators/${id}`);
  if (!res.ok) throw new Error("Creator not found");
  return res.json();
};

export async function getVideosByCreatorId(creatorId: number) {
  const res = await fetch(`${API_BASE_URL}/creators/${creatorId}/videos`);
  if (!res.ok) throw new Error("Failed to fetch videos for creator");
  return res.json();
};

export async function exportWorkoutPDF(youtubeVideoId: string): Promise<Blob> {
  const res = await fetch(`${API_BASE_URL}/workouts/${youtubeVideoId}/export-pdf`);
  if (!res.ok) throw new Error("Failed to export PDF");
  return res.blob();
}; 