package pt.ulisboa.tecnico.socialsoftware.tutor.clarification;

import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import pt.ulisboa.tecnico.socialsoftware.tutor.clarification.domain.ClarificationAnswer;
import pt.ulisboa.tecnico.socialsoftware.tutor.clarification.dto.ClarificationAnswerDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.clarification.domain.Clarification;
import pt.ulisboa.tecnico.socialsoftware.tutor.clarification.dto.ClarificationDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.clarification.repository.ClarificationAnswerRepository;
import pt.ulisboa.tecnico.socialsoftware.tutor.clarification.repository.ClarificationRepository;
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage;
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.QuestionRepository;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserRepository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.*;

@Service("ClarificationService")
public class ClarificationService {
    @Autowired
    QuestionRepository questionRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    ClarificationRepository clarificationRepository;

    @Autowired
    ClarificationAnswerRepository clarificationAnswerRepository;

    @PersistenceContext
    EntityManager entityManager;

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public ClarificationDto createClarification(Question question, User user, String content) {
        checkQuestion(question);
        checkUser(user, User.Role.STUDENT);
        checkContent(content, ErrorMessage.CLARIFICATION_IS_EMPTY);

        Clarification clarification = new Clarification(content, question, user);
        clarificationRepository.save(clarification);
        question.addClarification(clarification);
        user.addClarification(clarification);
        return new ClarificationDto(clarification);
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public ClarificationAnswerDto createClarificationAnswer(Clarification clarification, User user, String content){
        checkClarification(clarification);
        checkUser(user, User.Role.TEACHER);
        checkContent(content, ErrorMessage.CLARIFICATION_ANSWER_IS_EMPTY);

        ClarificationAnswer clarificationAnswer = new ClarificationAnswer(content, clarification, user);
        clarificationAnswerRepository.save(clarificationAnswer);
        clarification.addClarificationAnswer(clarificationAnswer);
        user.addClarificationAnswer(clarificationAnswer);

        return new ClarificationAnswerDto(clarificationAnswer);
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public ClarificationAnswerDto createClarificationAnswer(int clarificationId, String content, int userKey) {
        return createClarificationAnswer(clarificationRepository.findById(clarificationId).orElse(null),
                userRepository.findByKey(userKey),
                content);
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public List<ClarificationAnswerDto> getClarificationAnswers(int clarificationId) {
        Clarification clarification = clarificationRepository.findById(clarificationId).orElse(null);
        if (clarification == null)
            throw new TutorException(ErrorMessage.CLARIFICATION_NOT_FOUND, clarificationId);

        return Lists.newArrayList(clarification.getClarificationAnswers().stream()
            .map(ClarificationAnswerDto::new)
            .sorted(Comparator.comparing(ClarificationAnswerDto::getId))
            .collect(Collectors.toList())
        );
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public ClarificationAnswerDto updateClarificationAnswer(int clarificationAnswerId, String content) {
        ClarificationAnswer clarificationAnswer = clarificationAnswerRepository.findById(clarificationAnswerId)
                .orElseThrow(()-> new TutorException(ErrorMessage.CLARIFICATION_ANSWER_NOT_FOUND, clarificationAnswerId));

        clarificationAnswer.setContent(content);
        return new ClarificationAnswerDto(clarificationAnswer);
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public void removeClarificationAnswer(int clarificationAnswerId) {
        ClarificationAnswer clarificationAnswer = clarificationAnswerRepository.findById(clarificationAnswerId)
                .orElseThrow(()-> new TutorException(ErrorMessage.CLARIFICATION_ANSWER_NOT_FOUND, clarificationAnswerId));

        clarificationAnswer.remove();
        entityManager.remove(clarificationAnswer);
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public CourseDto findClarificationCourseById(int clarificationId) {
        return clarificationRepository.findById(clarificationId)
                .map(Clarification::getQuestion)
                .map(Question::getCourse)
                .map(CourseDto::new)
                .orElseThrow(() -> new TutorException(CLARIFICATION_NOT_FOUND, clarificationId));
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public CourseDto findClarificationAnswerCourseById(int clarificationAnswerId) {
        return clarificationAnswerRepository.findById(clarificationAnswerId)
                .map(ClarificationAnswer::getClarification)
                .map(Clarification::getQuestion)
                .map(Question::getCourse)
                .map(CourseDto::new)
                .orElseThrow(() -> new TutorException(CLARIFICATION_ANSWER_NOT_FOUND, clarificationAnswerId));
    }

    private void checkQuestion(Question question) {
        if (question == null)
            throw new TutorException(ErrorMessage.QUESTION_MISSING_DATA);

        Question qtn = questionRepository.findByKey(question.getKey()).orElse(null);
        if (qtn == null)
            throw new TutorException(ErrorMessage.QUESTION_NOT_FOUND, question.getKey());
    }

    private void checkUser(User user, User.Role role) {
        if (user == null)
            throw new TutorException(ErrorMessage.USER_NOT_FOUND, null);

        User usr = userRepository.findByKey(user.getKey());
        if (usr == null)
            throw new TutorException(ErrorMessage.USER_NOT_FOUND, user.getKey());
        else if (usr.getRole() != role)
            throw new TutorException(ErrorMessage.CLARIFICATION_WRONG_USER, role.toString());
    }

    private void checkContent(String content, ErrorMessage errorMessage) {
        if (content == null || content.isBlank() || content.isEmpty())
            throw new TutorException(errorMessage);
    }

    private void checkClarification(Clarification clarification){
        if (clarification == null)
            throw new TutorException(ErrorMessage.CLARIFICATION_NOT_FOUND, null);
        Clarification clr = clarificationRepository.findById(clarification.getId()).orElse(null);
        if (clr == null)
            throw new TutorException(ErrorMessage.CLARIFICATION_NOT_FOUND, clarification.getId());
    }
}
