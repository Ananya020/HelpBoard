"use client"

import { useForm } from "react-hook-form"
import { zodResolver } from "@hookform/resolvers/zod"
import { loginSchema, type LoginFormData } from "../../lib/validation/authSchema"
import { useAuthStore } from "../../store/authStore"
import { useRouter } from "next/navigation"
import { useState } from "react"

export default function LoginForm() {
  const { login } = useAuthStore()
  const router = useRouter()
  const [isLoading, setIsLoading] = useState(false)

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<LoginFormData>({
    resolver: zodResolver(loginSchema),
  })

  const onSubmit = async (data: LoginFormData) => {
    setIsLoading(true)
    try {
      await login(data.email, data.password)
      router.push("/")
    } catch (error) {
      // Error handled in store
    } finally {
      setIsLoading(false)
    }
  }

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
      <div>
        <label htmlFor="email" className="block text-sm font-medium text-gray-700 mb-1">
          Email
        </label>
        <input id="email" type="email" {...register("email")} className="input-field" placeholder="your@email.com" />
        {errors.email && <p className="text-red-500 text-sm mt-1">{errors.email.message}</p>}
      </div>

      <div>
        <label htmlFor="password" className="block text-sm font-medium text-gray-700 mb-1">
          Password
        </label>
        <input id="password" type="password" {...register("password")} className="input-field" placeholder="••••••••" />
        {errors.password && <p className="text-red-500 text-sm mt-1">{errors.password.message}</p>}
      </div>

      <button type="submit" disabled={isLoading} className="btn-primary w-full">
        {isLoading ? "Logging in..." : "Login"}
      </button>
    </form>
  )
}
