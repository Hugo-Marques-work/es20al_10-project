import Course from '@/models/user/Course';

interface CourseMap {
  [key: string]: Course[];
}

export default class User {
  name: string = 'Anonymous';
  username!: string;
  role!: string;
  courses: CourseMap = {};
  coursesNumber: number = 0;
  clarificationDashboardPublic: boolean = false;

  constructor(jsonObj?: User) {
    if (jsonObj) {
      this.name = jsonObj.name;
      this.username = jsonObj.username;
      this.role = jsonObj.role;
      this.clarificationDashboardPublic = jsonObj.clarificationDashboardPublic;

      if (jsonObj.courses != null) {
        for (let [name, courses] of Object.entries(jsonObj.courses)) {
          this.courses[name] = courses.map(course => new Course(course));
          this.coursesNumber += this.courses[name].length;
        }
      }
    }
  }
}
