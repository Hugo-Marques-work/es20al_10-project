package pt.ulisboa.tecnico.socialsoftware.tutor.clarification.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import pt.ulisboa.tecnico.socialsoftware.tutor.clarification.ClarificationService;
import pt.ulisboa.tecnico.socialsoftware.tutor.clarification.dto.ClarificationDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User;

import java.security.Principal;
import java.util.List;

import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.AUTHENTICATION_ERROR;

@RestController
public class ClarificationController {
    @Autowired
    private ClarificationService clarificationService;

    @PostMapping("/question/{questionId}/clarifications")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('DEMO_ADMIN') or hasPermission(#questionId, 'QUESTION.ACCESS')")
    public ClarificationDto addClarification(@PathVariable int questionId, @RequestBody String content, Principal principal) {
        User user = (User) ((Authentication) principal).getPrincipal();
        if(user == null){
            throw new TutorException(AUTHENTICATION_ERROR);
        }
        return clarificationService.createClarification(questionId, user.getKey(), content);
    }

    @GetMapping("/question/{questionId}/clarifications")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('DEMO_ADMIN') or hasPermission(#questionId, 'QUESTION.ACCESS')")
    public List<ClarificationDto> getClarificationsByQuestion(@PathVariable int questionId) {
        return clarificationService.getClarificationsByQuestion(questionId);
    }

    @GetMapping("/course/{courseId}/clarifications")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('DEMO_ADMIN') or hasPermission(#courseId, 'COURSE.ACCESS')")
    public List<ClarificationDto> getClarificationsByCourse(@PathVariable int courseId) {
        return clarificationService.getClarificationsByCourse(courseId);
    }

    @DeleteMapping("/clarification/{clarificationId}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('DEMO_ADMIN') or hasPermission(#clarificationId, 'CLARIFICATION.ACCESS')")
    public ResponseEntity removeClarification(@PathVariable int clarificationId) {
        clarificationService.removeClarification(clarificationId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/clarifications/{clarificationId}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('DEMO_ADMIN') or hasPermission(#clarificationId, 'CLARIFICATION.ACCESS')")
    public ClarificationDto updateClarifications(@PathVariable int clarificationId, @RequestBody String content) {
        return clarificationService.updateClarification(clarificationId, content);
    }

    @GetMapping("/clarifications")
    @PreAuthorize("hasRole('ROLE_STUDENT') or hasRole('DEMO_STUDENT')")
    public List<ClarificationDto> getClarificationsByUser(Principal principal) {
        User user = (User) ((Authentication) principal).getPrincipal();
        if(user == null){
            throw new TutorException(AUTHENTICATION_ERROR);
        }
        return clarificationService.getClarificationsByUser(user.getId());
    }
}
