import User from '@/models/user/User';

export default class ClarificationAnswer {
  id: number | null = null;
  content: string = '';
  user!: User;

  constructor(jsonObj?: ClarificationAnswer) {
    if (jsonObj) {
      this.id = jsonObj.id;
      this.content = jsonObj.content;
      this.user = new User(jsonObj.user);
    }
  }
}
