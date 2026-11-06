package com.dorm.model;

import com.dorm.model.enums.RequestStatus;
import java.sql.Timestamp;

/**
 * Entity representing the 'request' table.
 *
 * Relationships:
 *  - N-to-1 with Student (submits, via student_id FK)
 *  - N-to-1 with Room    (reported for, via room_id FK)
 *  - N-to-1 with Account (resolves, via resolved_by FK → account_id)
 */
public class Request extends BaseEntity<Integer> {

    private String        studentId;
    private int           roomId;
    private String        title;
    private String        description;
    private RequestStatus requestStatus;
    private Timestamp     requestedTime;
    private Timestamp     resolvedTime;
    private Integer       resolvedBy;      // nullable — FK to account.account_id

    public Request() {
    }

    public Request(Integer requestId, String studentId, int roomId,
                   String title, String description, RequestStatus requestStatus,
                   Timestamp requestedTime, Timestamp resolvedTime, Integer resolvedBy) {
        super(requestId);
        this.studentId     = studentId;
        this.roomId        = roomId;
        this.title         = title;
        this.description   = description;
        this.requestStatus = requestStatus;
        this.requestedTime = requestedTime;
        this.resolvedTime  = resolvedTime;
        this.resolvedBy    = resolvedBy;
    }

    // ── Getters & Setters ─────────────────────────────────────────────

    public Integer       getRequestId()                                { return getId(); }
    public void          setRequestId(Integer id)                       { setId(id); }

    public String        getStudentId()                                { return studentId; }
    public void          setStudentId(String studentId)                { this.studentId = studentId; }

    public int           getRoomId()                                   { return roomId; }
    public void          setRoomId(int roomId)                         { this.roomId = roomId; }

    public String        getTitle()                                    { return title; }
    public void          setTitle(String title)                        { this.title = title; }

    public String        getDescription()                              { return description; }
    public void          setDescription(String description)            { this.description = description; }

    public RequestStatus getRequestStatus()                            { return requestStatus; }
    public void          setRequestStatus(RequestStatus requestStatus) { this.requestStatus = requestStatus; }

    public Timestamp     getRequestedTime()                            { return requestedTime; }
    public void          setRequestedTime(Timestamp requestedTime)     { this.requestedTime = requestedTime; }

    public Timestamp     getResolvedTime()                             { return resolvedTime; }
    public void          setResolvedTime(Timestamp resolvedTime)       { this.resolvedTime = resolvedTime; }

    public Integer       getResolvedBy()                               { return resolvedBy; }
    public void          setResolvedBy(Integer resolvedBy)             { this.resolvedBy = resolvedBy; }

    // ── BaseEntity contract ───────────────────────────────────────────

    @Override
    public String getTableName()    { return "request"; }

    @Override
    public String getIdColumnName() { return "request_id"; }

    @Override
    public String toString() {
        return "Request{" +
                "requestId=" + getId() +
                ", studentId='" + studentId + '\'' +
                ", roomId=" + roomId +
                ", title='" + title + '\'' +
                ", requestStatus=" + requestStatus +
                '}';
    }
}
