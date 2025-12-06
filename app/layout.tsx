import type { Metadata } from 'next'
import './globals.css'

export const metadata: Metadata = {
  title: '3D Model Virtual Try-On',
  description: 'AI-powered virtual try-on service',
}

export default function RootLayout({
  children,
}: {
  children: React.ReactNode
}) {
  return (
    <html lang="ko">
      <body>{children}</body>
    </html>
  )
}

