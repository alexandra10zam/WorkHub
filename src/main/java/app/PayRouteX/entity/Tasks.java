package app.PayRouteX.entity;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/*
Field			Type	Required	Example
id		BIGSERIAL / BIGINT	Yes			1
title		VARCHAR(150)	Yes		Implement task API
description		TEXT		 No		Create POST and GET endpointsMore details
status			VARCHAR(30)	Yes			TODO
priority	VARCHAR(30)		Yes			HIGH
assignee	VARCHAR(100)	Yes			alexandra
project_name	VARCHAR(100) Yes		    WorkHub
due_date	       DATE	     No		2026-07-15
created_at	TIMESTAMP	    Yes		2026-06-04 10:30:00
updated_at	TIMESTAMP	    No		2026-06-04 12:00:00
*/
@Entity
@Table(name="tasks")
public class Tasks {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long  id;

    @Column (nullable=false, length = 150)
    private String  title;

    @Column (nullable=true, columnDefinition = "TEXT")
    private String  description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TaskStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TaskPriority priority;

    @Column(nullable = false, length = 100)
    private String assignee;

    @Column(nullable = false, length = 100)
    private String projectName;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

}
