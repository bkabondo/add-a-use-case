package team.projectpulse.student;

import team.projectpulse.course.Course;
import team.projectpulse.system.UserUtils;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

/**
 * Answers the questions a route asks about one named student: is that student the caller herself
 * ({@code isStudentSelf}), is the caller an instructor of that student's course section
 * ({@code isCurrentUserInstructorOfStudentSection}), and is the caller the course admin who owns that course section's
 * course ({@code isCurrentUserCourseAdminOfStudentSection}).
 *
 * <p>Every check fails closed: an id that names no row, or a link not yet established, is denied rather than
 * throwing. These methods run inside an {@code AuthorizationManager}, upstream of the {@code DispatcherServlet},
 * so an exception thrown here escapes the filter chain as a 500 that also tells the caller which ids exist.
 */
@Service
@Transactional
public class StudentSecurityService {

    private final StudentRepository studentRepository;
    private final UserUtils userUtils;


    public StudentSecurityService(StudentRepository studentRepository, UserUtils userUtils) {
        this.studentRepository = studentRepository;
        this.userUtils = userUtils;
    }

    public boolean isStudentSelf(Integer studentId) {
        Integer studentIdFromJwt = this.userUtils.getUserId();
        return studentId.equals(studentIdFromJwt);
    }

    /**
     * Check if the user is the instructor of the section that the student is in
     *
     * @param studentId the id of the student we need to access, this is NOT the current user's Id
     * @return
     */
    public boolean isCurrentUserInstructorOfStudentSection(Integer studentId) {
        Integer instructorIdFromJwt = this.userUtils.getUserId();
        Student student = this.studentRepository.findById(studentId).orElse(null);
        if (student == null) {
            return false;
        }
        return student.getSection().getInstructors().stream()
                .anyMatch(instructor -> instructor.getId().equals(instructorIdFromJwt));
    }

    /**
     * Check if the user is the course admin who owns the course the student's course section belongs to.
     *
     * <p>BR-student-lifecycle admits a course admin alongside an instructor assigned to the course section, and
     * BR-section-scoped-access scopes a course admin by course <em>ownership</em> rather than by section assignment —
     * so an admin who staffed a course section with someone else still reaches its students.
     *
     * @param studentId the id of the student we need to access, this is NOT the current user's Id
     */
    public boolean isCurrentUserCourseAdminOfStudentSection(Integer studentId) {
        Integer instructorIdFromJwt = this.userUtils.getUserId();
        Student student = this.studentRepository.findById(studentId).orElse(null);
        if (student == null) {
            return false;
        }
        Course course = student.getSection().getCourse();
        if (course == null || course.getCourseAdmin() == null) {
            return false; // Fails closed rather than throwing: this runs inside the filter chain.
        }
        return course.getCourseAdmin().getId().equals(instructorIdFromJwt);
    }

}
