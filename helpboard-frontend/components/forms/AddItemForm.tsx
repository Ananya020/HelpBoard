"use client"

import { useForm } from "react-hook-form"
import { zodResolver } from "@hookform/resolvers/zod"
import { addItemSchema, type AddItemFormData } from "../../lib/validation/authSchema"
import { api } from "../../lib/api"
import { useState } from "react"
import toast from "react-hot-toast"

interface AddItemFormProps {
  onSuccess: () => void
  onCancel: () => void
}

export default function AddItemForm({ onSuccess, onCancel }: AddItemFormProps) {
  const [isLoading, setIsLoading] = useState(false)

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<AddItemFormData>({
    resolver: zodResolver(addItemSchema),
  })

  const onSubmit = async (data: AddItemFormData) => {
    setIsLoading(true)
    try {
      await api.post("/items", {
        ...data,
        imageUrl: data.imageUrl || "/marketplace-item.png",
      })
      toast.success("Item added successfully!")
      onSuccess()
    } catch (error: any) {
      toast.error(error.response?.data?.message || "Failed to add item")
    } finally {
      setIsLoading(false)
    }
  }

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
      <div>
        <label htmlFor="title" className="block text-sm font-medium text-gray-700 mb-1">
          Item Title
        </label>
        <input id="title" type="text" {...register("title")} className="input-field" placeholder="e.g., Bicycle" />
        {errors.title && <p className="text-red-500 text-sm mt-1">{errors.title.message}</p>}
      </div>

      <div>
        <label htmlFor="description" className="block text-sm font-medium text-gray-700 mb-1">
          Description
        </label>
        <textarea
          id="description"
          {...register("description")}
          className="input-field"
          rows={4}
          placeholder="Describe your item..."
        />
        {errors.description && <p className="text-red-500 text-sm mt-1">{errors.description.message}</p>}
      </div>

      <div>
        <label htmlFor="category" className="block text-sm font-medium text-gray-700 mb-1">
          Category
        </label>
        <input
          id="category"
          type="text"
          {...register("category")}
          className="input-field"
          placeholder="e.g., Sports, Electronics"
        />
        {errors.category && <p className="text-red-500 text-sm mt-1">{errors.category.message}</p>}
      </div>

      <div>
        <label htmlFor="type" className="block text-sm font-medium text-gray-700 mb-1">
          Item Type
        </label>
        <select id="type" {...register("type")} className="input-field">
          <option value="">Select type...</option>
          <option value="BORROW">Borrow</option>
          <option value="LEND">Lend</option>
          <option value="DONATE">Donate</option>
        </select>
        {errors.type && <p className="text-red-500 text-sm mt-1">{errors.type.message}</p>}
      </div>

      <div>
        <label htmlFor="imageUrl" className="block text-sm font-medium text-gray-700 mb-1">
          Image URL (optional)
        </label>
        <input
          id="imageUrl"
          type="text"
          {...register("imageUrl")}
          className="input-field"
          placeholder="https://example.com/image.jpg"
        />
        {errors.imageUrl && <p className="text-red-500 text-sm mt-1">{errors.imageUrl.message}</p>}
      </div>

      <div className="flex gap-3">
        <button type="submit" disabled={isLoading} className="btn-primary flex-1">
          {isLoading ? "Adding..." : "Add Item"}
        </button>
        <button type="button" onClick={onCancel} className="btn-secondary flex-1">
          Cancel
        </button>
      </div>
    </form>
  )
}
