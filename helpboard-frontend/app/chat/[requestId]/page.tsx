"use client"

import type React from "react"

import { useEffect, useState, useRef } from "react"
import { useParams } from "next/navigation"
import { useChatStore } from "../../../store/chatStore"
import { useAuthStore } from "../../../store/authStore"
import ProtectedRoute from "../../../components/ProtectedRoute"
import ChatBubble from "../../../components/ChatBubble"
import Loader from "../../../components/Loader"
import { createStompClient } from "../../../lib/stompClient"
import type { Client } from "@stomp/stompjs"
import toast from "react-hot-toast"

export default function ChatPage() {
  const params = useParams()
  const requestId = Number(params.requestId)
  const { user, token } = useAuthStore()
  const { messages, loadMessages, addMessage, clearMessages } = useChatStore()
  const [isLoading, setIsLoading] = useState(true)
  const [messageInput, setMessageInput] = useState("")
  const [stompClient, setStompClient] = useState<Client | null>(null)
  const messagesEndRef = useRef<HTMLDivElement>(null)

  useEffect(() => {
    const initChat = async () => {
      try {
        await loadMessages(requestId)

        if (token) {
          const client = createStompClient(token)

          client.onConnect = () => {
            console.log("[v0] STOMP connected")

            client.subscribe(`/topic/requests/${requestId}`, (message) => {
              const newMessage = JSON.parse(message.body)
              console.log("[v0] Received message:", newMessage)
              addMessage(newMessage)
            })
          }

          client.onStompError = (frame) => {
            console.error("[v0] STOMP error:", frame)
            toast.error("Connection error. Please refresh the page.")
          }

          client.activate()
          setStompClient(client)
        }
      } catch (error) {
        console.error("Failed to initialize chat:", error)
        toast.error("Failed to load chat")
      } finally {
        setIsLoading(false)
      }
    }

    initChat()

    return () => {
      if (stompClient) {
        stompClient.deactivate()
      }
      clearMessages()
    }
  }, [requestId, token])

  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: "smooth" })
  }, [messages])

  const handleSendMessage = () => {
    if (!messageInput.trim() || !stompClient || !stompClient.connected) {
      return
    }

    try {
      stompClient.publish({
        destination: `/app/requests/${requestId}/send`,
        body: JSON.stringify({ content: messageInput }),
      })
      setMessageInput("")
    } catch (error) {
      console.error("Failed to send message:", error)
      toast.error("Failed to send message")
    }
  }

  const handleKeyPress = (e: React.KeyboardEvent) => {
    if (e.key === "Enter" && !e.shiftKey) {
      e.preventDefault()
      handleSendMessage()
    }
  }

  if (isLoading) {
    return (
      <ProtectedRoute>
        <Loader />
      </ProtectedRoute>
    )
  }

  return (
    <ProtectedRoute>
      <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div className="card h-[calc(100vh-12rem)] flex flex-col">
          <div className="border-b pb-4 mb-4">
            <h1 className="text-2xl font-bold text-gray-900">Chat - Request #{requestId}</h1>
            <p className="text-sm text-gray-500">{stompClient?.connected ? "🟢 Connected" : "🔴 Disconnected"}</p>
          </div>

          <div className="flex-1 overflow-y-auto mb-4 space-y-2">
            {messages.length === 0 ? (
              <p className="text-center text-gray-500 py-8">No messages yet. Start the conversation!</p>
            ) : (
              messages.map((message) => (
                <ChatBubble
                  key={message.id}
                  content={message.content}
                  senderName={message.senderName}
                  timestamp={message.timestamp}
                  isOwnMessage={message.senderId === user?.id}
                />
              ))
            )}
            <div ref={messagesEndRef} />
          </div>

          <div className="flex gap-2">
            <input
              type="text"
              value={messageInput}
              onChange={(e) => setMessageInput(e.target.value)}
              onKeyPress={handleKeyPress}
              placeholder="Type your message..."
              className="input-field flex-1"
              disabled={!stompClient?.connected}
            />
            <button
              onClick={handleSendMessage}
              disabled={!messageInput.trim() || !stompClient?.connected}
              className="btn-primary px-6"
            >
              Send
            </button>
          </div>
        </div>
      </div>
    </ProtectedRoute>
  )
}
