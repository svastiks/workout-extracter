import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import { Play, Clock, Users, Zap, Youtube, Search, Star } from "lucide-react"
import Image from "next/image"
import Link from "next/link"

const fitnessYoutubers = [
  {
    name: "Jeff Nippard",
    subscribers: "4.2M",
    specialty: "Science-Based Training",
    avatar: "/placeholder.svg?height=80&width=80",
    verified: true,
  },
  {
    name: "Athlean-X",
    subscribers: "13.5M",
    specialty: "Functional Training",
    avatar: "/placeholder.svg?height=80&width=80",
    verified: true,
  },
  {
    name: "Jeremy Ethier",
    subscribers: "5.8M",
    specialty: "Evidence-Based Fitness",
    avatar: "/placeholder.svg?height=80&width=80",
    verified: true,
  },
  {
    name: "Calisthenic Movement",
    subscribers: "2.1M",
    specialty: "Bodyweight Training",
    avatar: "/placeholder.svg?height=80&width=80",
    verified: true,
  },
  {
    name: "Stephanie Buttermore",
    subscribers: "1.3M",
    specialty: "Women's Fitness",
    avatar: "/placeholder.svg?height=80&width=80",
    verified: true,
  },
  {
    name: "Renaissance Periodization",
    subscribers: "1.8M",
    specialty: "Muscle Building Science",
    avatar: "/placeholder.svg?height=80&width=80",
    verified: true,
  },
]

const features = [
  {
    icon: Clock,
    title: "Save Hours of Time",
    description: "Get workout routines in minutes instead of watching 30+ minute videos",
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
            <Badge className="mb-6 bg-gray-800 text-gray-300 border-gray-700">🚀 Extract Any Workout Routine</Badge>
            <h1 className="text-5xl md:text-7xl font-bold text-white mb-6 leading-tight">
              Get Your Workout Routine in <span className="text-gray-400">Minutes</span>
            </h1>
            <p className="text-xl text-gray-300 mb-8 max-w-2xl mx-auto">
              No More Wasting Time Watching Long Videos. Paste any fitness YouTube URL and get a structured workout plan
              instantly.
            </p>

            {/* URL Input */}
            <div className="max-w-2xl mx-auto mb-12">
              <div className="flex gap-3 p-2 bg-gray-900 rounded-2xl border border-gray-800">
                <div className="flex items-center gap-2 px-3">
                  <Youtube className="w-5 h-5 text-red-500" />
                </div>
                <Input
                  placeholder="Paste YouTube URL here (e.g., https://youtube.com/watch?v=...)"
                  className="border-0 bg-transparent text-white placeholder:text-gray-400 focus-visible:ring-0 text-lg"
                />
                <Button className="bg-white hover:bg-gray-200 text-black px-8">
                  <Search className="w-4 h-4 mr-2" />
                  Extract
                </Button>
              </div>
              <p className="text-sm text-gray-400 mt-2">
                Try with videos from Jeff Nippard, Athlean-X, Jeremy Ethier, and more!
              </p>
            </div>

            {/* Features */}
            <div className="grid md:grid-cols-3 gap-6 mb-16">
              {features.map((feature, index) => (
                <Card key={index} className="bg-gray-900 border-gray-800">
                  <CardContent className="p-6 text-center">
                    <feature.icon className="w-12 h-12 text-gray-400 mx-auto mb-4" />
                    <h3 className="text-lg font-semibold text-white mb-2">{feature.title}</h3>
                    <p className="text-gray-400 text-sm">{feature.description}</p>
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
            {fitnessYoutubers.slice(0, 6).map((creator, index) => (
              <Card
                key={index}
                className="bg-gray-900 border-gray-800 hover:bg-gray-800 transition-all cursor-pointer group"
              >
                <CardHeader className="pb-3">
                  <div className="flex items-center gap-4">
                    <div className="relative">
                      <Image
                        src={creator.avatar || "/placeholder.svg"}
                        alt={creator.name}
                        width={60}
                        height={60}
                        className="rounded-full"
                      />
                      {creator.verified && (
                        <div className="absolute -bottom-1 -right-1 w-5 h-5 bg-blue-500 rounded-full flex items-center justify-center">
                          <Star className="w-3 h-3 text-white fill-white" />
                        </div>
                      )}
                    </div>
                    <div>
                      <CardTitle className="text-white text-lg group-hover:text-gray-300 transition-colors">
                        {creator.name}
                      </CardTitle>
                      <CardDescription className="text-gray-400">{creator.subscribers} subscribers</CardDescription>
                    </div>
                  </div>
                </CardHeader>
                <CardContent className="pt-0">
                  <Badge variant="secondary" className="bg-gray-800 text-gray-300 border-gray-700">
                    {creator.specialty}
                  </Badge>
                </CardContent>
              </Card>
            ))}
          </div>

          <div className="text-center">
            <Link href="/catalog">
              <Button className="bg-white hover:bg-gray-200 text-black px-8 py-3">View All Creators</Button>
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
