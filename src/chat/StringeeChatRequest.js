class StringeeChatRequest {
    constructor(props) {
        // Request info
        this.id = props.id;
        this.channelType = props.channelType;
        this.type = props.type;

        // Customer info
        this.convId = props.convId;
        this.customerId = props.customerId;
        this.customerName = props.customerName;
    }
}

export default StringeeChatRequest;
