package pt.ulisboa.tecnico.socialsoftware.tutor.tournament;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import pt.ulisboa.tecnico.socialsoftware.tutor.config.DateHandler;
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException;
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

    @GetMapping("/executions/{executionId}/tournaments/open")
    @PreAuthorize("hasPermission(#executionId, 'EXECUTION.ACCESS')")
    public List<TournamentDto> findOpenTournaments(@PathVariable int executionId) {
        return tournamentService.getOpenTournaments(executionId);
    }

    @PostMapping("/executions/{executionId}/tournaments/")
    @PreAuthorize("hasRole('ROLE_STUDENT') && hasPermission(#executionId, 'EXECUTION.ACCESS')")
    public TournamentDto createTournament(Principal principal,
                                          @PathVariable int executionId,
                                          @Valid @RequestBody TournamentDto tournamentDto) {
        User user = (User) ((Authentication) principal).getPrincipal();
        if(user == null){
            throw new TutorException(AUTHENTICATION_ERROR);
        }

        formatDates(tournamentDto);
        return tournamentService.createTournament(user.getId(), executionId, tournamentDto);
    }

    private void formatDates(TournamentDto tournament) {
        DateHandler.formatFromRequest(tournament.getStartingDate());
        DateHandler.formatFromRequest(tournament.getConclusionDate());
    }
}
