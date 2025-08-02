"use client";

import { useState, useEffect } from "react";
import { useRouter, useParams } from "next/navigation";
import { getExtractionStatus } from "@/services/api";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Progress } from "@/components/ui/progress";
import { Button } from "@/components/ui/button";
import { ArrowLeft, Loader2, CheckCircle, AlertCircle, Play, Download, Brain, FileText, Users } from "lucide-react";
import Link from "next/link";

interface ProgressState {
  status: string;
  progress: number;
  message: string;
  icon: React.ComponentType<any>;
  color: string;
}

const formatStatus = (status: string): string => {
  return status.replace(/_/g, ' ');
};

const getProgressState = (status: string, progress: number): ProgressState => {
  switch (status) {
    case "PENDING":
      return {
        status: "PENDING",
        progress: 0,
        message: "Initializing extraction...",
        icon: Play,
        color: "text-blue-400"
      };
    case "FETCHING":
      return {
        status: "FETCHING",
        progress: 10,
        message: "Downloading video data...",
        icon: Download,
        color: "text-blue-400"
      };
    case "ANALYZING_WORKOUT":
      return {
        status: "ANALYZING_WORKOUT",
        progress: 75,
        message: "Analyzing workout content...",
        icon: Brain,
        color: "text-purple-400"
      };
    case "COMPLETE":
      return {
        status: "COMPLETE",
        progress: 100,
        message: "Extraction complete!",
        icon: CheckCircle,
        color: "text-green-400"
      };
    case "FAILED":
      return {
        status: "FAILED",
        progress: 0,
        message: "Extraction failed",
        icon: AlertCircle,
        color: "text-red-400"
      };
    default:
      return {
        status: "PROCESSING",
        progress: progress,
        message: "Processing...",
        icon: Loader2,
        color: "text-gray-400"
      };
  }
};

