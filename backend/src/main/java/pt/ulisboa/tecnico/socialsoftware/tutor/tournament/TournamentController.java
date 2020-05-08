package pt.ulisboa.tecnico.socialsoftware.tutor.tournament;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException;
import pt.ulisboa.tecnico.socialsoftware.tutor.statement.StatementService;
import pt.ulisboa.tecnico.socialsoftware.tutor.statement.dto.StatementAnswerDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.statement.dto.StatementQuizDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.dto.TournamentDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User;

import javax.validation.Valid;
import java.security.Principal;
import java.util.List;

import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.AUTHENTICATION_ERROR;

@RestController
public class TournamentController {

    @Autowired
    TournamentService tournamentService;

    @Autowired
    StatementService statementService;

    @GetMapping("/executions/{executionId}/tournaments/open")
    @PreAuthorize("hasPermission(#executionId, 'EXECUTION.ACCESS')")
    public List<TournamentDto> findOpenTournaments(@PathVariable int executionId) {
        return tournamentService.getOpenTournaments(executionId);
    }

    @GetMapping("/executions/{executionId}/tournaments/closed")
    @PreAuthorize("hasPermission(#executionId, 'EXECUTION.ACCESS')")
    public List<TournamentDto> getClosedTournaments(Principal principal, @PathVariable int executionId) {
        User user = (User) ((Authentication) principal).getPrincipal();
        checkUserAuth(user);

        return tournamentService.getClosedTournaments(user.getId(), executionId);
    }

    @GetMapping("/executions/{executionId}/tournaments/running")
    @PreAuthorize("hasPermission(#executionId, 'EXECUTION.ACCESS')")
    public List<TournamentDto> findRunningTournaments(@PathVariable int executionId) {
        return tournamentService.getRunningTournaments(executionId);
    }

    @PostMapping("/executions/{executionId}/tournaments/")
    @PreAuthorize("hasRole('ROLE_STUDENT') && hasPermission(#executionId, 'EXECUTION.ACCESS')")
    public TournamentDto createTournament(Principal principal,
                                          @PathVariable int executionId,
                                          @Valid @RequestBody TournamentDto tournamentDto) {
        User user = (User) ((Authentication) principal).getPrincipal();
        checkUserAuth(user);

        return tournamentService.createTournament(user.getId(), executionId, tournamentDto);
    }

    @PostMapping("/tournaments/{tournamentId}/signUp")
    @PreAuthorize("hasRole('ROLE_STUDENT') && hasPermission(#tournamentId, 'TOURNAMENT.ACCESS')")
    public ResponseEntity signUpForTournament(Principal principal,@PathVariable int tournamentId) {

        User user = (User) ((Authentication) principal).getPrincipal();
        checkUserAuth(user);

        tournamentService.signUp(user.getId(), tournamentId);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/tournaments/{tournamentId}/cancel")
    @PreAuthorize("hasRole('ROLE_STUDENT') and hasPermission(#tournamentId, 'TOURNAMENT.CANCEL')")
    public ResponseEntity cancelTournament(@PathVariable int tournamentId) {
        tournamentService.cancelTournament(tournamentId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/tournaments/{tournamentId}/quiz")
    @PreAuthorize("hasRole('ROLE_STUDENT') and hasPermission(#tournamentId, 'TOURNAMENT.GET')")
    public StatementQuizDto getStatement(Principal principal, @PathVariable int tournamentId) {
        User user = (User) ((Authentication) principal).getPrincipal();
        checkUserAuth(user);

        return tournamentService.getStatement(user.getId(), tournamentId);
    }

    @PostMapping("/tournament/quiz/{quizId}/submit")
    @PreAuthorize("hasRole('ROLE_STUDENT') and hasPermission(#quizId, 'TOURNAMENT.PARTICIPATE')")
    public boolean submitTournamentAnswer(Principal principal, @PathVariable int quizId, @Valid @RequestBody StatementAnswerDto answer) {
        User user = (User) ((Authentication) principal).getPrincipal();

        if (user == null) {
            throw new TutorException(AUTHENTICATION_ERROR);
        }

        return statementService.submitTournamentAnswer(user.getId(), quizId, answer);
    }

    @GetMapping("/tournaments/user-privacy-preference/get")
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    public String getUserTournamentPrivacyPreference(Principal principal) {
        User user = (User) ((Authentication) principal).getPrincipal();
        return tournamentService.getTournamentPrivacyPreference(user.getId());
    }

    @PostMapping("/tournaments/user-privacy-preference/set/{preference}")
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    public void setUserTournamentPrivacyPreference(Principal principal, @PathVariable String preference) {
        User user = (User) ((Authentication) principal).getPrincipal();
        tournamentService.setTournamentPrivacyPreference(user.getId(), preference);
    }

    private void checkUserAuth(User user) {
        if(user == null) {
            throw new TutorException(AUTHENTICATION_ERROR);
        }
    }
}
