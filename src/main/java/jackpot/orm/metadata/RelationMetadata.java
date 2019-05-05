package jackpot.orm.metadata;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class RelationMetadata {

    RelationType type;

    String targetColumnName;
    String targetTableName;

    String sourceColumnName;
    String sourceTableName;

    boolean targetColumnNotNull;

}
