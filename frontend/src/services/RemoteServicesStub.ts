import User from '@/models/user/User';
import { Tournament } from '@/models/tournament/Tournament';

export default class RemoteServicesStub {
  static async getUserClosedTournaments() {
    let tournament = {
      id: 4217,
      title: 'Testtournament3455556',
      creator: {
        id: 676,
        username: 'Demo-Student',
        name: 'Demo Student',
        role: 'STUDENT',
        creationDate: null
      },
      startingDate: '2020-05-03T14:38:00Z',
      conclusionDate: '2020-05-03T14:39:00Z',
      numberOfQuestions: 10,
      topics: [{ id: 118, name: 'Architecture design', numberOfQuestions: 17 }],
      signedUpUsers: [
        {
          id: 676,
          username: 'Demo-Student',
          name: 'Demo Student',
          role: 'STUDENT',
          creationDate: null
        }
      ],
      leaderboard: [
        {
          user: {
            id: 676,
            username: 'Demo-Student',
            name: 'Demo Student',
            role: 'STUDENT',
            creationDate: null
          },
          correctAnswers: 4,
          place: 1
        },
        {
          user: {
            id: 1,
            username: 'Demo-sdsadsa',
            name: 'sadasdadsadsa Student',
            role: 'STUDENT',
            creationDate: null
          },
          correctAnswers: 4,
          place: 1
        },
        {
          user: {
            id: 1,
            username: '21211-sdsadsa',
            name: '12121 Student',
            role: 'STUDENT',
            creationDate: null
          },
          correctAnswers: 2,
          place: 3
        },
        {
          user: {
            id: 1,
            username: 'alibaba-sdsadsa',
            name: '12121 Student',
            role: 'STUDENT',
            creationDate: null
          },
          correctAnswers: 1,
          place: 4
        },
        {
          user: {
            id: 1,
            username: 'alololo-sdsadsa',
            name: '12121 Student',
            role: 'STUDENT',
            creationDate: null
          },
          correctAnswers: 0,
          place: 5
        },
        {
          user: {
            id: 1,
            username: 'stitch-sdsadsa',
            name: '12121 Student',
            role: 'STUDENT',
            creationDate: null
          },
          correctAnswers: 0,
          place: 5
        }
      ],
      status: 'FINISHED',
      open: false,
      conclusionDateDate: [2020, 5, 3, 14, 39],
      startingDateDate: [2020, 5, 3, 14, 38]
    } as Tournament;
    return [new Tournament(tournament)];
  }
}