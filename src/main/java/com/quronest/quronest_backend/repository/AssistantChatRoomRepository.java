package com.quronest.quronest_backend.repository;

import com.quronest.quronest_backend.model.table.AssistantChatRoom;
import com.quronest.quronest_backend.model.table.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface AssistantChatRoomRepository extends JpaRepository<AssistantChatRoom, UUID> {
    AssistantChatRoom findByIdAndUser(UUID id, User user);
}
