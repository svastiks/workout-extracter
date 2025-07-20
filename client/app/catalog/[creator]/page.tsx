import { Button } from "@/components/ui/button"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import { ArrowLeft, Play } from "lucide-react"
import Image from "next/image"
import Link from "next/link"
import { notFound } from "next/navigation"

// Mock data for creators and their videos
const creatorsData = {
  "jeff-nippard": {
    name: "Jeff Nippard",
    subscribers: "4.2M",
    specialty: "Science-Based Training",
    avatar: "/placeholder.svg?height=100&width=100",
    verified: true,
    description:
      "Evidence-based fitness content focusing on hypertrophy and strength training with scientific backing.",
    videos: [
      {
        id: "1",
        title: "The PERFECT Science-Based Push Workout (2024)",
        thumbnail: "/placeholder.svg?height=180&width=320&text=Push+Workout",
        date: "2024-01-15",
        duration: "12:34",
        views: "1.2M",
      },
      {
        id: "2",
        title: "Pull Day Routine: Build a Massive Back & Biceps",
        thumbnail: "/placeholder.svg?height=180&width=320&text=Pull+Workout",
        date: "2024-01-08",
        duration: "15:22",
        views: "890K",
      },
      {
        id: "3",
        title: "Leg Day: The Complete Lower Body Workout",
        thumbnail: "/placeholder.svg?height=180&width=320&text=Leg+Workout",
        date: "2024-01-01",
        duration: "18:45",
        views: "1.5M",
      },
      {
        id: "4",
        title: "How to Build Muscle: The Science Explained",
        thumbnail: "/placeholder.svg?height=180&width=320&text=Muscle+Building",
        date: "2023-12-25",
        duration: "22:10",
        views: "2.1M",
      },
      {
        id: "5",
        title: "Optimal Rep Ranges for Muscle Growth",
        thumbnail: "/placeholder.svg?height=180&width=320&text=Rep+Ranges",
        date: "2023-12-18",
        duration: "14:33",
        views: "756K",
      },
      {
        id: "6",
        title: "Progressive Overload: The Key to Gains",
        thumbnail: "/placeholder.svg?height=180&width=320&text=Progressive+Overload",
        date: "2023-12-11",
        duration: "16:28",
        views: "1.1M",
      },
    ],
  },
  "athlean-x": {
    name: "Athlean-X",
    subscribers: "13.5M",
    specialty: "Functional Training",
    avatar: "/placeholder.svg?height=100&width=100",
    verified: true,
    description: "Athletic training programs and injury prevention techniques from a physical therapist perspective.",
    videos: [
      {
        id: "1",
        title: "22 Minute Hard Corps Workout",
        thumbnail: "/placeholder.svg?height=180&width=320&text=Hard+Corps",
        date: "2024-01-20",
        duration: "22:15",
        views: "2.3M",
      },
      {
        id: "2",
        title: "Fix Your Shoulder Pain (INSTANTLY!)",
        thumbnail: "/placeholder.svg?height=180&width=320&text=Shoulder+Fix",
        date: "2024-01-13",
        duration: "11:42",
        views: "1.8M",
      },
      {
        id: "3",
        title: "The Perfect Push Up Workout",
        thumbnail: "/placeholder.svg?height=180&width=320&text=Push+Up+Workout",
        date: "2024-01-06",
        duration: "15:30",
        views: "3.2M",
      },
    ],
  },
}

interface PageProps {
  params: Promise<{ creator: string }>
}

export default async function CreatorPage({ params }: PageProps) {
  const { creator } = await params
  const creatorData = creatorsData[creator as keyof typeof creatorsData]

  if (!creatorData) {
    notFound()
  }

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
                  src={creatorData.avatar || "/placeholder.svg"}
                  alt={creatorData.name}
                  width={100}
                  height={100}
                  className="rounded-full"
                />
                <div className="flex-1">
                  <div className="flex items-center gap-3 mb-2">
                    <CardTitle className="text-3xl text-white">{creatorData.name}</CardTitle>
                    {creatorData.verified && (
                      <div className="w-6 h-6 bg-blue-500 rounded-full flex items-center justify-center">
                        <Play className="w-3 h-3 text-white fill-white" />
                      </div>
                    )}
                  </div>
                  <Badge className="bg-gray-800 text-gray-300 border-gray-700 mb-4">{creatorData.specialty}</Badge>
                  <p className="text-gray-400 max-w-2xl">{creatorData.description}</p>
                </div>
              </div>
            </CardHeader>
          </Card>
        </div>

        {/* Videos Section */}
        <div className="mb-8">
          <h2 className="text-2xl font-bold text-white mb-6">Latest Analyzed Videos</h2>
          <div className="grid md:grid-cols-2 lg:grid-cols-3 gap-6">
            {creatorData.videos.map((video) => (
              <Link key={video.id} href={`/extract/${video.id}`}>
                <Card className="bg-gray-900 border-gray-800 hover:bg-gray-800 transition-all cursor-pointer group">
                  <div className="relative">
                    <Image
                      src={video.thumbnail || "/placeholder.svg"}
                      alt={video.title}
                      width={320}
                      height={180}
                      className="w-full h-48 object-cover rounded-t-lg"
                    />
                  </div>
                  <CardContent className="p-4">
                    <h3 className="text-white font-semibold mb-2 line-clamp-2 group-hover:text-gray-300 transition-colors">
                      {video.title}
                    </h3>
                    <Button className="w-full mt-4 bg-white hover:bg-gray-200 text-black" size="sm">
                      Extract Workout
                    </Button>
                  </CardContent>
                </Card>
              </Link>
            ))}
          </div>
        </div>

        {/* Load More */}
        <div className="text-center">
          <Button
            variant="outline"
            className="border-gray-600 text-gray-300 hover:bg-white hover:text-black bg-transparent"
          >
            Load More Videos
          </Button>
        </div>
      </div>
    </div>
  )
}
