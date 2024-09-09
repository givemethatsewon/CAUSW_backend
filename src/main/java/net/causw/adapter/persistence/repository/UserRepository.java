package net.causw.adapter.persistence.repository;

import net.causw.adapter.persistence.user.User;
import net.causw.domain.model.enums.AcademicStatus;
import net.causw.domain.model.enums.Role;
import net.causw.domain.model.enums.UserState;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {

    List<User> findAll();

    Optional<User> findByEmailAndNameAndStudentIdAndPhoneNumber(String email, String name, String studentId, String phoneNumber);

    @NotNull
    Page<User> findAll(@NotNull Pageable pageable);

    Optional<User> findByEmailAndNameAndStudentId(String email, String name, String studentId);

    Optional<User> findByEmail(String email);

    Optional<User> findByNickname(String nickname);

    Optional<User> findById(String id);

    List<User> findByName(String name);

    List<User> findAllByState(UserState state);

    Optional<User> findByStudentIdAndNameAndPhoneNumber(String studentId, String name, String phoneNumber);

    List<User> findByStudentIdAndStateAndAcademicStatus(String studentId, UserState userState, AcademicStatus academicStatus);

    @Query("SELECT u FROM User u WHERE :role MEMBER OF u.roles AND u.state = :state")
    List<User> findByRoleAndState(@Param("role") Role role, @Param("state") UserState state);


    @Query(value = "SELECT * "  +
            "FROM tb_user AS u " +
            "WHERE u.state = :state AND (:name IS NULL OR u.name LIKE %:name%) ORDER BY u.created_at DESC" , nativeQuery = true)
    Page<User> findByStateAndName(@Param("state") String state, @Param("name") String name, Pageable pageable);

    @Query(value = "SELECT * "  +
            "FROM tb_user AS u " +
            "WHERE u.state IN :state AND (COALESCE(:name, '') = '' OR u.name LIKE CONCAT('%', :name, '%')) ORDER BY u.created_at DESC" , nativeQuery = true)
    Page<User> findByStateInAndNameContaining(@Param("state")List<String> states, @Param("name")String name, Pageable pageable);

    @Query(value = "SELECT * " +
            "FROM tb_user AS u " +
            "WHERE u.academic_status = :academicStatus1 OR u.academic_status = :academicStatus2 ORDER BY u.created_at DESC", nativeQuery = true)
    List<User> findAllByTwoAcademicStatus(AcademicStatus academicStatus1, AcademicStatus academicStatus2);
}












