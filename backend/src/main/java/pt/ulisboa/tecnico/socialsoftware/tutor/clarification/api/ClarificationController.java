package pt.ulisboa.tecnico.socialsoftware.tutor.clarification.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pt.ulisboa.tecnico.socialsoftware.tutor.clarification.ClarificationService;
import pt.ulisboa.tecnico.socialsoftware.tutor.clarification.dto.ClarificationDto;

import java.util.List;

@RestController
public class ClarificationController {
    @Autowired
    private ClarificationService clarificationService;

    @GetMapping("/question/{questionKey}/clarifications")
    public List<ClarificationDto> getClarificationsByQuestion(@PathVariable int questionKey) {
        return clarificationService.getClarificationsByQuestion(questionKey);
    }

    @GetMapping("/question/clarifications/user/{userKey}")
    public List<ClarificationDto> getClarificationsByUser(@PathVariable int userKey) {
        return clarificationService.getClarificationsByUser(userKey);
    }

    @PostMapping("/question/{questionKey}/clarifications/user/{userKey}")
    public ClarificationDto addClarification(@PathVariable int questionKey, @PathVariable int userKey, @RequestBody String content) {
        return clarificationService.createClarification(questionKey, userKey, content);
    }

    @PutMapping("/question/{questionKey}/clarifications/user/{userKey}")
    public List<ClarificationDto> replaceClarifications(@PathVariable int questionKey, @PathVariable int userKey, @RequestBody List<ClarificationDto> clarificationDtos) {
        return clarificationService.replaceClarifications(questionKey, userKey, clarificationDtos);
    }

    @DeleteMapping("/question//clarification/{clarificationId}")
    public ResponseEntity removeClarification(@PathVariable int clarificationId) {
        clarificationService.removeClarification(clarificationId);
        return ResponseEntity.ok().build();
    }
}
