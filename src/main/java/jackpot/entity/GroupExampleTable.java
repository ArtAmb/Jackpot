package jackpot.entity;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class GroupExampleTable {

    @Id
    Integer id;

    Integer intCol;
    String testStr;
}
