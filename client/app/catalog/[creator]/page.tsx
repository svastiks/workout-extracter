"use client"

import { Button } from "@/components/ui/button"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import { ArrowLeft, Play } from "lucide-react"
import Image from "next/image"
import Link from "next/link"
import { notFound, useParams } from "next/navigation"
import { useEffect, useState } from "react"
import { getCreatorById, getVideosByCreatorId, Creator, Workout } from "@/services/api"

export default function CreatorPage() {
  const params = useParams();
  const creatorId = params?.creator as string;
  const [creatorData, setCreatorData] = useState<Creator | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [videos, setVideos] = useState<Workout[]>([]);

  useEffect(() => {
    if (!creatorId) return;
    getCreatorById(Number(creatorId))
      .then(data => { setCreatorData(data); setLoading(false); })
      .catch(() => { setError("Creator not found"); setLoading(false); });
    getVideosByCreatorId(Number(creatorId))
      .then(setVideos)
      .catch(() => {});
  }, [creatorId]);

  if (loading) return <div className="text-center text-white py-20">Loading creator...</div>;
  if (error || !creatorData) return <div className="text-center text-red-500 py-20">{error || "Creator not found"}</div>;

  return (
    <div className="min-h-screen bg-black">
      {/* Header */}
      <header className="border-b border-gray-800 bg-black sticky top-0 z-50">
        <div className="container mx-auto px-4 py-4">
          <div className="flex items-center justify-between">
            <Link href="/catalog" className="flex items-center gap-2">
              <ArrowLeft className="w-5 h-5 text-gray-400" />
              <span className="text-gray-400 hover:text-white transition-colors">Back to Catalog</span>
            </Link>
            <div className="flex items-center gap-2">
              <div className="w-6 h-6 bg-white rounded flex items-center justify-center">
                <Play className="w-3 h-3 text-black fill-black" />
              </div>
              <span className="text-white font-semibold">Videos</span>
            </div>
          </div>
        </div>
      </header>

      <div className="container mx-auto px-4 py-8">
        {/* Creator Header */}
        <div className="mb-12">
          <Card className="bg-gray-900 border-gray-800 p-8">
            <div className="flex items-center gap-8">
              <div className="relative flex-shrink-0">
                <Image
                  src={creatorData.profileImageUrl || "/placeholder.svg"}
                  alt={creatorData.name}
                  width={120}
                  height={120}
                  className="rounded-full w-30 h-30 object-cover"
                />
              </div>
              <div className="flex-1">
                <div className="flex items-center gap-3 mb-4">
                  <CardTitle className="text-4xl text-white font-bold">{creatorData.name}</CardTitle>
                </div>
                <Link 
                  href={`https://www.youtube.com/results?search_query=${encodeURIComponent(creatorData.name)}`}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="inline-flex items-center gap-2 text-blue-400 hover:text-blue-300 transition-colors"
                >
                  <span>Find them on YouTube</span>
                  <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M10 6H6a2 2 0 00-2 2v10a2 2 0 002 2h10a2 2 0 002-2v-4M14 4h6m0 0v6m0-6L10 14" />
                  </svg>
                </Link>
              </div>
            </div>
          </Card>
        </div>
        
        {/* Videos Section */}
        <div>
          <h2 className="text-3xl text-white font-bold mb-8">Latest Analyzed Videos</h2>
          {videos.length === 0 ? (
            <div className="text-gray-400">No videos found for this creator.</div>
          ) : (
            <div className="grid md:grid-cols-2 lg:grid-cols-3 gap-6">
              {videos.map((video) => (
                <Card key={video.id} className="bg-gray-900 border-gray-800 overflow-hidden hover:bg-gray-800 transition-all cursor-pointer group">
                  <div className="relative">
                    <Image
                      src={video.thumbnailUrl || "/placeholder.svg"}
                      alt={video.title}
                      width={400}
                      height={225}
                      className="w-full h-48 object-cover"
                    />
                  </div>
                  <CardContent className="p-4">
                    <h3 className="text-white font-semibold text-lg mb-3 line-clamp-2 group-hover:text-gray-300 transition-colors">
                      {video.title}
                    </h3>
                    <Button 
                      className="w-full bg-white hover:bg-gray-200 text-black font-medium"
                      asChild
                    >
                      <Link href={`/extract/${video.youtubeVideoId}`}>
                        View Workout
                      </Link>
                    </Button>
                  </CardContent>
                </Card>
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  )
}
