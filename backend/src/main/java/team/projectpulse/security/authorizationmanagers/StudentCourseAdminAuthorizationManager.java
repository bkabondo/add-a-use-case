package team.projectpulse.security.authorizationmanagers;

import org.jspecify.annotations.Nullable;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.authorization.AuthorizationResult;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.stereotype.Component;
import team.projectpulse.student.StudentSecurityService;

import java.util.function.Supplier;

/**
 * Guards acting on one student as the course admin who owns that student's course section's course, which
 * BR-section-scoped-access scopes by course ownership rather than by section assignment. Paired with
 * {@link StudentInstructorAuthorizationManager} under {@code AuthorizationManagers.anyOf}, because a course admin who
 * staffed a course section with someone else is not among its instructors, and an assigned instructor does not own the
 * course — either manager alone would shut out the other.
 *
 * <p>Reads {@code studentId} and delegates to {@link StudentSecurityService#isCurrentUserCourseAdminOfStudentSection}.
 */
@Component
public class StudentCourseAdminAuthorizationManager implements AuthorizationManager<RequestAuthorizationContext> {

    private final StudentSecurityService studentSecurityService;


    public StudentCourseAdminAuthorizationManager(StudentSecurityService studentSecurityService) {
        this.studentSecurityService = studentSecurityService;
    }

    @Override
    public @Nullable AuthorizationResult authorize(Supplier<? extends @Nullable Authentication> authentication, RequestAuthorizationContext context) {
        Integer studentId = PathVariables.readId(context, "studentId");
        if (studentId == null) {
            return new AuthorizationDecision(false);
        }
        return new AuthorizationDecision(
                this.studentSecurityService.isCurrentUserCourseAdminOfStudentSection(studentId));
    }

}
