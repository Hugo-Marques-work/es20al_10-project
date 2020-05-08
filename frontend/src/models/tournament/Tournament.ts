import User from '@/models/user/User';
import Topic from '@/models/management/Topic';
import { ISOtoString } from '@/services/ConvertDateService';
import { UserBoardPlace } from '@/models/tournament/UserBoardPlace';

export enum TournamentStatus {
  Open = 'Open',
  Running = 'Running',
  Finished = 'Finished',
  Canceled = 'Canceled',
  Undefined = 'Undefined'
}

function stringToTournamentStatus(statusString: string): TournamentStatus {
  if (statusString == 'OPEN') return TournamentStatus.Open;
  else if (statusString == 'RUNNING') return TournamentStatus.Running;
  else if (statusString == 'FINISHED') return TournamentStatus.Finished;
  else if (statusString == 'CANCELED') return TournamentStatus.Canceled;
  return TournamentStatus.Undefined;
}

export class Tournament {
  id!: number;
  title!: string;
  numberOfQuestions!: number;
  startingDate!: string | undefined;
  conclusionDate!: string | undefined;
  creator!: User;
  status!: TournamentStatus;
  topics: Topic[] = [];
  signedUpUsers: User[] = [];
  leaderboard: UserBoardPlace[] = [];

  constructor(jsonObj?: Tournament) {
    if (jsonObj) {
      this.id = jsonObj.id;
      this.title = jsonObj.title;
      this.numberOfQuestions = jsonObj.numberOfQuestions;

      if (jsonObj.startingDate)
        this.startingDate = ISOtoString(jsonObj.startingDate);
      if (jsonObj.conclusionDate)
        this.conclusionDate = ISOtoString(jsonObj.conclusionDate);

      this.status = stringToTournamentStatus(jsonObj.status.toString());

      if (jsonObj.creator) {
        this.creator = new User(jsonObj.creator);
      }
      if (jsonObj.topics) {
        this.topics = jsonObj.topics.map((topic: Topic) => new Topic(topic));
        this.topics = this.topics.sort((topic1, topic2) =>
          topic1.name.localeCompare(topic2.name, 'en', { sensitivity: 'base' })
        );
      }
      if (jsonObj.signedUpUsers) {
        this.signedUpUsers = jsonObj.signedUpUsers.map(
          (user: User) => new User(user)
        );
      }
      if (jsonObj.leaderboard) {
        this.leaderboard = jsonObj.leaderboard.map(
          (ubp: UserBoardPlace) => new UserBoardPlace(ubp)
        );
      }
    }
  }
}
