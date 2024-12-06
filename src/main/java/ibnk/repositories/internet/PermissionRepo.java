package ibnk.repositories.internet;

import ibnk.models.internet.authorization.AppPaths;
import ibnk.models.internet.authorization.Permissions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface PermissionRepo extends JpaRepository<Permissions,Long> {
    Optional<Set<Permissions>> findPermissionsByIdIn(Set<Long> id);

    @Query("SELECT P from Permissions P WHERE P.path = NULL")
    List<Permissions> findOpenPermissions();

    Optional<Set<Permissions>> findByPath(AppPaths path);
}
