import { create } from "zustand"
import { api } from "../lib/api"

interface Message {
  id: number
  requestId: number
  senderId: number
  senderName: string
  content: string
  timestamp: string
}

interface ChatState {
  messages: Message[]
  loadMessages: (requestId: number) => Promise<void>
  addMessage: (message: Message) => void
  clearMessages: () => void
}

export const useChatStore = create<ChatState>((set) => ({
  messages: [],

  loadMessages: async (requestId: number) => {
    try {
      const response = await api.get(`/requests/${requestId}/messages`)
      set({ messages: response.data })
    } catch (error) {
      console.error("Failed to load messages:", error)
    }
  },

  addMessage: (message: Message) => {
    set((state) => ({
      messages: [...state.messages, message],
    }))
  },

  clearMessages: () => {
    set({ messages: [] })
  },
}))
