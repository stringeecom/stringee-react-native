class User {
  userId: string;
  name: string;
  avatar: string;
  constructor(props) {
    this.userId = props.userId;
    this.name = props.name;
    this.avatar = props.avatar;
  }
}

export default User;
