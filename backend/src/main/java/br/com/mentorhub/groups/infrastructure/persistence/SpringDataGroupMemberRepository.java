package br.com.mentorhub.groups.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface SpringDataGroupMemberRepository extends JpaRepository<GroupMemberJpaEntity, UUID> {

    List<GroupMemberJpaEntity> findByGroupIdOrderByJoinedAtAsc(UUID groupId);

    List<GroupMemberJpaEntity> findByGroupIdIn(Collection<UUID> groupIds);

    List<GroupMemberJpaEntity> findByUserId(UUID userId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("delete from GroupMemberJpaEntity m where m.groupId = :groupId")
    void deleteByGroupId(@Param("groupId") UUID groupId);
}
