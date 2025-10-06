"use client"

import { useEffect, useState } from "react"
import { useParams, useRouter } from "next/navigation"
import { api } from "../../../lib/api"
import { useAuthStore } from "../../../store/authStore"
import Loader from "../../../components/Loader"
import Image from "next/image"
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

export default function ItemDetailPage() {
  const params = useParams()
  const router = useRouter()
  const { user } = useAuthStore()
  const [item, setItem] = useState<ItemResponse | null>(null)
  const [isLoading, setIsLoading] = useState(true)
  const [isRequesting, setIsRequesting] = useState(false)

  useEffect(() => {
    const fetchItem = async () => {
      try {
        console.log("[v0] Fetching item details for itemId:", params.itemId)
        const response = await api.get(`/items/${params.itemId}`)
        console.log("[v0] Item details fetched:", response.data)
        setItem(response.data)
      } catch (error) {
        console.error("[v0] Failed to fetch item:", error)
        toast.error("Failed to load item")
      } finally {
        setIsLoading(false)
      }
    }

    fetchItem()
  }, [params.itemId])

  const handleRequest = async () => {
    if (!user) {
      toast.error("Please login to request items")
      router.push("/login")
      return
    }

    setIsRequesting(true)
    try {
      console.log("[v0] Sending request for itemId:", params.itemId)
      const response = await api.post(`/items/${params.itemId}/request`)
      console.log("[v0] Request sent successfully:", response.data)
      toast.success("Request sent successfully!")
      router.push("/dashboard")
    } catch (error: any) {
      console.error("[v0] Failed to send request:", error)
      toast.error(error.response?.data?.message || "Failed to send request")
    } finally {
      setIsRequesting(false)
    }
  }

  if (isLoading) {
    return <Loader />
  }

  if (!item) {
    return (
      <div className="max-w-4xl mx-auto px-4 py-12 text-center">
        <h1 className="text-2xl font-bold text-gray-900">Item not found</h1>
      </div>
    )
  }

  const isOwner = user?.id === item.ownerId

  return (
    <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <div className="card">
        <div className="grid md:grid-cols-2 gap-8">
          <div className="relative w-full h-96 bg-gray-200 rounded-lg overflow-hidden">
            <Image
              src={item.imageUrl || "/placeholder.svg?height=400&width=400&query=marketplace+item"}
              alt={item.title}
              fill
              className="object-cover"
            />
          </div>

          <div className="flex flex-col justify-between">
            <div>
              <div className="flex items-start justify-between mb-4">
                <h1 className="text-3xl font-bold text-gray-900">{item.title}</h1>
                <span className="bg-primary/10 text-primary px-3 py-1 rounded-full text-sm font-medium">
                  {item.category}
                </span>
              </div>

              <p className="text-gray-600 mb-6 leading-relaxed">{item.description}</p>

              <div className="border-t pt-4">
                <p className="text-sm text-gray-500">Shared by</p>
                <p className="text-lg font-semibold text-gray-900">{item.ownerName}</p>
              </div>
            </div>

            <div className="mt-6">
              {isOwner ? (
                <div className="bg-blue-50 border border-blue-200 rounded-lg p-4 text-center">
                  <p className="text-blue-800 font-medium">This is your item</p>
                </div>
              ) : (
                <button onClick={handleRequest} disabled={isRequesting} className="btn-primary w-full text-lg py-3">
                  {isRequesting ? "Sending Request..." : "Request This Item"}
                </button>
              )}
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}
