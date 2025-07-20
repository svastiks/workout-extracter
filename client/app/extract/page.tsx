import { Button } from "@/components/ui/button"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import { Separator } from "@/components/ui/separator"
import { Clock, Users, Target, ArrowLeft, Download, Share2 } from "lucide-react"
import Link from "next/link"
import Image from "next/image"

// Mock extracted workout data
const workoutData = {
  videoTitle: "The PERFECT Science-Based Push Workout (2024)",
  creator: "Jeff Nippard",
  duration: "12 minutes",
  difficulty: "Intermediate",
  equipment: ["Barbell", "Dumbbells", "Cable Machine"],
  targetMuscles: ["Chest", "Shoulders", "Triceps"],
  exercises: [
    {
      name: "Barbell Bench Press",
      sets: 4,
      reps: "6-8",
      rest: "3 minutes",
      notes: "Focus on controlled eccentric, pause at chest",
    },
    {
      name: "Incline Dumbbell Press",
      sets: 3,
      reps: "8-10",
      rest: "2-3 minutes",
      notes: "30-45 degree incline, full range of motion",
    },
    {
      name: "Cable Lateral Raises",
      sets: 3,
      reps: "12-15",
      rest: "90 seconds",
      notes: "Slight forward lean, control the negative",
    },
    {
      name: "Overhead Press",
      sets: 3,
      reps: "8-10",
      rest: "2-3 minutes",
      notes: "Standing or seated, core engaged throughout",
    },
    {
      name: "Close-Grip Bench Press",
      sets: 3,
      reps: "10-12",
      rest: "2 minutes",
      notes: "Hands shoulder-width apart, elbows tucked",
    },
    {
      name: "Cable Tricep Pushdowns",
      sets: 3,
      reps: "12-15",
      rest: "90 seconds",
      notes: "Keep elbows stationary, full extension",
    },
  ],
}

