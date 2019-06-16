package jackpot.orm.repository;

import lombok.Builder;
import lombok.Value;

@Builder
@Value
public class Transaction {
    Long threadID;
    ConnectionManager connectionManager;
}
