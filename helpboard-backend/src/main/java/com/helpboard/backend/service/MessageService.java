package com.helpboard.backend.service;

import com.helpboard.backend.dto.message.ChatMessageDTO;
import com.helpboard.backend.dto.message.MessageResponse;
import com.helpboard.backend.exception.AccessDeniedException;
import com.helpboard.backend.exception.ResourceNotFoundException;
import com.helpboard.backend.model.Message;
import com.helpboard.backend.model.Request;
import com.helpboard.backend.model.User;
import com.helpboard.backend.model.enums.RequestStatus;
import com.helpboard.backend.repository.MessageRepository;
import com.helpboard.backend.repository.RequestRepository;
import com.helpboard.backend.repository.UserRepository;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing chat messages within requests.
 */
@Service
public class MessageService {

    private final MessageRepository messageRepository;
    private final RequestRepository requestRepository;
    private final UserRepository userRepository;
    private final AuthService authService;
    private final ModelMapper modelMapper;

    public MessageService(MessageRepository messageRepository, RequestRepository requestRepository,
                          UserRepository userRepository, AuthService authService, ModelMapper modelMapper) {
        this.messageRepository = messageRepository;
        this.requestRepository = requestRepository;
        this.userRepository = userRepository;
        this.authService = authService;
        this.modelMapper = modelMapper;
    }

    /**
     * Saves a new chat message to the database.
     *
     * @param requestId The ID of the request this message belongs to.
     * @param senderId  The ID of the user sending the message.
     * @param messageText The content of the message.
     * @return The saved Message entity.
     * @throws ResourceNotFoundException if the request or sender is not found.
     * @throws AccessDeniedException     if the chat for this request is not active (i.e., request not APPROVED).
     */
    @Transactional
    public Message saveMessage(Long requestId, Long senderId, String messageText) {
        Request request = requestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Request not found with ID: " + requestId));
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new ResourceNotFoundException("Sender user not found with ID: " + senderId));

        // Only allow chat for APPROVED requests
        if (request.getStatus() != RequestStatus.APPROVED) {
            throw new AccessDeniedException("Chat is only available for APPROVED requests.");
        }

        // Ensure the sender is a participant of the request
        if (!request.getRequester().getUserId().equals(senderId) &&
            !request.getItem().getOwner().getUserId().equals(senderId)) {
            throw new AccessDeniedException("You are not a participant in this request's chat.");
        }

        Message message = new Message();
        message.setRequest(request);
        message.setSender(sender);
        message.setMessageText(messageText);
        message.setTimestamp(LocalDateTime.now());

        return messageRepository.save(message);
    }

    /**
     * Retrieves the chat history for a given request.
     *
     * @param requestId The ID of the request.
     * @return A list of MessageResponse DTOs, ordered by timestamp.
     * @throws ResourceNotFoundException if the request is not found.
     * @throws AccessDeniedException     if the current user is not a participant of the request.
     */
    public List<MessageResponse> getChatHistory(Long requestId) {
        Request request = requestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Request not found with ID: " + requestId));

        Long currentUserId = authService.getCurrentUserId();
        if (!request.getRequester().getUserId().equals(currentUserId) &&
            !request.getItem().getOwner().getUserId().equals(currentUserId)) {
            throw new AccessDeniedException("You are not a participant in this request's chat.");
        }

        List<Message> messages = messageRepository.findByRequest_RequestIdOrderByTimestampAsc(requestId);
        return messages.stream()
                .map(this::mapMessageToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Helper method to map a Message entity to a MessageResponse DTO.
     *
     * @param message The Message entity.
     * @return The MessageResponse DTO.
     */
    private MessageResponse mapMessageToResponse(Message message) {
        MessageResponse response = modelMapper.map(message, MessageResponse.class);
        response.setRequestId(message.getRequest().getRequestId());
        response.setSenderId(message.getSender().getUserId());
        response.setSenderName(message.getSender().getName());
        return response;
    }

    /**
     * Helper method to map a Message entity to a ChatMessageDTO for WebSocket broadcast.
     *
     * @param message The Message entity.
     * @return The ChatMessageDTO.
     */
    public ChatMessageDTO mapMessageToChatMessageDTO(Message message) {
        ChatMessageDTO dto = modelMapper.map(message, ChatMessageDTO.class);
        dto.setRequestId(message.getRequest().getRequestId());
        dto.setSenderId(message.getSender().getUserId());
        dto.setSenderName(message.getSender().getName());
        return dto;
    }
}