package pt.ulisboa.tecnico.socialsoftware.tutor.tournament;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pt.ulisboa.tecnico.socialsoftware.tutor.config.DateHandler;
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.dto.TournamentDto;

import javax.validation.Valid;
import java.util.List;

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
    public TournamentDto createTournament(@PathVariable int executionId,
                                                @Valid @RequestBody int userId,
                                                @Valid @RequestBody TournamentDto tournamentDto) {
        formatDates(tournamentDto);
        return tournamentService.createTournament(executionId, userId, tournamentDto);
    }

    private void formatDates(TournamentDto tournament) {
        DateHandler.formatFromRequest(tournament.getStartingDate());
        DateHandler.formatFromRequest(tournament.getConclusionDate());
    }
}
