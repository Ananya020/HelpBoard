import { Client } from "@stomp/stompjs";
import SockJS from "sockjs-client";

export function createStompClient(token: string) {
  const client = new Client({
    // ✅ Use relative path, not full URL
    webSocketFactory: () => new SockJS("http://localhost:8080/ws"),
    connectHeaders: {
      Authorization: token?.startsWith("Bearer ")
        ? token
        : `Bearer ${token}`,
    },
    debug: (str) => console.log("[STOMP]", str),
    reconnectDelay: 5000,
    heartbeatIncoming: 4000,
    heartbeatOutgoing: 4000,
  });
  window.stompClient = client;
  return client;
}