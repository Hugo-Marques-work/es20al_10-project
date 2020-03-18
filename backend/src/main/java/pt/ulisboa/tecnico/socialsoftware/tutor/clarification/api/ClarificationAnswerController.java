package pt.ulisboa.tecnico.socialsoftware.tutor.clarification.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import pt.ulisboa.tecnico.socialsoftware.tutor.clarification.ClarificationService;
import pt.ulisboa.tecnico.socialsoftware.tutor.clarification.dto.ClarificationAnswerDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User;

import javax.validation.Valid;
import java.security.Principal;
import java.util.List;


@RestController
public class ClarificationAnswerController {

    @Autowired
    ClarificationService clarificationService;

    @GetMapping("/clarification/{clarificationId}/answers")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_DEMO_ADMIN') or hasPermission(#clarificationId, 'CLARIFICATION.ACCESS')")
    public List<ClarificationAnswerDto> getClarificationAnswers(@PathVariable int clarificationId) {
        return this.clarificationService.getClarificationAnswers(clarificationId);
    }

    @PostMapping("/clarification/{clarificationId}/answer")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_DEMO_ADMIN') or hasPermission(#clarificationId, 'CLARIFICATION.ACCESS')")
    public ClarificationAnswerDto addClarificationAnswer(@PathVariable int clarificationId,
                                                         @Valid @RequestBody String content,
                                                         Principal principal) {
        User user = (User) ((Authentication) principal).getPrincipal();
        return this.clarificationService.createClarificationAnswer(clarificationId, content, user.getKey());
    }

    //FIXME: Do I need to check if it is the user creator?
    @PutMapping("/clarification/answer/{clarificationAnswerId}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_DEMO_ADMIN') or hasPermission(#clarificationId, 'CLARIFICATION.ACCESS')")
    public ClarificationAnswerDto updateClarificationAnswer(@PathVariable int clarificationAnswerId,
                                                            @Valid @PathVariable ClarificationAnswerDto clarificationAnswerDto,) {
        return this.clarificationService.updateClarificationAnswer(clarificationAnswerId, clarificationAnswerDto);
    }

    //FIXME: Do I need to check if it is the user creator?
    @DeleteMapping("/clarification/answer/{clarificationAnswerId}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_DEMO_ADMIN') or hasPermission(#clarificationId, 'CLARIFICATION.ACCESS')")
    public ResponseEntity removeClarificationAnswer(@PathVariable int clarificationAnswerId) {
        this.clarificationService.removeClarificationAnswer(clarificationAnswerId);
        return ResponseEntity.ok().build();
    }

}