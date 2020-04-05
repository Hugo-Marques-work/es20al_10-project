export class SimpleUser {
  name!: string;
  username!: string;
  role!: string;

  constructor(jsonObj?: SimpleUser) {
    if (jsonObj) {
      this.name = jsonObj.name;
      this.username = jsonObj.username;
      this.role = jsonObj.role;
    }
  }
}
