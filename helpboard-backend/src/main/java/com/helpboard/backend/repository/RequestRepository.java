package com.helpboard.backend.repository;

import com.helpboard.backend.model.Request;
import com.helpboard.backend.model.enums.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RequestRepository extends JpaRepository<Request, Long> {
    Optional<Request> findByRequestId(Long requestId);

    List<Request> findByRequesterUserId(Long requesterId);

    // Find requests where the item owner is the specified user
    List<Request> findByItemOwnerUserId(Long ownerId);

    // Find requests by a user, either as a requester or an item owner
    @Query("SELECT r FROM Request r WHERE r.requester.userId = :userId OR r.item.owner.userId = :userId")
    List<Request> findByRequesterOrOwnerId(@Param("userId") Long userId);

    // Find an active request for an item (e.g., PENDING or APPROVED)
    Optional<Request> findByItemItemIdAndStatusIn(Long itemId, List<RequestStatus> statuses);
}