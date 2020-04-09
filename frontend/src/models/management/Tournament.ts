import User from '@/models/user/User';
import Topic from '@/models/management/Topic';
import { SimpleUser } from '@/models/user/SimpleUser';

export enum TournamentStatus {
  Open = 'Open',
  Running = 'Running',
  Finished = 'Finished',
  Canceled = 'Canceled'
}
export class Tournament {
  id!: number;
  title!: string;
  startingDate!: string | undefined;
  conclusionDate!: string | undefined;
  creator!: SimpleUser;
  status!: TournamentStatus;
  topics: Topic[] = [];
  signedUpUsers: SimpleUser[] = [];

  constructor(jsonObj?: Tournament) {
    if (jsonObj) {
      this.id = jsonObj.id;
      this.title = jsonObj.title;
      this.startingDate = jsonObj.startingDate;
      this.conclusionDate = jsonObj.conclusionDate;
      if (jsonObj.status.toString() == 'OPEN')
        this.status = TournamentStatus.Open;
      else if (jsonObj.status.toString() == 'RUNNING')
        this.status = TournamentStatus.Running;
      else if (jsonObj.status.toString() == 'FINISHED')
        this.status = TournamentStatus.Finished;
      else if (jsonObj.status.toString() == 'CANCELED')
        this.status = TournamentStatus.Canceled;
      else this.status = jsonObj.status;
      if (jsonObj.creator) {
        this.creator = new SimpleUser(jsonObj.creator);
      }
      if (jsonObj.topics) {
        this.topics = jsonObj.topics.map((topic: Topic) => new Topic(topic));
        this.topics = this.topics.sort((topic1, topic2) =>
          topic1.name.localeCompare(topic2.name, 'en', { sensitivity: 'base' })
        );
      }
      if (jsonObj.signedUpUsers) {
        this.signedUpUsers = jsonObj.signedUpUsers.map(
          (user: SimpleUser) => new SimpleUser(user)
        );
      }
    }
  }
}
