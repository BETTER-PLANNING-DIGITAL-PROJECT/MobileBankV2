package ibnk.service;

import ibnk.dto.AuthzDao;
import ibnk.models.internet.UserEntity;
import ibnk.models.internet.authorization.AppPaths;
import ibnk.models.internet.authorization.Roles;
import ibnk.models.internet.enums.PathType;
import ibnk.repositories.internet.PathsRepository;
import ibnk.repositories.internet.PermissionRepo;
import ibnk.repositories.internet.RolesRepo;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

@Service()
@RequiredArgsConstructor
public class AuthorizationService {
    private  final PathsRepository pathsRepository;

    private  final RolesRepo rolesRepo;

    private  final PermissionRepo permissionRepo;

    public void saveNewPath (AuthzDao.saveAppPath dao) throws Exception {
        AppPaths path = new AppPaths();
        if(dao.getPathId() != null) {
            Optional<AppPaths> reqPath = pathsRepository.findById(dao.getPathId());
            if(reqPath.isEmpty()) throw  new Exception("path_not_found");
            path = reqPath.get();
        }
        if (dao.getType() == PathType.MODULE  )  {
            if(dao.getParentId() != null) throw new Exception("module_cant_have_parent");
            path.setPath(dao.getPath());
            path.setIcon(dao.getIcon());
            path.setMenuLevel(dao.getOrder());
            path.setType(dao.getType());
            path.setName(dao.getName());
        }
        if (dao.getType() == PathType.MENU  )  {
            Objects.requireNonNull(dao.getModuleId(), "module_required_for_menu");
            Optional<AppPaths> module = pathsRepository.findByIdAndType(dao.getModuleId(), PathType.MODULE);
            if(module.isEmpty()) throw new Exception("module_not_found");

            Optional<AppPaths> parent = Optional.empty();
            if(dao.getParentId() != null) {
                parent = pathsRepository.findByIdAndModule(dao.getParentId(), module.get());
                if(parent.isEmpty()) throw  new Exception("parent_not_found");
                path.setParent(parent.get());
            }
            path.setPath(dao.getPath());
            path.setIcon(dao.getIcon());
            path.setMenuLevel(dao.getOrder());
            path.setModule( module.get());
            path.setType(dao.getType());
            path.setName(dao.getName());
            path.setMenuStyle(dao.getMenuStyle());
        }
//        Optional<Set<Permissions>> permissions = permissionRepo.findPermissionsByIdIn(dao.getPathPermissions());
//        if(permissions.isPresent()) {
//            path.setPermissions(permissions.get());
//        }
        pathsRepository.save(path);
    }

    @Transactional()
    public void saveRole (AuthzDao.saveRole dao) throws Exception {
        HashSet<AppPaths> rolePaths = new HashSet<AppPaths>();
        AtomicBoolean isError = new AtomicBoolean(false);
        AtomicReference<String> ErrorMsg  =  new AtomicReference<String>("");

        for (AuthzDao.module moduleInfo : dao.getModules()) {
            Optional<AppPaths> module =  pathsRepository.findByIdAndType(moduleInfo.getModuleId(), PathType.MODULE);
            if(module.isEmpty()) {
                isError.set(true);
                ErrorMsg.set("module_not_found " + moduleInfo.getModuleId());
                break;
            } else {
                rolePaths.add(module.get());
                for (Long menuId: moduleInfo.getMenuIds()){
                    Optional<AppPaths> menu =  pathsRepository.findByIdAndModule(menuId, module.get());
                    if(menu.isEmpty()) {
                        isError.set(true);
                        ErrorMsg.set("menu_not_found");
                        break;
                    } else {
                        rolePaths.add(menu.get());
                    }
                }
            }
        }

        if(isError.get() == new AtomicBoolean(true).get()) throw new Exception(ErrorMsg.get());

        Roles role = new Roles();
        role.setName(dao.getName());
        role.setAppPaths(rolePaths);
        rolesRepo.save(role);
    }

    @Transactional()
    public void updateRole (AuthzDao.saveRole dao, Long roleId) throws Exception {
        HashSet<AppPaths> rolePaths = new HashSet<AppPaths>();
        AtomicBoolean isError = new AtomicBoolean(false);
        AtomicReference<String> ErrorMsg  =  new AtomicReference<String>("");
        Optional<Roles> role = rolesRepo.findById(roleId);
        if(role.isEmpty()) throw  new Exception("role_not_found");
        for (AuthzDao.module moduleInfo : dao.getModules()) {
            Optional<AppPaths> module =  pathsRepository.findByIdAndType(moduleInfo.getModuleId(), PathType.MODULE);
            if(module.isEmpty()) {
                isError.set(true);
                ErrorMsg.set("module_not_found " + moduleInfo.getModuleId());
                break;
            } else {
                rolePaths.add(module.get());
                for (Long menuId: moduleInfo.getMenuIds()){
                    Optional<AppPaths> menu =  pathsRepository.findByIdAndModule(menuId, module.get());
                    if(menu.isEmpty()) {
                        isError.set(true);
                        ErrorMsg.set("menu_not_found");
                        break;
                    } else {
                        rolePaths.add(menu.get());
                    }
                }
            }
        }

        if(isError.get() == new AtomicBoolean(true).get()) throw new Exception(ErrorMsg.get());

        role.get().setName(dao.getName());
        role.get().setAppPaths(rolePaths);
        rolesRepo.save(role.get());
    }

    public List<AppPaths> getAuthorizedPaths(UserEntity user, Long moduleId) {
        ArrayList<AppPaths> paths = new ArrayList<>();
        Optional<ArrayList<AppPaths>> pathsSearch = rolesRepo.findAllUserAuthorizedPaths(user.getId());
        if(pathsSearch.isPresent()) {
            paths = pathsSearch.get();
        }
         return paths.stream().filter(appPaths -> appPaths.getType() == PathType.MENU && appPaths.getModule().getId().equals(moduleId)).toList();
    }

    public List<AppPaths> getAuthorizedModules(UserEntity user) {
        ArrayList<AppPaths> paths = new ArrayList<>();
        Optional<ArrayList<AppPaths>> pathsSearch = rolesRepo.findAllUserAuthorizedPaths(user.getId());
        if(pathsSearch.isPresent()) {
            paths = pathsSearch.get();
        }
        return paths.stream().filter(appPaths -> appPaths.getType() == PathType.MODULE).toList();
    }

//    public ArrayList<AppPaths> getAuthorizedPathsByModule(UserEntity user, Long moduleId) {
//        ArrayList<AppPaths> paths = new ArrayList<>();
//        Optional<ArrayList<AppPaths>> pathsSearch = rolesRepo.findAllUserAuthorizedPaths(user.getId());
//        if(pathsSearch.isPresent()) {
//            paths = pathsSearch.get();
//        }
//        List<AppPaths> parents = paths.stream().filter(appPaths -> Objects.equals(appPaths.getModule().getId(), moduleId) && appPaths.getParent() == null).toList();
//        parents.forEach(appPaths -> {
//            parents.s
//        });
//        return  paths;
//    }
}
