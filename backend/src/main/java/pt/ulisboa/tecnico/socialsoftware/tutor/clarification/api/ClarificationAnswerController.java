package pt.ulisboa.tecnico.socialsoftware.tutor.clarification.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import pt.ulisboa.tecnico.socialsoftware.tutor.clarification.ClarificationService;
import pt.ulisboa.tecnico.socialsoftware.tutor.clarification.dto.ClarificationAnswerDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User;

import javax.validation.Valid;
import java.security.Principal;
import java.util.List;

import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.AUTHENTICATION_ERROR;


@RestController
public class ClarificationAnswerController {
    private static Logger logger = LoggerFactory.getLogger(ClarificationAnswerController.class);

    @Autowired
    ClarificationService clarificationService;

    @GetMapping("/clarification/{clarificationId}/answers")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_DEMO_ADMIN') or hasPermission(#clarificationId, 'CLARIFICATION.ACCESS')")
    public List<ClarificationAnswerDto> getClarificationAnswers(@PathVariable int clarificationId) {
        logger.debug("getClarificationAnswers - clarificationId: {}", clarificationId);
        return clarificationService.getClarificationAnswers(clarificationId);
    }

    @PostMapping("/clarification/{clarificationId}/answer")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_DEMO_ADMIN') or hasPermission(#clarificationId, 'CLARIFICATION.ACCESS')")
    public ClarificationAnswerDto addClarificationAnswer(@PathVariable int clarificationId,
                                                         @Valid @RequestBody String content,
                                                         Principal principal) {
        User user = (User) ((Authentication) principal).getPrincipal();

        if(user == null){
            throw new TutorException(AUTHENTICATION_ERROR);
        }
        logger.debug("addClarificationAnswer - clarificationId: {}", clarificationId);
        return clarificationService.createClarificationAnswer(clarificationId, content, user.getId());
    }

    @PutMapping("/clarification/answer/{clarificationAnswerId}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_DEMO_ADMIN') " +
            "or (hasRole('ROLE_TEACHER') and hasPermission(#clarificationAnswerId, 'CLARIFICATION_ANSWER.ACCESS'))")
    public ClarificationAnswerDto updateClarificationAnswer(@PathVariable int clarificationAnswerId,
                                                            @Valid @RequestBody String content) {
        logger.debug("updateClarificationAnswer - clarificationAnswerId: {}", clarificationAnswerId);
        return clarificationService.updateClarificationAnswer(clarificationAnswerId, content);
    }

    @DeleteMapping("/clarification/answer/{clarificationAnswerId}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_DEMO_ADMIN') " +
            "or (hasRole('ROLE_TEACHER') and hasPermission(#clarificationAnswerId, 'CLARIFICATION_ANSWER.ACCESS'))")
    public ResponseEntity removeClarificationAnswer(@PathVariable int clarificationAnswerId) {
        logger.debug("deleteClarificationAnswer - clarificationAnswerId: {}", clarificationAnswerId);
        clarificationService.removeClarificationAnswer(clarificationAnswerId);
        return ResponseEntity.ok().build();
    }

}