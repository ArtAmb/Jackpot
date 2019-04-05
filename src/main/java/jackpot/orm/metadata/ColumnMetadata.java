package jackpot.orm.metadata;

import lombok.Builder;
import lombok.Value;

import javax.persistence.Column;

@Builder
@Value
public class ColumnMetadata {
    String columnName;
    ColumnType columnType;

    boolean notNull;
    boolean primaryKey;
}
