package pt.ulisboa.tecnico.socialsoftware.tutor.tournament.dto;

import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.domain.UserBoardPlace;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.dto.UserDto;

import javax.persistence.*;

public class UserBoardPlaceDto {
    private Integer id;
    private int correctAnswers;
    private int place;
    private UserDto user;

    public UserBoardPlaceDto(UserBoardPlace ubp) {
        this(ubp,true);
    }
    public UserBoardPlaceDto(UserBoardPlace ubp, boolean deepCopy) {
        this.correctAnswers = ubp.getCorrectAnswers();
        this.place = ubp.getPlace();

        if(deepCopy) {
            this.user = new UserDto(ubp.getUser());
        }
    }

    public UserDto getUser() {
        return user;
    }

    public void setUser(UserDto user) {
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

    public void setCorrectAnswers(int correctAnswers) {
        this.correctAnswers = correctAnswers;
    }
}