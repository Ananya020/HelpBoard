import { create } from "zustand"
import { api } from "../lib/api"
import toast from "react-hot-toast"

interface User {
  id: number
  name: string
  email: string
  location: string
  items: any[]
  requestsMade: any[]
  requestsReceived: any[]
}

interface AuthState {
  token: string | null
  user: User | null
  login: (email: string, password: string) => Promise<void>
  register: (name: string, email: string, password: string, location: string) => Promise<void>
  logout: () => void
  loadUserFromLocalStorage: () => void
}

export const useAuthStore = create<AuthState>((set) => ({
  token: null,
  user: null,

  login: async (email: string, password: string) => {
    try {
      const response = await api.post("/auth/login", { email, password })
      const { token, user } = response.data

      localStorage.setItem("token", token)
      localStorage.setItem("user", JSON.stringify(user))

      set({ token, user })
      toast.success("Login successful!")
    } catch (error: any) {
      toast.error(error.response?.data?.message || "Login failed")
      throw error
    }
  },

  register: async (name: string, email: string, password: string, location: string) => {
    try {
      await api.post("/auth/register", { name, email, password, location })
      toast.success("Registration successful! Please login.")
    } catch (error: any) {
      toast.error(error.response?.data?.message || "Registration failed")
      throw error
    }
  },

  logout: () => {
    localStorage.removeItem("token")
    localStorage.removeItem("user")
    set({ token: null, user: null })
    toast.success("Logged out successfully")
  },

  loadUserFromLocalStorage: () => {
    if (typeof window !== "undefined") {
      const token = localStorage.getItem("token")
      const userStr = localStorage.getItem("user")

      if (token && userStr) {
        try {
          const user = JSON.parse(userStr)
          set({ token, user })
        } catch (error) {
          localStorage.removeItem("token")
          localStorage.removeItem("user")
        }
      }
    }
  },
}))
