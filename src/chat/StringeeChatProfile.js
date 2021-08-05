import User from './StringeeQueue';
import StringeeQueue from './StringeeQueue';

class StringeeChatProfile {
    constructor(props) {
        this.id = props.id;
        this.autoCreateTicket = props.autoCreateTicket;
        this.background = props.background;
        this.enabled = props.enabled;
        this.facebookAsLivechat = props.facebookAsLivechat;
        this.hour = props.hour;
        this.language = props.language;
        this.logoUrl = props.logoUrl;
        this.popupAnswerUrl = props.popupAnswerUrl;
        this.portal = props.portal;
        this.projectId = props.projectId;
        this.zaloAsLivechat = props.zaloAsLivechat;

        this.queues = [];
        props.queues.map((loopQueue) => {
            var queue = new StringeeQueue(loopQueue);
            this.queues.push(queue);
        });
    }
}

export default StringeeChatProfile;
