package com.quronest.quronest_backend.repository;

import com.quronest.quronest_backend.model.table.AssistantChat;
import com.quronest.quronest_backend.model.table.AssistantChatRoom;
import com.quronest.quronest_backend.model.table.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface AssistantChatRepository extends JpaRepository<AssistantChat, UUID> {
    Page<AssistantChat> findByChatRoomAndUser(AssistantChatRoom chatRoom, User user, Pageable pageable);
}
