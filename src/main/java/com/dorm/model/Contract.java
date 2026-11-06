package com.dorm.model;

import com.dorm.model.enums.ContractStatus;
import java.sql.Date;

/**
 * Entity representing the 'contract' table.
 *
 * Relationships:
 *  - N-to-1 with Student (signs, via student_id FK)
 *  - N-to-1 with Room    (assigned to, via room_id FK)
 *  - 1-to-N with Invoice (bills)
 */
public class Contract extends BaseEntity<Integer> {

    private String         studentId;
    private int            roomId;
    private Date           startDate;
    private Date           endDate;
    private ContractStatus contractStatus;

    public Contract() {
    }

    public Contract(Integer contractId, String studentId, int roomId,
                    Date startDate, Date endDate, ContractStatus contractStatus) {
        super(contractId);
        this.studentId      = studentId;
        this.roomId         = roomId;
        this.startDate      = startDate;
        this.endDate        = endDate;
        this.contractStatus = contractStatus;
    }

    // ── Getters & Setters ─────────────────────────────────────────────

    public Integer        getContractId()                                   { return getId(); }
    public void           setContractId(Integer id)                          { setId(id); }

    public String         getStudentId()                                    { return studentId; }
    public void           setStudentId(String studentId)                    { this.studentId = studentId; }

    public int            getRoomId()                                       { return roomId; }
    public void           setRoomId(int roomId)                             { this.roomId = roomId; }

    public Date           getStartDate()                                    { return startDate; }
    public void           setStartDate(Date startDate)                      { this.startDate = startDate; }

    public Date           getEndDate()                                      { return endDate; }
    public void           setEndDate(Date endDate)                          { this.endDate = endDate; }

    public ContractStatus getContractStatus()                               { return contractStatus; }
    public void           setContractStatus(ContractStatus contractStatus)  { this.contractStatus = contractStatus; }

    // ── BaseEntity contract ───────────────────────────────────────────

    @Override
    public String getTableName()    { return "contract"; }

    @Override
    public String getIdColumnName() { return "contract_id"; }

    @Override
    public String toString() {
        return "Contract{" +
                "contractId=" + getId() +
                ", studentId='" + studentId + '\'' +
                ", roomId=" + roomId +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", contractStatus=" + contractStatus +
                '}';
    }
}
