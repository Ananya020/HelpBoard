"use client"

import { useForm } from "react-hook-form"
import { zodResolver } from "@hookform/resolvers/zod"
import { registerSchema, type RegisterFormData } from "../../lib/validation/authSchema"
import { useAuthStore } from "../../store/authStore"
import { useRouter } from "next/navigation"
import { useState } from "react"

export default function RegisterForm() {
  const { register: registerUser } = useAuthStore()
  const router = useRouter()
  const [isLoading, setIsLoading] = useState(false)

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<RegisterFormData>({
    resolver: zodResolver(registerSchema),
  })

  const onSubmit = async (data: RegisterFormData) => {
    setIsLoading(true)
    try {
      await registerUser(data.name, data.email, data.password, data.location)
      router.push("/login")
    } catch (error) {
      // Error handled in store
    } finally {
      setIsLoading(false)
    }
  }

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
      <div>
        <label htmlFor="name" className="block text-sm font-medium text-gray-700 mb-1">
          Name
        </label>
        <input id="name" type="text" {...register("name")} className="input-field" placeholder="John Doe" />
        {errors.name && <p className="text-red-500 text-sm mt-1">{errors.name.message}</p>}
      </div>

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

      <div>
        <label htmlFor="location" className="block text-sm font-medium text-gray-700 mb-1">
          Location
        </label>
        <input id="location" type="text" {...register("location")} className="input-field" placeholder="New York, NY" />
        {errors.location && <p className="text-red-500 text-sm mt-1">{errors.location.message}</p>}
      </div>

      <button type="submit" disabled={isLoading} className="btn-primary w-full">
        {isLoading ? "Registering..." : "Register"}
      </button>
    </form>
  )
}
