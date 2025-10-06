import Link from "next/link"
import RegisterForm from "../../components/forms/RegisterForm"

export default function RegisterPage() {
  return (
    <div className="min-h-[calc(100vh-4rem)] flex items-center justify-center px-4 py-8">
      <div className="card max-w-md w-full">
        <h1 className="text-3xl font-bold text-center mb-2">Join HelpBoard</h1>
        <p className="text-gray-600 text-center mb-6">Create your account to get started</p>

        <RegisterForm />

        <p className="text-center mt-6 text-gray-600">
          Already have an account?{" "}
          <Link href="/login" className="text-primary hover:underline font-medium">
            Login here
          </Link>
        </p>
      </div>
    </div>
  )
}
