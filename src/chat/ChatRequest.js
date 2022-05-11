class ChatRequest {

  constructor(props) {
    // Request info
    this.convId = props.convId;
    this.channelType = props.channelType;
    this.type = props.type;

    // Customer info
    this.customerId = props.customerId;
    this.customerName = props.customerName;
  }
}

export default ChatRequest;
