package jackpot.orm.metadata;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ForeignKeyRelation {

    RelationType type;
    String tableName;
    String columnName;
}
