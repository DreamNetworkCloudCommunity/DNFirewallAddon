# DNFirewallAddon

### You must be running Linux. You must have UFW installed! (via apt-get (apt-get install ufw -y) or yum, ...) 
You need to put it in the addons folder in the DreamNetwork root directory.
Make sure DreamNetwork has root permissions at runtime
If you have a port other than 22 to connect to ssh, please issue this command before running the addon: sudo ufw allow "your port"/tcp example => sudo ufw allow 2222/tcp
