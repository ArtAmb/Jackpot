package jackpot.orm.repository;

import lombok.Builder;
import lombok.Data;
import lombok.Value;

@Value
@Builder
public class JackpotRepositoryMetadata {
    private final String repositoryClassName;
    private final Class<?> repositoryClass;
    private final Class<?> tableClass;
    private final Class<?> idClass;
}
