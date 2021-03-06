package pt.ulisboa.tecnico.socialsoftware.tutor.tournament.domain;

import pt.ulisboa.tecnico.socialsoftware.tutor.user.User;

import javax.persistence.*;

@Entity
@Table(name = "userBoardPlace")
public class UserBoardPlace {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private int correctAnswers;
    private int place;

    @ManyToOne(cascade = CascadeType.ALL)
    private User user;

    public UserBoardPlace() {
    }

    public UserBoardPlace(User user, int correctAnswers, int place) {
        this.user = user;
        this.correctAnswers = correctAnswers;
        this.place = place;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public int getPlace() {
        return place;
    }

    public void setPlace(int place) {
        this.place = place;
    }

    public int getCorrectAnswers() {
        return correctAnswers;
    }

    public Integer getId() {
        return this.id;
    }

    public void setCorrectAnswers(int correctAnswers) {
        this.correctAnswers = correctAnswers;
    }
}
