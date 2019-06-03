package jackpot.entity;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Value;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import java.util.List;

@Entity
@Data
@Builder
public class GroupExampleTable {

    @Id
    Integer id;

    Integer intCol;
    String testStr;

    @OneToMany
    List<ExampleTable> tables;
}