export default function ExtractPage() {
  return (
    <div className="bg-black min-h-screen">
      {/* Header */}
      <header className="bg-black border-b border-white/10 backdrop-blur-sm">
        <div className="container mx-auto px-4 py-4">
          <div className="flex items-center justify-between">
            <Link href="/" className="flex items-center gap-2">
              <ArrowLeft className="w-5 h-5 text-gray-400" />
              <span className="text-gray-400 hover:text-white transition-colors">Back to Home</span>
            </Link>
            <div className="flex items-center gap-3">
              <Button
                variant="outline"
                size="sm"
                className="border-purple-500 text-purple-400 hover:bg-purple-500 hover:text-white bg-transparent"
              >
                <Share2 className="w-4 h-4 mr-2" />
                Share
              </Button>
              <Button size="sm" className="bg-white hover:bg-gray-200 text-black">
                <Download className="w-4 h-4 mr-2" />
                Export
              </Button>
            </div>
          </div>
        </div>
      </header>

      <div className="container mx-auto px-4 py-8">
        {/* Video Info */}
        <Card className="bg-gray-900 border-gray-800">
          <CardHeader>
            <div className="flex items-start gap-4">
              <Image
                src="/placeholder.svg?height=80&width=80"
                alt={workoutData.creator}
                width={60}
                height={60}
                className="rounded-full"
              />
              <div className="flex-1">
                <CardTitle className="text-2xl text-white mb-2">{workoutData.videoTitle}</CardTitle>
                <CardDescription className="text-gray-300 text-lg mb-4">by {workoutData.creator}</CardDescription>
                <div className="flex flex-wrap gap-3">
                  <Badge className="bg-gray-800 text-gray-300 border-gray-700">
                    <Clock className="w-3 h-3 mr-1" />
                    {workoutData.duration}
                  </Badge>
                  <Badge className="bg-blue-500/20 text-blue-300 border-blue-500/30">
                    <Target className="w-3 h-3 mr-1" />
                    {workoutData.difficulty}
                  </Badge>
                  <Badge className="bg-green-500/20 text-green-300 border-green-500/30">
                    <Users className="w-3 h-3 mr-1" />
                    Push Day
                  </Badge>
                </div>
              </div>
            </div>
          </CardHeader>
        </Card>

        <div className="grid lg:grid-cols-3 gap-8">
          {/* Workout Details */}
          <div className="lg:col-span-2">
            <Card className="bg-gray-900 border-gray-800">
              <CardHeader>
                <CardTitle className="text-xl text-white">Workout Routine</CardTitle>
                <CardDescription className="text-gray-300">
                  Extracted exercises with sets, reps, and rest periods
                </CardDescription>
              </CardHeader>
              <CardContent className="space-y-6">
                {workoutData.exercises.map((exercise, index) => (
                  <div key={index}>
                    <div className="flex items-start justify-between mb-3">
                      <div>
                        <h3 className="text-lg font-semibold text-white mb-1">
                          {index + 1}. {exercise.name}
                        </h3>
                        <p className="text-sm text-gray-400">{exercise.notes}</p>
                      </div>
                    </div>
                    <div className="grid grid-cols-3 gap-4 text-sm">
                      <div className="bg-white/5 rounded-lg p-3 text-center">
                        <div className="text-gray-300 font-semibold">{exercise.sets}</div>
                        <div className="text-gray-400">Sets</div>
                      </div>
                      <div className="bg-white/5 rounded-lg p-3 text-center">
                        <div className="text-gray-300 font-semibold">{exercise.reps}</div>
                        <div className="text-gray-400">Reps</div>
                      </div>
                      <div className="bg-white/5 rounded-lg p-3 text-center">
                        <div className="text-gray-300 font-semibold">{exercise.rest}</div>
                        <div className="text-gray-400">Rest</div>
                      </div>
                    </div>
                    {index < workoutData.exercises.length - 1 && <Separator className="mt-6 bg-white/10" />}
                  </div>
                ))}
              </CardContent>
            </Card>
          </div>

          {/* Sidebar */}
          <div className="space-y-6">
            {/* Target Muscles */}
            <Card className="bg-gray-900 border-gray-800">
              <CardHeader>
                <CardTitle className="text-lg text-white">Target Muscles</CardTitle>
              </CardHeader>
              <CardContent>
                <div className="flex flex-wrap gap-2">
                  {workoutData.targetMuscles.map((muscle, index) => (
                    <Badge key={index} variant="secondary" className="bg-gray-800 text-gray-300 border-gray-700">
                      {muscle}
                    </Badge>
                  ))}
                </div>
              </CardContent>
            </Card>

            {/* Equipment */}
            <Card className="bg-gray-900 border-gray-800">
              <CardHeader>
                <CardTitle className="text-lg text-white">Equipment Needed</CardTitle>
              </CardHeader>
              <CardContent>
                <div className="space-y-2">
                  {workoutData.equipment.map((item, index) => (
                    <div key={index} className="flex items-center gap-2 text-gray-300">
                      <div className="w-2 h-2 bg-purple-400 rounded-full"></div>
                      {item}
                    </div>
                  ))}
                </div>
              </CardContent>
            </Card>

            {/* Workout Summary */}
            <Card className="bg-gray-900 border-gray-800">
              <CardHeader>
                <CardTitle className="text-lg text-white">Workout Summary</CardTitle>
              </CardHeader>
              <CardContent className="space-y-3">
                <div className="flex justify-between">
                  <span className="text-gray-400">Total Exercises</span>
                  <span className="text-white font-semibold">{workoutData.exercises.length}</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-gray-400">Estimated Time</span>
                  <span className="text-white font-semibold">45-60 min</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-gray-400">Difficulty</span>
                  <span className="text-white font-semibold">{workoutData.difficulty}</span>
                </div>
              </CardContent>
            </Card>
          </div>
        </div>
      </div>
    </div>
  )
}
