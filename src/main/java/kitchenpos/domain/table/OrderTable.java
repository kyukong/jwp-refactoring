package kitchenpos.domain.table;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class OrderTable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "table_group_id")
    private Long tableGroupId;
    
    @Column(nullable = false)
    private int numberOfGuests;
    
    @Column(nullable = false)
    private boolean empty;

    protected OrderTable() {
    }

    public OrderTable(final int numberOfGuests, final boolean empty) {
        this(null, null, numberOfGuests, empty);
    }

    public OrderTable(final Long id, final Long tableGroupId, final int numberOfGuests, final boolean empty) {
        this.id = id;
        this.tableGroupId = tableGroupId;
        this.numberOfGuests = numberOfGuests;
        this.empty = empty;
    }

    public static OrderTable updated(final Long id, final int numberOfGuests, final boolean empty) {
        return new OrderTable(id, null, numberOfGuests, empty);
    }

    public void changeEmpty(final boolean empty) {
        this.empty = empty;
    }

    public void changeNumberOfGuests(final int numberOfGuests) {
        validateNumberOfGuestsIsHigherZero(numberOfGuests);
        validateTableIsNotEmpty();
        this.numberOfGuests = numberOfGuests;
    }

    public void group(final Long tableGroupId) {
        this.tableGroupId = tableGroupId;
        empty = false;
    }

    private void validateNumberOfGuestsIsHigherZero(final int numberOfGuests) {
        if (numberOfGuests < 0) {
            throw new IllegalArgumentException(String.format("손님의 수는 0명 이하일 수 없습니다. [%s]", numberOfGuests));
        }
    }

    private void validateTableIsNotEmpty() {
        if (empty) {
            throw new IllegalArgumentException("테이블이 비어있습니다.");
        }
    }

    public Long getId() {
        return id;
    }

    public Long getTableGroupId() {
        return tableGroupId;
    }

    public int getNumberOfGuests() {
        return numberOfGuests;
    }

    public boolean isEmpty() {
        return empty;
    }

    public boolean isNotEmpty() {
        return !empty;
    }
}
