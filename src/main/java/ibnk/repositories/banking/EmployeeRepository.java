package ibnk.repositories.banking;

import ibnk.models.banking.EmployeId;
import ibnk.models.banking.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, EmployeId> {

    @Query("SELECT e FROM Employe e WHERE e.id.matricule = :matricule")
    Optional<Employee> findByMatricule(@Param("matricule") String matricule);
}
