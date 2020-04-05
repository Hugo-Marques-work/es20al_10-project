import User from '@/models/user/User';
import Topic from '@/models/management/Topic';
import { SimpleUser } from '@/models/user/SimpleUser';

export class Tournament {
  id!: number;
  title!: string;
  startingDate!: string | undefined;
  conclusionDate!: string | undefined;
  creator!: SimpleUser;
  status!: string;
  open!: boolean;
  topics: Topic[] = [];
  signedUpUsers: SimpleUser[] = [];

  constructor(jsonObj?: Tournament) {
    if (jsonObj) {
      console.log(jsonObj);
      this.id = jsonObj.id;
      this.title = jsonObj.title;
      this.startingDate = jsonObj.startingDate;
      this.conclusionDate = jsonObj.conclusionDate;
      this.status = jsonObj.status;
      this.open = jsonObj.open;

      if(jsonObj.creator) {
        this.creator = new SimpleUser(jsonObj.creator);
      }
      if (jsonObj.topics) {
        this.topics = jsonObj.topics.map((topic: Topic) => new Topic(topic));
      }
      if (jsonObj.signedUpUsers) {
        this.signedUpUsers = jsonObj.signedUpUsers.map(
          (user: SimpleUser) => new SimpleUser(user)
        );
      }
    }
  }
}
