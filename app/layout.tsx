import type { Metadata } from 'next'
import './globals.css'
import Header from '@/components/common/Header'
import Footer from '@/components/common/Footer'

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
    <html lang="ko">
      <body className="min-h-screen flex flex-col bg-[#f8f9fa]">
        <Header />
        <main className="flex-1 flex flex-col">
          {children}
        </main>
        <Footer />
      </body>
    </html>
  )
}
