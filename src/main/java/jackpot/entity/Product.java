package jackpot.entity;

import lombok.Builder;
import lombok.Value;

import javax.persistence.Entity;
import javax.persistence.Id;

@Builder
@Value
@Entity
public class Product {
    @Id
    Integer id;

    String name;
}
