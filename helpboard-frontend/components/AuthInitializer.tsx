"use client"

import { useEffect } from "react"
import { useAuthStore } from "../store/authStore"

export default function AuthInitializer() {
  const loadUserFromLocalStorage = useAuthStore((state) => state.loadUserFromLocalStorage)

  useEffect(() => {
    loadUserFromLocalStorage()
  }, [loadUserFromLocalStorage])

  return null
}
