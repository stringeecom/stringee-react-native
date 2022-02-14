export default class UserInfoParam {
  name: string;
  email: string;
  avatar: string;
  phone: string;

  constructor(name: string, email: string, avatar: string, phone: string) {
    this.name = name;
    this.email = email;
    this.avatar = avatar;
    this.phone = phone;
  }
}
