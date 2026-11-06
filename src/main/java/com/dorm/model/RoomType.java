package com.dorm.model;

/**
 * Entity representing the 'room_type' table.
 *
 * Relationships:
 *  - 1-to-N with Room (categorizes)
 */
public class RoomType extends BaseEntity<Integer> {

    private String typeName;
    private int    capacity;
    private long   price;

    public RoomType() {
    }

    public RoomType(Integer roomTypeId, String typeName, int capacity, long price) {
        super(roomTypeId);
        this.typeName = typeName;
        this.capacity = capacity;
        this.price    = price;
    }

    // ── Getters & Setters ─────────────────────────────────────────────

    public Integer getRoomTypeId()                  { return getId(); }
    public void    setRoomTypeId(Integer id)         { setId(id); }

    public String  getTypeName()                    { return typeName; }
    public void    setTypeName(String typeName)     { this.typeName = typeName; }

    public int     getCapacity()                    { return capacity; }
    public void    setCapacity(int capacity)        { this.capacity = capacity; }

    public long    getPrice()                       { return price; }
    public void    setPrice(long price)             { this.price = price; }

    // ── BaseEntity contract ───────────────────────────────────────────

    @Override
    public String getTableName()    { return "room_type"; }

    @Override
    public String getIdColumnName() { return "room_type_id"; }

    @Override
    public String toString() {
        return "RoomType{" +
                "roomTypeId=" + getId() +
                ", typeName='" + typeName + '\'' +
                ", capacity=" + capacity +
                ", price=" + price +
                '}';
    }
}
