package pt.ulisboa.tecnico.socialsoftware.tutor.clarification;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import pt.ulisboa.tecnico.socialsoftware.tutor.clarification.dto.ClarificationAnswerDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.clarification.domain.Clarification;
import pt.ulisboa.tecnico.socialsoftware.tutor.clarification.dto.ClarificationDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.clarification.repository.ClarificationRepository;
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage;
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.QuestionRepository;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserRepository;

@Service
public class ClarificationService {
    @Autowired
    QuestionRepository questionRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    ClarificationRepository clarificationRepository;

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public ClarificationDto createClarification(Question question, User user, String content) {
        CheckQuestion(question);
        CheckUser(user);
        CheckContent(content);

        Clarification clarification = new Clarification(content, question, user);
        clarificationRepository.save(clarification);
        question.addClarification(clarification);
        user.addClarification(clarification);

        return new ClarificationDto(clarification);
    }

    private void CheckQuestion(Question question) {
        Question qtn = questionRepository.findByKey(question.getKey()).orElse(null);
        if (qtn == null)
            throw new TutorException(ErrorMessage.QUESTION_NOT_FOUND, question.getKey());
    }

    private void CheckUser(User user) {
        User usr = userRepository.findByKey(user.getKey());
        if (usr == null)
            throw new TutorException(ErrorMessage.USER_NOT_FOUND, user.getKey());
        else if (usr.getRole() != User.Role.STUDENT)
            throw new TutorException(ErrorMessage.CLARIFICATION_WRONG_USER);
    }

    private void CheckContent(String content) {
        if (content == null || content.isBlank() || content.isEmpty())
            throw new TutorException(ErrorMessage.CLARIFICATION_IS_EMPTY);
    }

    public ClarificationAnswerDto createClarificationAnswer(){
        return new ClarificationAnswerDto();
    }
}
