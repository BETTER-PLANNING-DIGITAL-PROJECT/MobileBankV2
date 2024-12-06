package ibnk.dto;

import ibnk.models.internet.authorization.AppPaths;
import ibnk.models.internet.enums.PathType;
import lombok.Data;

import java.util.List;
import java.util.Set;

public class AuthzDao {

    @Data
    public static class saveAppPath {

        private Long pathId;

        private String name;

        private String path;

        private String icon;

        private PathType type;

        private Long parentId;

        private Long moduleId;

        private String menuStyle;

        private Long order;

        private Set<Long> pathPermissions;

        public static saveAppPath ModelToDao(AppPaths mdl){
            saveAppPath dto = new saveAppPath();
            dto.setPath(mdl.getPath());
            dto.setName(mdl.getName());
            dto.setType(mdl.getType());
            return dto;
        }
    }

    @Data
    public static class saveRole {

        private Long roleId;

        private String name;

        List<module> modules;

    }

    @Data
    public static class module {
        private Long moduleId;

        private List<Long> menuIds;
    }

    @Data
    public static class menu {
        private Long menuId;

        private Set<Long> permissionIds;

    }

    @Data
    public static class RolesInfo {

        private Long id;

        private String name;

        private List<AppPaths> paths;

    }
}
