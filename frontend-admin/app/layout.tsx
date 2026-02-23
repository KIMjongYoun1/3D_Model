import type { Metadata } from 'next'
import './globals.css'
import Header from "@/components/common/Header"
import Footer from "@/components/common/Footer"

export const metadata: Metadata = {
  title: 'Quantum Studio | AI 3D Data Mapping',
  description: 'High-end AI-powered 3D data visualization and mapping studio.',
}

export default function RootLayout({
  children,
}: {
  children: React.ReactNode
}) {
  return (
    <html lang="ko" className="h-full">
      <body className="min-h-screen h-full flex flex-col bg-[#f8f9fa] overflow-x-hidden">
        <Header />
        <main className="flex-1 flex flex-col min-h-0">
          {children}
        </main>
        <Footer />
      </body>
    </html>
  )
}
