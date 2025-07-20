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
          <Card className="bg-gray-900 border-gray-800">
            <CardHeader>
              <div className="flex items-start gap-6">
                <Image
                  src={creatorData.profileImageUrl || "/placeholder.svg"}
                  alt={creatorData.name}
                  width={100}
                  height={100}
                  className="rounded-full"
                />
                <div className="flex-1">
                  <div className="flex items-center gap-3 mb-2">
                    <CardTitle className="text-3xl text-white">{creatorData.name}</CardTitle>
                  </div>
                  <Badge className="bg-gray-800 text-gray-300 border-gray-700 mb-4">{creatorData.youtubeChannelId}</Badge>
                  <p className="text-gray-400 max-w-2xl">Creator ID: {creatorData.id}</p>
                </div>
              </div>
            </CardHeader>
          </Card>
        </div>
        {/* Videos Section */}
        <div>
          <h2 className="text-2xl text-white mb-4">Videos</h2>
          {videos.length === 0 ? (
            <div className="text-gray-400">No videos found for this creator.</div>
          ) : (
            <div className="grid md:grid-cols-2 lg:grid-cols-3 gap-6">
              {videos.map((video) => (
                <Card key={video.id} className="bg-gray-900 border-gray-800">
                  <CardHeader>
                    <CardTitle className="text-white text-lg">{video.title}</CardTitle>
                  </CardHeader>
                  <CardContent>
                    <Image
                      src={video.thumbnailUrl || "/placeholder.svg"}
                      alt={video.title}
                      width={320}
                      height={180}
                      className="rounded mb-2"
                    />
                    <p className="text-gray-400 text-sm mb-2">YouTube ID: {video.youtubeVideoId}</p>
                    <Link href={`/extract/${video.youtubeVideoId}`} className="text-blue-400 hover:underline">View Details</Link>
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
