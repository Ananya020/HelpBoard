"use client"

import type React from "react"

import { useAuthStore } from "../store/authStore"
import { useRouter } from "next/navigation"
import { useEffect } from "react"
import Loader from "../components/Loader"

export default function ProtectedRoute({ children }: { children: React.ReactNode }) {
  const { user, token } = useAuthStore()
  const router = useRouter()

  useEffect(() => {
    if (!token || !user) {
      router.push("/login")
    }
  }, [token, user, router])

  if (!token || !user) {
    return <Loader />
  }

  return <>{children}</>
}
