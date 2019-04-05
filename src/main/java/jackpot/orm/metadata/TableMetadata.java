package jackpot.orm.metadata;


import lombok.Builder;
import lombok.Value;

import java.util.List;

@Builder
@Value
public class TableMetadata {

    String tableName;

    List<ColumnMetadata> columns;
}
