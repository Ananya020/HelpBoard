"use client"

import { useEffect, useState } from "react"
import { api } from "../../lib/api"
import { useAuthStore } from "../../store/authStore"
import ProtectedRoute from "../../components/ProtectedRoute"
import Loader from "../../components/Loader"
import Link from "next/link"
import AddItemForm from "../../components/forms/AddItemForm"
import toast from "react-hot-toast"

interface Item {
  itemId: number
  ownerId: number
  ownerName: string
  title: string
  description: string
  category: string
  type: string
  imageUrl: string
  status: string
  createdAt: string
}

interface Request {
  requestId: number
  itemId: number
  itemTitle: string
  requesterId: number
  requesterName: string
  ownerId: number
  ownerName: string
  status: string
  timestamp: string
  approvedAt?: string
  closedAt?: string
}

export default function DashboardPage() {
  const { user } = useAuthStore()
  const [myItems, setMyItems] = useState<Item[]>([])
  const [myRequests, setMyRequests] = useState<Request[]>([])
  const [receivedRequests, setReceivedRequests] = useState<Request[]>([])
  const [isLoading, setIsLoading] = useState(true)
  const [showAddForm, setShowAddForm] = useState(false)
  const [fetched, setFetched] = useState(false) // prevents multiple fetches

  // ✅ Centralized fetchData
  const fetchData = async (currentUser: any) => {
    if (!currentUser) return
    setIsLoading(true)
    try {
      // 1️⃣ Fetch items
      console.log("[v0] Fetching items from /items endpoint")
      const itemsResponse = await api.get("/items")
      const allItems = itemsResponse.data
      const userItems = allItems.filter((item: any) => item.ownerId === currentUser.userId)
      console.log("[v0] Items fetched successfully:", allItems)
      setMyItems(userItems)

      // 2️⃣ Fetch requests (if backend is working)
      console.log("[v0] Fetching my requests and received requests")
      try {
        const [myReqRes, recReqRes] = await Promise.all([
          api.get(`/users/${currentUser.userId}/requests?role=requester`),
          api.get(`/users/${currentUser.userId}/requests?role=owner`)
        ])
        console.log("[v0] My requests:", myReqRes.data)
        console.log("[v0] Received requests:", recReqRes.data)
        setMyRequests(myReqRes.data || [])
        setReceivedRequests(recReqRes.data || [])
      } catch (reqError) {
        console.error("Failed to fetch requests:", reqError)
        setMyRequests([])
        setReceivedRequests([])
      }

    } catch (error) {
      console.error("Failed to fetch dashboard data:", error)
      setMyItems([])
      setMyRequests([])
      setReceivedRequests([])
    } finally {
      setIsLoading(false)
      setFetched(true) // mark fetch complete
    }
  }

  // ✅ Load user once from localStorage
  useEffect(() => {
    console.log("[v0] Loading user from localStorage...")
    useAuthStore.getState().loadUserFromLocalStorage()
  }, [])

  // ✅ Fetch dashboard data once user is available
  useEffect(() => {
    if (user && !fetched) {
      console.log("[v0] User loaded, fetching dashboard data...")
      fetchData(user)
    }
  }, [user, fetched])

  const handleStatusUpdate = async (requestId: number, status: "APPROVED" | "REJECTED") => {
    try {
      await api.patch(`/requests/${requestId}/status`, { status })
      toast.success(`Request ${status.toLowerCase()} successfully!`)
      fetchData(user)
    } catch (error: any) {
      toast.error(error.response?.data?.message || "Failed to update request")
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
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <h1 className="text-4xl font-bold text-gray-900 mb-8">My Dashboard</h1>

        {/* My Items Section */}
        <section className="mb-12">
          <div className="flex justify-between items-center mb-6">
            <h2 className="text-2xl font-bold text-gray-900">My Items</h2>
            <button
              onClick={() => setShowAddForm(!showAddForm)}
              className="btn-primary"
            >
              {showAddForm ? "Cancel" : "+ Add Item"}
            </button>
          </div>

          {showAddForm && (
            <div className="card mb-6">
              <h3 className="text-xl font-semibold mb-4">Add New Item</h3>
              <AddItemForm
                onSuccess={() => {
                  setShowAddForm(false)
                  fetchData(user)
                }}
                onCancel={() => setShowAddForm(false)}
              />
            </div>
          )}

          {myItems.length === 0 ? (
            <p className="text-gray-500">You haven't added any items yet.</p>
          ) : (
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
              {myItems.map((item) => (
                <div key={item.itemId} className="card">
                  <h3 className="text-lg font-semibold mb-2">{item.title}</h3>
                  <p className="text-gray-600 text-sm mb-2 line-clamp-2">
                    {item.description}
                  </p>
                  <div className="flex gap-2">
                    <span className="text-xs bg-primary/10 text-primary px-2 py-1 rounded-full">
                      {item.category}
                    </span>
                    <span className="text-xs bg-secondary/10 text-secondary px-2 py-1 rounded-full">
                      {item.type}
                    </span>
                  </div>
                </div>
              ))}
            </div>
          )}
        </section>

        {/* Requests Received */}
        <section className="mb-12">
          <h2 className="text-2xl font-bold text-gray-900 mb-6">Requests Received</h2>
          {receivedRequests.length === 0 ? (
            <p className="text-gray-500">No requests received yet.</p>
          ) : (
            <div className="space-y-4">
              {receivedRequests.map((request) => (
                <div key={request.requestId} className="card flex justify-between items-center">
                  <div>
                    <p className="font-semibold">Request #{request.requestId}</p>
                    <p className="text-sm text-gray-600">
                      Status:{" "}
                      <span
                        className={`font-medium ${
                          request.status === "APPROVED"
                            ? "text-green-600"
                            : request.status === "REJECTED"
                            ? "text-red-600"
                            : "text-yellow-600"
                        }`}
                      >
                        {request.status}
                      </span>
                    </p>
                    <p className="text-xs text-gray-500">
                      {new Date(request.timestamp).toLocaleDateString()}
                    </p>
                  </div>
                  <div className="flex gap-2">
                    {request.status === "PENDING" && (
                      <>
                        <button
                          onClick={() => handleStatusUpdate(request.requestId, "APPROVED")}
                          className="bg-green-500 text-white px-4 py-2 rounded-lg hover:bg-green-600 transition-colors"
                        >
                          Approve
                        </button>
                        <button
                          onClick={() => handleStatusUpdate(request.requestId, "REJECTED")}
                          className="bg-red-500 text-white px-4 py-2 rounded-lg hover:bg-red-600 transition-colors"
                        >
                          Reject
                        </button>
                      </>
                    )}
                    <Link href={`/chat/${request.requestId}`} className="btn-primary">
                      Chat
                    </Link>
                  </div>
                </div>
              ))}
            </div>
          )}
        </section>

        {/* My Requests */}
        <section>
          <h2 className="text-2xl font-bold text-gray-900 mb-6">My Requests</h2>
          {myRequests.length === 0 ? (
            <p className="text-gray-500">You haven't made any requests yet.</p>
          ) : (
            <div className="space-y-4">
              {myRequests.map((request) => (
                <div key={request.requestId} className="card flex justify-between items-center">
                  <div>
                    <p className="font-semibold">Request #{request.requestId}</p>
                    <p className="text-sm text-gray-600">Item: {request.itemTitle}</p>
                    <p className="text-sm text-gray-600">To: {request.ownerName}</p>
                    <p className="text-sm text-gray-600">
                      Status:{" "}
                      <span
                        className={`font-medium ${
                          request.status === "APPROVED"
                            ? "text-green-600"
                            : request.status === "REJECTED"
                            ? "text-red-600"
                            : "text-yellow-600"
                        }`}
                      >
                        {request.status}
                      </span>
                    </p>
                    <p className="text-xs text-gray-500">
                      {new Date(request.timestamp).toLocaleDateString()}
                    </p>
                  </div>
                  <Link href={`/chat/${request.requestId}`} className="btn-primary">
                    View Chat
                  </Link>
                </div>
              ))}
            </div>
          )}
        </section>
      </div>
    </ProtectedRoute>
  )
}
