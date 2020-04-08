export default class Clarification {
  id: number | null = null;
  content: string = '';
  userId: number | null = null;
  questionId: number | null = null;
  isAnswered: Boolean = false;

  constructor(jsonObj?: Clarification) {
    if (jsonObj) {
      this.id = jsonObj.id;
      this.content = jsonObj.content;
      this.userId = jsonObj.userId;
      this.questionId = jsonObj.questionId;
      this.isAnswered = jsonObj.isAnswered;
    }
  }
}
