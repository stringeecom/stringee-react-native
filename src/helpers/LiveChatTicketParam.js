export default class LiveChatTicketParam {
  name: string;
  email: string;
  avatar: string;
  note: string;

  constructor(name: string, email: string, phone: string, note: string) {
    this.name = name;
    this.email = email;
    this.phone = phone;
    this.note = note;
  }
}
