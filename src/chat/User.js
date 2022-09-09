class User {
  userId: string;
  name: string;
  avatar: string;
  role: string;
  email: string;
  phone: string;
  location: string;
  browser: string;
  platform: string;
  device: string;
  ipAddress: string;
  hostName: string;
  userAgent: string;

  constructor(props) {
    this.userId = props.userId;
    this.name = props.name;
    this.avatar = props.avatar;
    this.role = props.role;
    this.email = props.email;
    this.phone = props.phone;
    this.location = props.location;
    this.browser = props.browser;
    this.platform = props.platform;
    this.device = props.device;
    this.ipAddress = props.ipAddress;
    this.hostName = props.hostName;
    this.userAgent = props.userAgent;
  }
}

export default User;
