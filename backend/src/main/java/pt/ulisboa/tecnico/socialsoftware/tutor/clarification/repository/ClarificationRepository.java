package pt.ulisboa.tecnico.socialsoftware.tutor.clarification.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import pt.ulisboa.tecnico.socialsoftware.tutor.clarification.domain.Clarification;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User;

@Repository
@Transactional
public interface ClarificationRepository extends JpaRepository<Clarification, Integer> {
    @Query(value = "SELECT * FROM users u WHERE u.key = userKey", nativeQuery = true)
    User getUser();
}
