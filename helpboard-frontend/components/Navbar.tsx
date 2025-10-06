"use client"

import { useEffect, useState } from "react"
import Link from "next/link"
import { useAuthStore } from "../store/authStore"
import { useRouter } from "next/navigation"

export default function Navbar() {
  const [mounted, setMounted] = useState(false)
  const { user, logout } = useAuthStore()
  const router = useRouter()

  useEffect(() => {
    setMounted(true)
  }, [])

  const handleLogout = () => {
    logout()
    router.push("/login")
  }

  return (
    <nav className="bg-white shadow-md sticky top-0 z-50">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex justify-between items-center h-16">
          <Link href="/" className="text-2xl font-bold text-primary">
            Neighborhood Help Board
          </Link>

          <div className="flex items-center gap-6">
            {!mounted ? (
              // Render placeholder during SSR to match initial client render
              <div className="flex items-center gap-6">
                <span className="text-gray-700 opacity-0">Loading...</span>
              </div>
            ) : user ? (
              <>
                <span className="text-gray-700">Hello, {user.name}</span>
                <Link href="/dashboard" className="text-gray-700 hover:text-primary transition-colors">
                  Dashboard
                </Link>
                <button onClick={handleLogout} className="text-gray-700 hover:text-primary transition-colors">
                  Logout
                </button>
              </>
            ) : (
              <>
                <Link href="/login" className="text-gray-700 hover:text-primary transition-colors">
                  Login
                </Link>
                <Link href="/register" className="btn-primary">
                  Register
                </Link>
              </>
            )}
          </div>
        </div>
      </div>
    </nav>
  )
}
