import Link from "next/link"
import Image from "next/image"

interface ItemCardProps {
  itemId: number
  title: string
  description: string
  category: string
  imageUrl?: string
  ownerName: string
}

export default function ItemCard({ itemId, title, description, category, imageUrl, ownerName }: ItemCardProps) {
  return (
    <Link href={`/items/${itemId}`}>
      <div className="card hover:shadow-lg transition-shadow cursor-pointer h-full">
        <div className="relative w-full h-48 mb-4 bg-gray-200 rounded-lg overflow-hidden">
          <Image
            src={imageUrl || "/placeholder.svg?height=200&width=300&query=marketplace+item"}
            alt={title}
            fill
            className="object-cover"
          />
        </div>
        <div className="space-y-2">
          <div className="flex justify-between items-start">
            <h3 className="text-lg font-semibold text-gray-900 line-clamp-1">{title}</h3>
            <span className="text-xs bg-primary/10 text-primary px-2 py-1 rounded-full whitespace-nowrap ml-2">
              {category}
            </span>
          </div>
          <p className="text-gray-600 text-sm line-clamp-2">{description}</p>
          <p className="text-xs text-gray-500">by {ownerName}</p>
        </div>
      </div>
    </Link>
  )
}

