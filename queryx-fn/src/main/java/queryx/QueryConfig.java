package queryx;

import io.quarkus.runtime.annotations.StaticInitSafe;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@ConfigMapping(prefix = "qx")
@StaticInitSafe
public interface QueryConfig {
    Optional<String> secretId();

    String queries64();

    @WithDefault("org.postgresql.Driver")
    String providerClassName();

    String jdbcURL();

    Optional<String> checkURL();

}
