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

    @GetMapping("/question/{questionKey}/clarifications")
    public List<ClarificationDto> getClarificationsByQuestion(@PathVariable int questionKey) {
        return clarificationService.getClarificationsByQuestion(questionKey);
    }

    @GetMapping("/clarifications")
    public List<ClarificationDto> getClarificationsByUser(Principal principal) {
        User user = (User) ((Authentication) principal).getPrincipal();
        if(user == null){
            throw new TutorException(AUTHENTICATION_ERROR);
        }
        return clarificationService.getClarificationsByUser(user.getKey());
    }

    @PostMapping("/question/{questionKey}/clarifications")
    public ClarificationDto addClarification(@PathVariable int questionKey, @RequestBody String content, Principal principal) {
        User user = (User) ((Authentication) principal).getPrincipal();
        if(user == null){
            throw new TutorException(AUTHENTICATION_ERROR);
        }
        return clarificationService.createClarification(questionKey, user.getKey(), content);
    }

    @PutMapping("/clarifications")
    public ClarificationDto replaceClarifications(@RequestBody ClarificationDto clarificationDto) {
        return clarificationService.updateClarifications(clarificationDto);
    }

    @DeleteMapping("/clarification/{clarificationId}")
    public ResponseEntity removeClarification(@PathVariable int clarificationId) {
        clarificationService.removeClarification(clarificationId);
        return ResponseEntity.ok().build();
    }
}
