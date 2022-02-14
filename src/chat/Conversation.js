import User from './User';
import Message from './Message';

class Conversation {
  id: string;
  name: string;
  isGroup: boolean;
  updatedAt: number;
  creator: string;
  created: number;
  unreadCount: number;
  participants: Array<User>;
  lastMessage: Message;
  pinMsgId: string;

  constructor(props) {
    this.id = props.id;
    this.name = props.name;
    this.isGroup = props.isGroup;
    this.updatedAt = props.updatedAt;
    this.creator = props.creator;
    this.created = props.created;
    this.unreadCount = props.unreadCount;

    let parts = [];
    props.participants.map(part => {
      let user = new User(part);
      parts.push(user);
    });
    this.participants = parts;

    this.lastMessage = new Message(props);
    this.lastMessage.localId = null;
    this.lastMessage.id = props.lastMsgId;
    this.lastMessage.conversationId = props.id;
    this.lastMessage.sender = props.lastMsgSender;
    this.lastMessage.createdAt = props.lastMsgCreatedAt;
    this.lastMessage.state = props.lastMsgState;
    this.lastMessage.sequence = props.lastMsgSeq;
    this.lastMessage.type = props.lastMsgType;
    this.lastMessage.content = props.text;

    this.pinMsgId = props.pinMsgId != null ? props.pinMsgId : null;
  }
}

export default Conversation;
