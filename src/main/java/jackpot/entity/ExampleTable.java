package jackpot.entity;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

@Entity
public class ExampleTable {

    @Id
    Integer id;

//    @OneToMany
    String stringCol;
    Integer intCol;
    Integer testCol;

    @ManyToOne
    GroupExampleTable groupExampleTable;
}
