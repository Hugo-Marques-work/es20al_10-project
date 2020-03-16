package pt.ulisboa.tecnico.socialsoftware.tutor.tournament;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.dto.TournamentDto;

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

}
