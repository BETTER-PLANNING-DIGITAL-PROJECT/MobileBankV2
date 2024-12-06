package ibnk.repositories.internet;

import ibnk.models.internet.authorization.AppPaths;
import ibnk.models.internet.enums.PathType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.ArrayList;
import java.util.Optional;

public interface PathsRepository extends JpaRepository<AppPaths,Long> {
    Optional<AppPaths> findByIdAndType(Long id, PathType type);

    Optional<ArrayList<AppPaths>> findByType(PathType type);
    Optional<AppPaths> findByIdAndParent(Long id, AppPaths parent);

    Optional<AppPaths> findByIdAndModule(Long id, AppPaths module);
}
