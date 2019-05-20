package jackpot.entity;

import lombok.Builder;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

@Entity
@Data
@Builder
public class TestTmpTable {

    @Id
    Integer id;

    String stringCol;
    Integer intCol;
    Integer testCol;
}
