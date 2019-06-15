package jackpot.entity;


import lombok.Builder;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

@Entity
@Builder
@Data
public class ExampleManySameFKTable {

    @Id
    Integer id;

    String stringCol;
    Integer intCol;
    Integer testCol;

    @ManyToOne
    GroupExampleTable group1;
    @ManyToOne
    GroupExampleTable group2;
}
