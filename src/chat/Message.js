
class Message {
    constructor(props) {
        this.localId = props.localId;
        this.id = props.id;
        this.conversationId = props.conversationId;
        this.sender = props.sender;
        this.createdAt = props.createdAt;
        this.state = props.state;
        this.sequence = props.sequence;
        this.type = props.type;
        this.content = props.content;
    }
}

export default Message;