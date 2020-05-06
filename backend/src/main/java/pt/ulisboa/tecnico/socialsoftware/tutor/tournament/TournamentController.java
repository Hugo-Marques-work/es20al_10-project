package pt.ulisboa.tecnico.socialsoftware.tutor.tournament;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException;
import pt.ulisboa.tecnico.socialsoftware.tutor.statement.dto.StatementQuizDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.dto.TournamentDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User;

import javax.validation.Valid;
import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.*;

@RestController
public class TournamentController {

    @Autowired
    TournamentService tournamentService;

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
    @PreAuthorize("hasRole('ROLE_STUDENT') and hasPermission(#tournamentId, 'TOURNAMENT.ACCESS')")
    public StatementQuizDto getStatement(Principal principal, @PathVariable int tournamentId) {
        User user = (User) ((Authentication) principal).getPrincipal();
        checkUserAuth(user);

        return tournamentService.getStatement(user.getId(), tournamentId);
    }

    private void checkUserAuth(User user) {
        if(user == null) {
            throw new TutorException(AUTHENTICATION_ERROR);
        }
    }
}
