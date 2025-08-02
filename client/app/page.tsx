"use client";

import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import { Play, Clock, Users, Zap, Youtube, Search, Star } from "lucide-react"
import Image from "next/image"
import Link from "next/link"
import { useState, useEffect } from "react";
import React from "react";
import { initiateExtraction, getCreators, type Creator, getVideosByCreatorId } from "@/services/api";
import { useRouter } from "next/navigation";

const features = [
  {
    icon: Clock,
    title: "Save Hours of Time",
    description: "Get workout routines in seconds instead of watching 30+ minute videos",
  },
  {
    icon: Zap,
    title: "Instant Analysis",
    description: "AI-powered extraction identifies exercises, sets, reps, and rest periods",
  },
  {
    icon: Users,
    title: "Top Fitness Creators",
    description: "Curated catalog of the most trusted fitness YouTubers and their content",
  },
]

export default function HomePage() {
  const [url, setUrl] = useState("");
  const [creators, setCreators] = useState<Creator[]>([]);
  const [loading, setLoading] = useState(true);
  const router = useRouter();

  useEffect(() => {
    const fetchCreators = async () => {
      try {
        const creatorsData = await getCreators();
        // Fetch video counts for each creator
        const creatorsWithVideoCounts = await Promise.all(
          creatorsData.slice(0, 6).map(async (creator) => {
            try {
              const videos = await getVideosByCreatorId(creator.id);
              return {
                ...creator,
                videoCount: videos.length
              };
            } catch (error) {
              console.error(`Failed to fetch videos for creator ${creator.name}:`, error);
              return {
                ...creator,
                videoCount: 0
              };
            }
          })
        );
        setCreators(creatorsWithVideoCounts);
      } catch (error) {
        console.error("Failed to fetch creators:", error);
        // Fallback to empty array
        setCreators([]);
      } finally {
        setLoading(false);
      }
    };

    fetchCreators();
  }, []);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    console.log("[Extract] Form submitted! URL:", url);
    try {
      console.log("[Extract] Calling initiateExtraction...");
      const data = await initiateExtraction(url);
      console.log("[Extract] API call succeeded. Response:", data);
      if ('jobId' in data) {
        // New extraction started, redirect to loading page
        console.log("Redirecting to loading page with jobId:", data.jobId);
        router.push(`/loading/${data.jobId}`);
      } else if ('id' in data && 'youtubeVideoId' in data) {
        console.log("Redirecting to: /extract/" + data.youtubeVideoId);
        router.push(`/extract/${data.youtubeVideoId}`);
      } else {
        console.log("Unexpected response from server:", data);
      }
    } catch (err) {
      console.log("[Extract] Error during extraction:", err);
    }
  };

  return (
    <div className="min-h-screen bg-black">
      {/* Header */}
      <header className="border-b border-gray-800 bg-black">
        <div className="container mx-auto px-4 py-4">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-2">
              <div className="w-8 h-8 bg-white rounded-lg flex items-center justify-center">
                <Play className="w-4 h-4 text-black fill-black" />
              </div>
              <span className="text-xl font-bold text-white">WorkoutExtract</span>
            </div>
            <nav className="hidden md:flex items-center gap-6">
              <Link href="/catalog" className="text-gray-300 hover:text-white transition-colors">
                Catalog
              </Link>
              <Link href="#how-it-works" className="text-gray-300 hover:text-white transition-colors">
                How it Works
              </Link>
              <Button
                variant="outline"
                className="border-gray-600 text-gray-300 hover:bg-white hover:text-black bg-transparent"
              >
                Sign In
              </Button>
            </nav>
          </div>
        </div>
      </header>

      {/* Hero Section */}
      <section className="py-20 px-4">
        <div className="container mx-auto text-center">
          <div className="max-w-4xl mx-auto">
            <div className="inline-flex items-center rounded-full border px-3 py-1 text-sm font-semibold transition-colors mb-6 bg-gray-800 text-gray-300 border-gray-700 hover:bg-gray-700">🚀 Extract Any Workout Routine</div>
            <h1 className="text-5xl md:text-7xl font-bold text-white mb-6 leading-tight">
              Get Your Workout Routine in <span className="text-gray-300">Seconds</span>
            </h1>
            <p className="text-xl text-gray-300 mb-8 max-w-2xl mx-auto">
              No More Wasting Time Watching Long Videos. Paste any fitness YouTube URL and get a structured workout plan
              instantly.
            </p>

            {/* URL Input */}
            <div className="max-w-2xl mx-auto mb-12">
              <form onSubmit={handleSubmit}>
                <div className="flex gap-3 p-3 bg-gray-900 rounded-2xl border border-gray-800">
                  <div className="flex items-center gap-2 px-3">
                    <Youtube className="w-5 h-5 text-red-500" />
                  </div>
                  <Input
                    type="text"
                    value={url}
                    onChange={e => setUrl(e.target.value)}
                    placeholder="Paste YouTube URL here (e.g., https://youtube.com/watch?v=...)"
                    className="border-0 bg-transparent text-white placeholder:text-gray-400 focus-visible:ring-0 focus-visible:outline-none text-lg flex-1"
                    required
                  />
                  <Button type="submit" className="bg-white hover:bg-gray-200 text-black px-8 border-0 shadow-none rounded-xl">
                    <Search className="w-4 h-4 mr-2 text-black" />
                    Extract
                  </Button>
                </div>
              </form>
              <p className="text-sm text-gray-400 mt-2">
                Try with videos from Jeff Nippard, Athlean-X, Jeremy Ethier, and more!
              </p>
            </div>

            {/* Features */}
            <div className="grid md:grid-cols-3 gap-8 mb-16 max-w-6xl mx-auto">
              {features.map((feature, index) => (
                <Card key={index} className="bg-gray-900 border-gray-800 min-h-[200px]">
                  <CardContent className="p-8 text-center">
                    <feature.icon className="w-16 h-16 text-gray-400 mx-auto mb-6" />
                    <h3 className="text-xl font-semibold text-white mb-3">{feature.title}</h3>
                    <p className="text-gray-400 text-base">{feature.description}</p>
                  </CardContent>
                </Card>
              ))}
            </div>
          </div>
        </div>
      </section>

      {/* YouTuber Catalog Preview */}
      <section id="catalog" className="py-20 px-4 bg-gray-950">
        <div className="container mx-auto">
          <div className="text-center mb-12">
            <h2 className="text-4xl font-bold text-white mb-4">Trusted Fitness Creators</h2>
            <p className="text-xl text-gray-300 max-w-2xl mx-auto">
              Extract workouts from the most popular and trusted fitness YouTubers
            </p>
          </div>

          <div className="grid md:grid-cols-2 lg:grid-cols-3 gap-6 mb-8">
            {loading ? (
              <p className="text-center text-gray-400">Loading creators...</p>
            ) : creators.length === 0 ? (
              <p className="text-center text-gray-400">No creators found.</p>
            ) : (
              creators.map((creator, index) => (
                <Card
                  key={index}
                  className="bg-gray-900 border-gray-800 hover:bg-gray-800 transition-all cursor-pointer group"
                >
                  <CardHeader className="pb-3">
                    <div className="flex items-center gap-4">
                      <div className="relative">
                        <Image
                          src={creator.profileImageUrl || "/placeholder.svg"}
                          alt={creator.name}
                          width={60}
                          height={60}
                          className="rounded-full"
                        />
                        <div className="absolute -bottom-1 -right-1 w-5 h-5 bg-blue-500 rounded-full flex items-center justify-center">
                          <Star className="w-3 h-3 text-white fill-white" />
                        </div>
                      </div>
                      <div>
                        <CardTitle className="text-white text-lg group-hover:text-gray-300 transition-colors">
                          {creator.name}
                        </CardTitle>
                        <CardDescription className="text-gray-400">Verified Creator</CardDescription>
                      </div>
                    </div>
                  </CardHeader>
                  <CardContent className="pt-0">
                    <Badge variant="secondary" className="bg-gray-800 text-gray-300 border-gray-700 text-sm px-3 py-1">
                      {creator.videoCount || 0} {creator.videoCount === 1 ? 'video' : 'videos'}
                    </Badge>
                  </CardContent>
                </Card>
              ))
            )}
          </div>

          <div className="text-center">
            <Link href="/catalog">
              <Button className="bg-white hover:bg-gray-200 text-black px-8 py-3 rounded-xl">View All Creators</Button>
            </Link>
          </div>
        </div>
      </section>

      {/* How It Works */}
      <section id="how-it-works" className="py-20 px-4">
        <div className="container mx-auto">
          <div className="text-center mb-12">
            <h2 className="text-4xl font-bold text-white mb-4">How It Works</h2>
            <p className="text-xl text-gray-300">Three simple steps to get your workout routine</p>
          </div>

          <div className="grid md:grid-cols-3 gap-8 max-w-4xl mx-auto">
            <div className="text-center">
              <div className="w-16 h-16 bg-white rounded-full flex items-center justify-center mx-auto mb-4">
                <span className="text-2xl font-bold text-black">1</span>
              </div>
              <h3 className="text-xl font-semibold text-white mb-2">Paste URL</h3>
              <p className="text-gray-400">Copy and paste any fitness YouTube video URL into our analyzer</p>
            </div>

            <div className="text-center">
              <div className="w-16 h-16 bg-white rounded-full flex items-center justify-center mx-auto mb-4">
                <span className="text-2xl font-bold text-black">2</span>
              </div>
              <h3 className="text-xl font-semibold text-white mb-2">AI Analysis</h3>
              <p className="text-gray-400">Our AI extracts exercises, sets, reps, and timing from the video</p>
            </div>

            <div className="text-center">
              <div className="w-16 h-16 bg-white rounded-full flex items-center justify-center mx-auto mb-4">
                <span className="text-2xl font-bold text-black">3</span>
              </div>
              <h3 className="text-xl font-semibold text-white mb-2">Get Routine</h3>
              <p className="text-gray-400">Receive a clean, structured workout plan ready to use</p>
            </div>
          </div>
        </div>
      </section>

      {/* CTA Section */}
      <section className="py-20 px-4 bg-gray-900">
        <div className="container mx-auto text-center">
          <h2 className="text-4xl font-bold text-white mb-4">Ready to Extract Your First Workout?</h2>
          <p className="text-xl text-gray-300 mb-8 max-w-2xl mx-auto">
            Join thousands of fitness enthusiasts who save time with WorkoutExtract
          </p>
          <Button size="lg" className="bg-white hover:bg-gray-200 text-black px-8 py-3 text-lg">
            Start Extracting Now
          </Button>
        </div>
      </section>

      {/* Footer */}
      <footer className="border-t border-gray-800 bg-black py-8">
        <div className="container mx-auto px-4">
          <div className="flex flex-col md:flex-row items-center justify-between">
            <div className="flex items-center gap-2 mb-4 md:mb-0">
              <div className="w-6 h-6 bg-white rounded flex items-center justify-center">
                <Play className="w-3 h-3 text-black fill-black" />
              </div>
              <span className="text-white font-semibold">WorkoutExtract</span>
            </div>
            <div className="flex items-center gap-6 text-gray-400 text-sm">
              <Link href="#" className="hover:text-white transition-colors">
                Privacy
              </Link>
              <Link href="#" className="hover:text-white transition-colors">
                Terms
              </Link>
              <Link href="#" className="hover:text-white transition-colors">
                Contact
              </Link>
            </div>
          </div>
        </div>
      </footer>
    </div>
  )
}
