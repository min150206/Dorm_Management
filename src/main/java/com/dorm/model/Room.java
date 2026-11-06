package com.dorm.model;

import com.dorm.model.enums.RoomStatus;

/**
 * Entity representing the 'room' table.
 *
 * Relationships:
 *  - N-to-1 with RoomType (categorizes, via room_type_id FK)
 *  - 1-to-N with Request  (reported for)
 *  - 1-to-N with Contract (assigned to)
 */
public class Room extends BaseEntity<Integer> {

    private int        roomTypeId;
    private String     roomNumber;
    private String     building;
    private int        currentOccupancy;
    private RoomStatus roomStatus;

    public Room() {
    }

    public Room(Integer roomId, int roomTypeId, String roomNumber,
                String building, int currentOccupancy, RoomStatus roomStatus) {
        super(roomId);
        this.roomTypeId       = roomTypeId;
        this.roomNumber       = roomNumber;
        this.building         = building;
        this.currentOccupancy = currentOccupancy;
        this.roomStatus       = roomStatus;
    }

    // ── Getters & Setters ─────────────────────────────────────────────

    public Integer    getRoomId()                                { return getId(); }
    public void       setRoomId(Integer id)                      { setId(id); }

    public int        getRoomTypeId()                            { return roomTypeId; }
    public void       setRoomTypeId(int roomTypeId)              { this.roomTypeId = roomTypeId; }

    public String     getRoomNumber()                            { return roomNumber; }
    public void       setRoomNumber(String roomNumber)           { this.roomNumber = roomNumber; }

    public String     getBuilding()                              { return building; }
    public void       setBuilding(String building)               { this.building = building; }

    public int        getCurrentOccupancy()                      { return currentOccupancy; }
    public void       setCurrentOccupancy(int currentOccupancy)  { this.currentOccupancy = currentOccupancy; }

    public RoomStatus getRoomStatus()                            { return roomStatus; }
    public void       setRoomStatus(RoomStatus roomStatus)       { this.roomStatus = roomStatus; }

    // ── BaseEntity contract ───────────────────────────────────────────

    @Override
    public String getTableName()    { return "room"; }

    @Override
    public String getIdColumnName() { return "room_id"; }

    @Override
    public String toString() {
        return "Room{" +
                "roomId=" + getId() +
                ", roomTypeId=" + roomTypeId +
                ", roomNumber='" + roomNumber + '\'' +
                ", building='" + building + '\'' +
                ", currentOccupancy=" + currentOccupancy +
                ", roomStatus=" + roomStatus +
                '}';
    }
}
