package com.example.straffic.board.repository;

import com.example.straffic.board.entity.CommentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentRepository extends JpaRepository<CommentEntity, Long> {
    @Query("select count(distinct c.board.id) from CommentEntity c where c.author.role like %:role%")
    long countBoardsWithAdminComments(@Param("role") String role);
}
