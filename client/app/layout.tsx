import type { Metadata } from 'next'
import './globals.css'

export const metadata: Metadata = {
  title: 'Workout Extract',
  description: 'This application helps extract the full workout routine from youtube videos using LLMs'
}

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode
}>) {
  return (
    <html lang="en">
      <body>{children}</body>
    </html>
  )
}
