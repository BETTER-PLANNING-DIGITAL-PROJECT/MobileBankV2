package ibnk.repositories.internet;

import ibnk.models.internet.authorization.AppPaths;
import ibnk.models.internet.authorization.Roles;
//import org.apache.ibatis.annotations.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public interface RolesRepo extends JpaRepository<Roles,Long> {
    @Query("SELECT r.appPaths FROM UserEntity u JOIN Roles r ON u.role.id = r.id  WHERE u.id = ?1")
    Optional<ArrayList<AppPaths>> findAllUserAuthorizedPaths(@Param("userId") Long userId);

    @Query("SELECT p FROM AppPaths p  WHERE p.module.id = ?1")
    Optional<ArrayList<AppPaths>> findAllUserAuthorizedPathsByModule(@Param("moduleId") Long moduleId, @Param("userId") Long userId);

    @Query("SELECT r.appPaths FROM  Roles r  WHERE r.id = ?1")
    List<AppPaths> findAllRolePartsByRoleId(@Param("roleId") Long roleId);




}
