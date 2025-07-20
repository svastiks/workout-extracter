// client/src/services/api.ts

export interface ExtractionJob {
  id: string;
  youtubeVideoId: string;
  status: string;
  progress: number;
  resultVideoId?: number;
  errorMessage?: string;
}

export interface Workout {
  id: number;
  youtubeVideoId: string;
  title: string;
  thumbnailUrl: string;
  workoutData: any; // You can refine this type if you have a schema
  creator: {
    id: number;
    name: string;
    youtubeChannelId: string;
    profileImageUrl: string;
  };
}

// --- Creators API ---

export interface Creator {
  id: number;
  name: string;
  youtubeChannelId: string;
  profileImageUrl: string;
  // Add other fields as needed (subscribers, specialty, etc.)
}

const API_BASE = process.env.NEXT_PUBLIC_API_BASE_URL || "/api/v1";

export const initiateExtraction = async (url: string): Promise<{ jobId: string }> => {
  const res = await fetch(`${API_BASE}/workouts/extract`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ url }),
  });
  if (!res.ok) throw new Error("Failed to initiate extraction");
  return res.json();
};

export const getExtractionStatus = async (jobId: string): Promise<ExtractionJob> => {
  const res = await fetch(`${API_BASE}/workouts/extract/status/${jobId}`);
  if (!res.ok) throw new Error("Failed to get extraction status");
  return res.json();
};

export const getWorkoutDetails = async (videoId: string): Promise<Workout> => {
  const res = await fetch(`${API_BASE}/workouts/${videoId}`);
  if (!res.ok) throw new Error("Workout not found");
  return res.json();
};

export const getWorkoutByYoutubeId = async (youtubeVideoId: string): Promise<Workout> => {
  const res = await fetch(`${API_BASE}/workouts/youtube/${youtubeVideoId}`);
  if (!res.ok) throw new Error("Workout not found");
  return res.json();
};

export const getCreators = async (): Promise<Creator[]> => {
  const res = await fetch(`${API_BASE}/creators`);
  if (!res.ok) throw new Error("Failed to fetch creators");
  return res.json();
};

export const getCreatorById = async (id: number): Promise<Creator> => {
  const res = await fetch(`${API_BASE}/creators/${id}`);
  if (!res.ok) throw new Error("Creator not found");
  return res.json();
};

export const getVideosByCreatorId = async (creatorId: number) => {
  const res = await fetch(`${API_BASE}/creators/${creatorId}/videos`);
  if (!res.ok) throw new Error("Failed to fetch videos for creator");
  return res.json();
}; 