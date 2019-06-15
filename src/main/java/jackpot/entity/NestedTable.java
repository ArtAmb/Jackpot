package jackpot.entity;


import lombok.Builder;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

@Entity
@Builder
@Data
public class NestedTable {
    @Id
    Integer id;

    String tmp;

//    @ManyToOne
//    NestedTable nextTab;
}
