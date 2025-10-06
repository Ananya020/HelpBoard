"use client"

import { useState } from "react"
import { api } from "../../lib/api"
import toast from "react-hot-toast"

export default function HealthPage() {
  const [response, setResponse] = useState<any>(null)
  const [isLoading, setIsLoading] = useState(false)

  const checkHealth = async () => {
    setIsLoading(true)
    try {
      console.log("[v0] Checking backend health at /health")
      const res = await api.get("/health")
      console.log("[v0] Health check response:", res.data)
      setResponse(res.data)
      toast.success("Backend is healthy!")
    } catch (error: any) {
      console.error("[v0] Health check failed:", error)
      setResponse({ error: error.message })
      toast.error("Backend health check failed")
    } finally {
      setIsLoading(false)
    }
  }

  return (
    <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
      <div className="card">
        <h1 className="text-3xl font-bold text-gray-900 mb-6">Backend Health Check</h1>
        <p className="text-gray-600 mb-6">
          Click the button below to test the connection to the backend API at{" "}
          <code className="bg-gray-100 px-2 py-1 rounded text-sm">http://localhost:8080/health</code>
        </p>

        <button onClick={checkHealth} disabled={isLoading} className="btn-primary mb-6">
          {isLoading ? "Checking..." : "Check Backend Health"}
        </button>

        {response && (
          <div className="bg-gray-50 border border-gray-200 rounded-lg p-4">
            <h2 className="text-lg font-semibold text-gray-900 mb-2">Response:</h2>
            <pre className="bg-white p-4 rounded border border-gray-200 overflow-auto">
              {JSON.stringify(response, null, 2)}
            </pre>
          </div>
        )}
      </div>
    </div>
  )
}
