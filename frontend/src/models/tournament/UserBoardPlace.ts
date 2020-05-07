import User from '@/models/user/User';
import Topic from '@/models/management/Topic';
import { ISOtoString } from '@/services/ConvertDateService';

export class UserBoardPlace {
  id!: number;
  correctAnswers!:number;
  place!: number;
  user!: User;

  constructor(jsonObj?: UserBoardPlace) {
    if (jsonObj) {
      this.id = jsonObj.id;
      this.correctAnswers = jsonObj.correctAnswers;
      this.place = jsonObj.place;
      if (jsonObj.user) {
        this.user = new User(jsonObj.user);
      }
    }
  }
}