export default function LoadingPage() {
  const params = useParams();
  const jobId = params?.jobId as string;
  const router = useRouter();
  
  const [currentState, setCurrentState] = useState<ProgressState>({
    status: "PENDING",
    progress: 0,
    message: "Initializing extraction...",
    icon: Play,
    color: "text-blue-400"
  });
  const [error, setError] = useState<string | null>(null);
  const [isPolling, setIsPolling] = useState(true);
  const [animatedProgress, setAnimatedProgress] = useState(0);

  useEffect(() => {
    if (!jobId) return;

    const pollStatus = async () => {
      try {
        const job = await getExtractionStatus(jobId);
        console.log("[Loading] Job status:", job.status, "Progress:", job.progress);
        const progressState = getProgressState(job.status, job.progress);
        setCurrentState(progressState);
        
        // Animate progress bar smoothly
        setAnimatedProgress(prev => {
          const target = job.progress;
          console.log("[Loading] Progress animation - prev:", prev, "target:", target);
          if (prev < target) {
            return Math.min(prev + 1, target);
          }
          return target;
        });

        // Handle completion
        if (job.status === "COMPLETE") {
          const youtubeVideoId = job.resultYoutubeVideoId || job.youtubeVideoId;
          if (youtubeVideoId) {
            console.log("[Loading] Job complete, redirecting to:", youtubeVideoId);
            setIsPolling(false);
            // Redirect to results page after a short delay
            setTimeout(() => {
              console.log("[Loading] Executing redirect to:", `/extract/${youtubeVideoId}`);
              router.push(`/extract/${youtubeVideoId}`);
            }, 1500);
          } else {
            console.log("[Loading] Job complete but no YouTube video ID found:", job);
            console.log("[Loading] Full job object:", JSON.stringify(job, null, 2));
            console.log("[Loading] Available fields:", Object.keys(job));
          }
        }

        // Handle failure
        if (job.status === "FAILED") {
          setIsPolling(false);
          setError(job.errorMessage || "Extraction failed. Please try again.");
        }
      } catch (err) {
        setError("Failed to get extraction status. Please try again.");
        setIsPolling(false);
      }
    };

    // Initial poll
    pollStatus();

    // Set up polling interval
    const interval = setInterval(() => {
      if (isPolling) {
        pollStatus();
      }
    }, 3000);

    return () => clearInterval(interval);
  }, [jobId, router, isPolling]);

  // Animate progress bar
  useEffect(() => {
    if (animatedProgress < currentState.progress) {
      const timer = setTimeout(() => {
        setAnimatedProgress(prev => Math.min(prev + 1, currentState.progress));
      }, 50);
      return () => clearTimeout(timer);
    }
  }, [animatedProgress, currentState.progress]);

  const IconComponent = currentState.icon;

    return (
    <div className="min-h-screen bg-black">
      {/* Header */}
      <header className="border-b border-gray-800 bg-black">
        <div className="container mx-auto px-4 py-4">
          <div className="flex items-center justify-between">
            <Link href="/" className="flex items-center gap-2 text-gray-400 hover:text-white transition-colors">
              <ArrowLeft className="w-5 h-5" />
              <span>Back to Home</span>
            </Link>
            <div className="flex items-center gap-2">
              <div className="w-8 h-8 bg-white rounded-lg flex items-center justify-center">
                <Users className="w-4 h-4 text-black" />
              </div>
              <span className="text-xl font-bold text-white">Extracting...</span>
            </div>
          </div>
        </div>
      </header>

      {/* Main Content */}
      <div className="flex flex-col items-center justify-center p-8 min-h-[calc(100vh-80px)]">
        <div className="w-full max-w-2xl">
          <div className="text-center mb-12">
            <h1 className="text-4xl font-bold text-white mb-4">Extracting Workout</h1>
            <p className="text-xl text-gray-400">We're analyzing your video to extract the workout routine</p>
          </div>

          {/* Progress Card */}
          <Card className="bg-gray-900 border-gray-800">
            <CardContent className="p-8">
              <div className="text-center mb-8">
                <div className="inline-flex items-center justify-center w-20 h-20 bg-gray-800 rounded-full mb-6">
                  <IconComponent className={`w-10 h-10 ${currentState.color}`} />
                </div>
                <h2 className="text-2xl font-semibold text-white mb-3">{currentState.message}</h2>
                <p className="text-gray-400 text-lg">
                  {currentState.status === "PENDING" && "Setting up the extraction process..."}
                  {currentState.status === "FETCHING" && "Downloading video metadata and transcript..."}
                  {currentState.status === "ANALYZING_WORKOUT" && "Using AI to extract exercises, sets, and reps..."}
                  {currentState.status === "COMPLETE" && "Your workout is ready!"}
                  {currentState.status === "FAILED" && "Something went wrong during extraction"}
                </p>
              </div>

              {/* Progress Bar */}
              <div className="mb-8">
                <div className="flex justify-between text-lg text-gray-400 mb-3">
                  <span>Progress</span>
                  <span>{animatedProgress}%</span>
                </div>
                <Progress value={animatedProgress} className="h-3" />
              </div>

              {/* Status Details */}
              <div className="space-y-4">
                <div className="flex items-center justify-between text-lg">
                  <span className="text-gray-400">Status</span>
                  <span className={`font-medium ${
                    currentState.status === "COMPLETE" ? "text-green-400" :
                    currentState.status === "FAILED" ? "text-red-400" :
                    "text-blue-400"
                  }`}>
                    {formatStatus(currentState.status)}
                  </span>
                </div>
                
                {currentState.status === "ANALYZING_WORKOUT" && (
                  <div className="flex items-center gap-3 text-lg text-gray-400">
                    <Brain className="w-5 h-5" />
                    <span>AI is analyzing video content and comments</span>
                  </div>
                )}
                
                {currentState.status === "FETCHING" && (
                  <div className="flex items-center gap-3 text-lg text-gray-400">
                    <Download className="w-5 h-5" />
                    <span>Downloading video data and transcript</span>
                  </div>
                )}
              </div>

              {/* Error Message */}
              {error && (
                <div className="mt-6 p-6 bg-red-900/20 border border-red-800 rounded-lg">
                  <div className="flex items-center gap-3 text-red-400 mb-3">
                    <AlertCircle className="w-5 h-5" />
                    <span className="font-medium text-lg">Error</span>
                  </div>
                  <p className="text-red-300 text-base">{error}</p>
                  <Button 
                    onClick={() => router.push("/")}
                    className="mt-4 w-full bg-red-600 hover:bg-red-700 text-white"
                  >
                    Try Again
                  </Button>
                </div>
              )}

              {/* Loading Animation */}
              {isPolling && currentState.status !== "COMPLETE" && (
                <div className="mt-6 text-center">
                  <div className="inline-flex items-center gap-3 text-gray-400 text-lg">
                    <Loader2 className="w-5 h-5 animate-spin" />
                    <span>Checking status...</span>
                  </div>
                </div>
              )}

              {/* Success Message */}
              {currentState.status === "COMPLETE" && (
                <div className="mt-6 p-6 bg-green-900/20 border border-green-800 rounded-lg">
                  <div className="flex items-center gap-3 text-green-400 mb-3">
                    <CheckCircle className="w-5 h-5" />
                    <span className="font-medium text-lg">Success!</span>
                  </div>
                  <p className="text-green-300 text-base">Redirecting to your workout results...</p>
                </div>
              )}
            </CardContent>
          </Card>

          {/* Tips */}
          <div className="mt-8 text-center">
            <p className="text-gray-500 text-base">
              This process typically takes 30-60 seconds depending on video length
            </p>
          </div>
        </div>
      </div>
    </div>
  );
} 