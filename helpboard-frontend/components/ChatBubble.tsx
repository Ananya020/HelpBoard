interface ChatBubbleProps {
  content: string
  senderName: string
  timestamp: string
  isOwnMessage: boolean
}

export default function ChatBubble({ content, senderName, timestamp, isOwnMessage }: ChatBubbleProps) {
  return (
    <div className={`flex ${isOwnMessage ? "justify-end" : "justify-start"} mb-4`}>
      <div
        className={`max-w-[70%] ${isOwnMessage ? "bg-primary text-white" : "bg-gray-200 text-gray-900"} rounded-lg px-4 py-2`}
      >
        {!isOwnMessage && <p className="text-xs font-semibold mb-1">{senderName}</p>}
        <p className="text-sm">{content}</p>
        <p className={`text-xs mt-1 ${isOwnMessage ? "text-blue-100" : "text-gray-500"}`}>
          {new Date(timestamp).toLocaleTimeString()}
        </p>
      </div>
    </div>
  )
}
