package ibnk.models.internet.authorization;

import com.fasterxml.jackson.annotation.JsonIgnore;
import ibnk.models.internet.enums.PathType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicUpdate;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@DynamicUpdate
public class AppPaths {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long menuLevel;

    private String name;

    private String path;

    private String icon;

    private String category;

    @Enumerated(EnumType.STRING)
    private PathType type;

    private String menuStyle;

//    @OneToMany(cascade = CascadeType.ALL)
//    @JsonIgnore()
//    private Set<Permissions> permissions ;

    @OneToMany(mappedBy = "module")
    @JsonIgnore()
    private List<AppPaths> menus;

    @OneToMany(mappedBy = "parent")
    @JsonIgnore()
    private List<AppPaths> children;

    @ManyToOne
    @JoinColumn(name = "parent_id")
//    @JsonIgnore()
    private AppPaths parent;

    @ManyToOne
    @JoinColumn(name = "module_id")
//    @JsonIgnore()
    private AppPaths module;

}
