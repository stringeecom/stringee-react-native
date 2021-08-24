class StringeeServerAddress {
    constructor(host: string, port: number) {
        this.host = host;
        this.port = Number.isInteger(port) ? port : 0;
    }
}

export default StringeeServerAddress;
