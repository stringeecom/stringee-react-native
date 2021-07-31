import User from "./User";

class ChatRequest {
    constructor(props) {
        this.id = props.id;
        this.conversationId = props.conversationId;
        this.state = props.state;
        this.createdAt = props.createdAt;
        this.updatedAt = props.updatedAt;

        var parts = [];
        var tempParts = props.participants;
        tempParts.map((part) => {
            var user = new User(part);
            parts.push(user);
        });
        this.participants = parts;

        this.userId = props.userId;
        this.name = props.name;
        this.customerId = props.customerId;
        this.channelType = props.channelType;
        this.requestType = props.requestType;
        this.transferFrom = props.transferFrom;
    }
}

export default ChatRequest;
