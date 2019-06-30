package jackpot.entity;

import lombok.Builder;
import lombok.Data;
import lombok.Value;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import java.util.List;

@Entity
@Value
@Builder
public class Invoice {
    @Id
    Integer id;

    String invoiceNumber;

    @ManyToMany
    List<Product> invoiceProducts;
}
