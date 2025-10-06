package com.helpboard.backend.repository;

import com.helpboard.backend.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    // Navigate the entity relationship: Message.request.requestId
    List<Message> findByRequest_RequestIdOrderByTimestampAsc(Long requestId);
}
