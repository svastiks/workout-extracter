"use client";

import { useState, useEffect } from "react";
import { useRouter } from "next/navigation";
import { initiateExtraction, getExtractionStatus } from "@/services/api";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Youtube, Search } from "lucide-react";

export default function ExtractPage() {
  const [url, setUrl] = useState("");
  const [jobId, setJobId] = useState<string | null>(null);
  const [progress, setProgress] = useState<number>(0);
  const [status, setStatus] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);
  const router = useRouter();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    // Extraction logic removed; now handled in the homepage
  };

  useEffect(() => {
    if (!jobId) return;
    let interval: NodeJS.Timeout;
    const pollStatus = async () => {
      try {
        const job = await getExtractionStatus(jobId);
        setStatus(job.status);
        setProgress(job.progress);
        if (job.status === "COMPLETE" && job.resultVideoId) {
          clearInterval(interval);
          router.push(`/extract/${job.resultVideoId}`);
        }
        if (job.status === "FAILED") {
          clearInterval(interval);
          setError(job.errorMessage || "Extraction failed.");
        }
      } catch (err) {
        setError("Failed to get extraction status.");
        clearInterval(interval);
      }
    };
    interval = setInterval(pollStatus, 3000);
    pollStatus();
    return () => clearInterval(interval);
  }, [jobId, router]);

  return (
    <div className="min-h-screen bg-black flex flex-col items-center justify-center">
      <Card className="w-full max-w-xl bg-gray-900 border-gray-800 p-8">
        <CardHeader>
          <CardTitle className="text-2xl text-white mb-2">Extract a Workout</CardTitle>
          <CardDescription className="text-gray-300 mb-4">
            Paste a YouTube URL to extract a structured workout routine.
          </CardDescription>
        </CardHeader>
        <CardContent>
          <form onSubmit={handleSubmit} className="flex flex-col gap-4">
            <div className="flex gap-2 items-center">
              <Youtube className="w-5 h-5 text-red-500" />
              <input
                type="text"
                value={url}
                onChange={e => setUrl(e.target.value)}
                placeholder="Paste YouTube URL here"
                className="flex-1 bg-gray-800 text-white rounded-lg px-4 py-2 border border-gray-700 focus:outline-none"
                required
              />
              <Button type="submit" disabled={!!jobId && status !== 'FAILED' && status !== 'COMPLETE'}>
                <Search className="w-4 h-4 mr-2" />
                {jobId ? 'Processing...' : 'Extract'}
              </Button>
            </div>
            {progress > 0 && <div className="text-gray-400">Progress: {progress}%</div>}
            {status && <div className="text-gray-400">Status: {status}</div>}
            {error && <div className="text-red-500">{error}</div>}
          </form>
              </CardContent>
            </Card>
    </div>
  );
}
