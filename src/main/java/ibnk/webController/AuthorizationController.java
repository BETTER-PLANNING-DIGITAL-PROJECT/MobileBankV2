package ibnk.webController;

import ibnk.dto.AuthzDao;
import ibnk.models.internet.UserEntity;
import ibnk.models.internet.authorization.AppPaths;
import ibnk.models.internet.authorization.Permissions;
import ibnk.models.internet.authorization.Roles;
import ibnk.models.internet.enums.PathType;
import ibnk.repositories.internet.PathsRepository;
import ibnk.repositories.internet.PermissionRepo;
import ibnk.repositories.internet.RolesRepo;
import ibnk.service.AuthorizationService;
import ibnk.tools.ResponseHandler;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.ibatis.javassist.NotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * @author PHILF
 */
@RestController
@RequestMapping("/api/v1/admin/authz")
@RequiredArgsConstructor
@CrossOrigin
public class AuthorizationController {
    private final AuthorizationService authorizationService;

    private final PathsRepository pathsRepository;

    private final PermissionRepo permissionRepo;

    private final RolesRepo rolesRepo;

    @CrossOrigin
    @GetMapping("/listAllOpenPermissions")
    public ResponseEntity<Object> listOpenPermissions() {
        List<Permissions> openPermissions = permissionRepo.findOpenPermissions();
        return ResponseHandler.generateResponse(HttpStatus.OK, true, "Success", openPermissions);
    }

    @CrossOrigin
    @GetMapping("/listAllPaths")
    public ResponseEntity<Object> lisAppPath() {
        List<AppPaths> paths = pathsRepository.findAll();
        return ResponseHandler.generateResponse(HttpStatus.OK, true, "Success", paths);
    }


    @CrossOrigin
    @GetMapping("/listAllPaths/{type}")
    public ResponseEntity<Object> lisAppPathsByType(@Valid @PathVariable PathType type) {
        ArrayList<AppPaths> resPaths = new ArrayList<>();
        Optional<ArrayList<AppPaths>> paths = pathsRepository.findByType(type);
        if(paths.isPresent()) {
            resPaths = paths.get();
        }
        return ResponseHandler.generateResponse(HttpStatus.OK, true, "Success", resPaths);
    }

    @CrossOrigin
    @GetMapping("/findPath/{pathId}")
    public ResponseEntity<Object> findPathById(@PathVariable Long pathId) throws NotFoundException {
        Optional<AppPaths> path = pathsRepository.findById(pathId);
        if(path.isEmpty()) {
            throw  new NotFoundException("path_not_found");
        }
        return ResponseHandler.generateResponse(HttpStatus.OK, true, "Success", path);
    }

    @CrossOrigin
    @GetMapping("/listAllRoles")
    public ResponseEntity<Object> listAllRoles() {
        List<Roles> roles = rolesRepo.findAll();
        return ResponseHandler.generateResponse(HttpStatus.OK, true, "Success", roles);
    }

    @CrossOrigin
    @GetMapping("/listPermissions/{pathId}")
    public ResponseEntity<Object> listPermissions(@PathVariable Long pathId) throws Exception {
        Set<Permissions> resPermissions = new HashSet<Permissions>();
        if(pathId == 0) {
            Optional<AppPaths> path = pathsRepository.findById(pathId);
            if(path.isEmpty()) {
                throw new Exception("path_not_found");
            }
            Optional<Set<Permissions>> permissions = permissionRepo.findByPath(path.get());
            if(permissions.isPresent()) {
                resPermissions = permissions.get();
            }
        } else {
            Optional<Set<Permissions>> permissions = permissionRepo.findByPath(null);        if(permissions.isPresent()) {
                resPermissions = permissions.get();
            }
        }

        return ResponseHandler.generateResponse(HttpStatus.OK, true, "Success", resPermissions);
    }

    @CrossOrigin
    @PostMapping("/createNewPath/{type}")
    public ResponseEntity<Object> createAppPaths(@Valid @RequestBody AuthzDao.saveAppPath dao, @PathVariable PathType type) throws Exception {
        dao.setType(type);
        authorizationService.saveNewPath(dao);
        String message = dao.getType()+"_CREATE";
        if(dao.getPath() != null) {
            message = dao.getType()+"_UPDATED";
        }
        return ResponseHandler.generateResponse(HttpStatus.OK, true, message, null);
    }

    @CrossOrigin
    @DeleteMapping("/deleteAppPath/{pathId}")
    public ResponseEntity<Object> deleteAppPath(@PathVariable Long pathId) {
        pathsRepository.deleteById(pathId);
        return ResponseHandler.generateResponse(HttpStatus.OK, true, "deleted", null);
    }


    @CrossOrigin
    @GetMapping("/authorized-menus/{moduleId}")
    public ResponseEntity<Object> getAuthorizedMenus(@PathVariable Long moduleId, @AuthenticationPrincipal UserEntity user) {
        List<AppPaths> paths =  authorizationService.getAuthorizedPaths(user, moduleId);
        return ResponseHandler.generateResponse(HttpStatus.OK, true, "list", paths);
    }

    @CrossOrigin
    @GetMapping("/authorized-modules")
    public ResponseEntity<Object> getAuthorizedModules( @AuthenticationPrincipal UserEntity user) {
        List<AppPaths> paths = new ArrayList<>(authorizationService.getAuthorizedModules(user));
        return ResponseHandler.generateResponse(HttpStatus.OK, true, "list", paths);
    }

    @CrossOrigin
    @PostMapping("/save-roles")
    public ResponseEntity<Object> saveUserRole(@Valid @RequestBody AuthzDao.saveRole dao ) throws Exception {
       authorizationService.saveRole(dao);
        return ResponseHandler.generateResponse(HttpStatus.OK, true, "created", null);
    }


    @CrossOrigin
    @PostMapping("/update-role/{roleId}")
    public ResponseEntity<Object> updateUserRole(@PathVariable Long roleId, @Valid @RequestBody AuthzDao.saveRole dao) throws Exception {
        authorizationService.updateRole(dao, roleId);
        return ResponseHandler.generateResponse(HttpStatus.OK, true, "created", null);
    }

    @CrossOrigin
    @DeleteMapping("/deleteRole/{roleId}")
    public ResponseEntity<Object> deleteRole(@PathVariable Long roleId)  {
        rolesRepo.deleteById(roleId);
        return ResponseHandler.generateResponse(HttpStatus.OK, true, "deleted", null);
    }

    @CrossOrigin
    @GetMapping("/getRolePaths/{roleId}")
    public ResponseEntity<Object> getRolesPaths(@PathVariable Long roleId) throws Exception {
        Optional<Roles> role = rolesRepo.findById(roleId);
        if(role.isEmpty()) {
            throw new Exception("role_not_found");
        }
        List<AppPaths> paths = rolesRepo.findAllRolePartsByRoleId(roleId);
        AuthzDao.RolesInfo rolesInfo = new AuthzDao.RolesInfo();
        rolesInfo.setId(role.get().getId());
        rolesInfo.setName(role.get().getName());
        rolesInfo.setPaths(paths);
        return ResponseHandler.generateResponse(HttpStatus.OK, true, "successful", rolesInfo);
    }

}
