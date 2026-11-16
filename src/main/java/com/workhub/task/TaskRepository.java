package com.workhub.task;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TaskRepository extends JpaRepository<Task, Long> {

    /** Every filter is optional: a null parameter means "don't filter on it". */
    @Query("""
            SELECT t FROM Task t
            WHERE (:status IS NULL OR t.status = :status)
              AND (:assignee IS NULL OR t.assignee = :assignee)
              AND (:projectName IS NULL OR t.projectName = :projectName)
            """)
    Page<Task> search(@Param("status") TaskStatus status,
                      @Param("assignee") String assignee,
                      @Param("projectName") String projectName,
                      Pageable pageable);
}
