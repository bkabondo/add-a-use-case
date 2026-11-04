package team.projectpulse.student;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Integer>, JpaSpecificationExecutor<Student> {

    // Find all students in a section
    List<Student> findBySectionSectionId(Integer sectionId);

    List<Student> findByTeamTeamId(Integer teamId);

    // Load a student by id and bind her to an instructor of her section in one query, so a caller can only reach a
    // student in a section she is assigned to (BR-section-scoped-access).
    Optional<Student> findByIdAndSectionInstructorsId(Integer studentId, Integer instructorId);

    // The course-admin half of the same scope: an admin is bound to the student by owning her section's course, not by
    // being assigned to the section (BR-section-scoped-access, BR-role-based-access).
    Optional<Student> findByIdAndSectionCourseCourseAdminId(Integer studentId, Integer courseAdminId);
}
