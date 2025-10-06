package com.helpboard.backend.repository;

import com.helpboard.backend.model.Item;
import com.helpboard.backend.model.enums.ItemStatus;
import com.helpboard.backend.model.enums.ItemType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {
    /**
     * Finds items based on various optional criteria.
     *
     * @param type       Optional ItemType.
     * @param category   Optional item category.
     * @param location   Optional owner's location.
     * @param ownerId    Optional owner's userId.
     * @param status     Optional ItemStatus.
     * @return A list of matching items.
     */
    @Query("SELECT i FROM Item i JOIN i.owner u WHERE " +
           "(:type IS NULL OR i.type = :type) AND " +
           "(:category IS NULL OR i.category = :category) AND " +
           "(:location IS NULL OR u.location = :location) AND " +
           "(:ownerId IS NULL OR u.userId = :ownerId) AND " +
           "(:status IS NULL OR i.status = :status)")
    List<Item> findByCriteria(
            @Param("type") ItemType type,
            @Param("category") String category,
            @Param("location") String location,
            @Param("ownerId") Long ownerId,
            @Param("status") ItemStatus status
    );

    List<Item> findByOwnerUserId(Long ownerId);
}