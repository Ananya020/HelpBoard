"use client"

import { useEffect, useState } from "react"
import { api } from "../lib/api"
import ItemCard from "../components/ItemCard"
import Loader from "../components/Loader"
import toast from "react-hot-toast"

interface ItemResponse {
  itemId: number
  ownerId: number
  ownerName: string
  title: string
  description: string
  category: string
  imageUrl?: string
  status: "AVAILABLE" | "REQUESTED" | "APPROVED" | "COMPLETED"
  createdAt: string
}

export default function HomePage() {
  const [items, setItems] = useState<ItemResponse[]>([])
  const [isLoading, setIsLoading] = useState(true)

  useEffect(() => {
    const fetchItems = async () => {
      try {
        console.log("[v0] Fetching items from /items endpoint")
        const response = await api.get("/items")
        console.log("[v0] Items fetched successfully:", response.data)
        setItems(response.data)
      } catch (error) {
        console.error("[v0] Failed to fetch items:", error)
        toast.error("Failed to fetch items. Please check if the backend is running.")
      } finally {
        setIsLoading(false)
      }
    }

    fetchItems()
  }, [])

  if (isLoading) {
    return <Loader />
  }

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <div className="mb-8">
        <h1 className="text-4xl font-bold text-gray-900 mb-2">Community Marketplace</h1>
        <p className="text-gray-600">Discover items shared by your community</p>
      </div>

      {items.length === 0 ? (
        <div className="text-center py-12">
          <p className="text-gray-500 text-lg">No items available yet. Be the first to share!</p>
        </div>
      ) : (
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
          {items.map((item) => (
            <ItemCard key={item.itemId} {...item} />
          ))}
        </div>
      )}
    </div>
  )
}
