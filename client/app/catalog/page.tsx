"use client";

import { Button } from "@/components/ui/button"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import { Input } from "@/components/ui/input"
import { ArrowLeft, Search, Star, Users } from "lucide-react"
import Image from "next/image"
import Link from "next/link"
import { useEffect, useState } from "react"
import { getCreators, getVideosByCreatorId, Creator } from "@/services/api"

export default function CatalogPage() {
  const [creators, setCreators] = useState<Creator[]>([]);
  const [filteredCreators, setFilteredCreators] = useState<Creator[]>([]);
  const [searchQuery, setSearchQuery] = useState("");
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchCreatorsWithVideoCounts = async () => {
      try {
        const creatorsData = await getCreators();
        // Fetch video counts for each creator
        const creatorsWithVideoCounts = await Promise.all(
          creatorsData.map(async (creator) => {
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
        setLoading(false);
      } catch (error) {
        setError("Failed to load creators");
        setLoading(false);
      }
    };

    fetchCreatorsWithVideoCounts();
  }, []);

  // Filter creators based on search query
  useEffect(() => {
    if (!searchQuery.trim()) {
      setFilteredCreators(creators);
    } else {
      const filtered = creators.filter(creator =>
        creator.name.toLowerCase().includes(searchQuery.toLowerCase())
      );
      setFilteredCreators(filtered);
    }
  }, [searchQuery, creators]);

  if (loading) return <div className="text-center text-white py-20">Loading creators...</div>;
  if (error) return <div className="text-center text-red-500 py-20">{error}</div>;

  return (
    <div className="min-h-screen bg-black">
      {/* Header */}
      <header className="border-b border-gray-800 bg-black sticky top-0 z-50">
        <div className="container mx-auto px-4 py-4">
          <div className="flex items-center justify-between">
            <Link href="/" className="flex items-center gap-2">
              <ArrowLeft className="w-5 h-5 text-gray-400" />
              <span className="text-gray-400 hover:text-white transition-colors">Back to Home</span>
            </Link>
            <div className="flex items-center gap-2">
              <div className="w-6 h-6 bg-white rounded flex items-center justify-center">
                <Users className="w-3 h-3 text-black" />
              </div>
              <span className="text-white font-semibold">Fitness Creator Catalog</span>
            </div>
          </div>
        </div>
      </header>

      <div className="container mx-auto px-4 py-8">
        {/* Page Header */}
        <div className="text-center mb-12">
          <h1 className="text-4xl font-bold text-white mb-4">Fitness Creator Catalog</h1>
          <p className="text-xl text-gray-300 max-w-2xl mx-auto mb-8">
            Browse our curated collection of top fitness YouTubers and extract workouts from their videos
          </p>

          {/* Search Bar */}
          <div className="max-w-md mx-auto">
            <div className="relative">
              <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 w-4 h-4" />
              <Input
                placeholder="Search creators..."
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                className="pl-10 bg-gray-900 border-gray-800 text-white placeholder:text-gray-400 focus-visible:ring-1 focus-visible:ring-gray-600 rounded-md"
              />
            </div>
          </div>
        </div>

        {/* Creator Grid */}
        {filteredCreators.length === 0 && searchQuery.trim() ? (
          <div className="text-center py-12">
            <p className="text-gray-400 text-lg">No creators found matching "{searchQuery}"</p>
            <p className="text-gray-500 text-sm mt-2">Try a different search term</p>
          </div>
        ) : (
          <div className="grid md:grid-cols-2 lg:grid-cols-3 gap-6">
            {filteredCreators.map((creator) => (
              <Link key={creator.id} href={`/catalog/${creator.id}`}>
                <Card className="bg-gray-900 border-gray-800 hover:bg-gray-800 transition-all cursor-pointer group h-full">
                  <CardHeader className="pb-4">
                    <div className="flex items-start gap-4">
                      <div className="relative flex-shrink-0">
                        <Image
                          src={creator.profileImageUrl || "/placeholder.svg"}
                          alt={creator.name}
                          width={80}
                          height={80}
                          className="rounded-full w-20 h-20 object-cover"
                        />
                      </div>
                      <div className="flex-1 min-w-0">
                        <CardTitle className="text-white text-xl font-bold group-hover:text-gray-300 transition-colors truncate mb-2">
                          {creator.name}
                        </CardTitle>
                        <p className="text-gray-400 text-sm mb-3">
                          {creator.videoCount || 0} videos analyzed
                        </p>
                      </div>
                    </div>
                  </CardHeader>
                  <CardContent className="pt-0">
                    <div className="flex items-center justify-between">
                      <Button
                        variant="ghost"
                        size="sm"
                        className="text-gray-400 hover:text-white hover:bg-gray-800 p-0 h-auto font-medium"
                      >
                        View Videos →
                      </Button>
                    </div>
                  </CardContent>
                </Card>
              </Link>
            ))}
          </div>
        )}

        {/* Stats */}
        <div className="mt-16 text-center">
          <div className="grid md:grid-cols-3 gap-8 max-w-2xl mx-auto">
            <div>
              <div className="text-3xl font-bold text-white mb-2">{creators.length}</div>
              <div className="text-gray-400">Fitness Creators</div>
            </div>
            <div>
              <div className="text-3xl font-bold text-white mb-2">2.5K+</div>
              <div className="text-gray-400">Total Videos</div>
            </div>
            <div>
              <div className="text-3xl font-bold text-white mb-2">50M+</div>
              <div className="text-gray-400">Combined Subscribers</div>
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}
