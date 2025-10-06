import { z } from "zod"

export const loginSchema = z.object({
  email: z.string().email("Invalid email address"),
  password: z.string().min(6, "Password must be at least 6 characters"),
})

export const registerSchema = z.object({
  name: z.string().min(2, "Name must be at least 2 characters"),
  email: z.string().email("Invalid email address"),
  password: z.string().min(6, "Password must be at least 6 characters"),
  location: z.string().min(2, "Location must be at least 2 characters"),
})

export const addItemSchema = z.object({
  title: z.string().min(2, "Title must be at least 2 characters"),
  description: z.string().min(10, "Description must be at least 10 characters"),
  category: z.string().min(2, "Category is required"),
  type: z.enum(["BORROW", "LEND", "DONATE"], {
    errorMap: () => ({ message: "Please select an item type" })
  }),
  imageUrl: z.string().url("Must be a valid URL").optional().or(z.literal("")),
})

export type LoginFormData = z.infer<typeof loginSchema>
export type RegisterFormData = z.infer<typeof registerSchema>
export type AddItemFormData = z.infer<typeof addItemSchema>
