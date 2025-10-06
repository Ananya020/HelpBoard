import { Client } from "@stomp/stompjs"

export function createStompClient(token: string) {
  const client = new Client({
    brokerURL: "ws://localhost:8080/ws",
    connectHeaders: {
      Authorization: token?.startsWith("Bearer ") ? token : `Bearer ${token}`,
    },
    debug: (str) => {
      console.log("[STOMP]", str)
    },
    reconnectDelay: 5000,
    heartbeatIncoming: 4000,
    heartbeatOutgoing: 4000,
  })

  return client
}
