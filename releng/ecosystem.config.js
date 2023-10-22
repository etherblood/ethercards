module.exports = {
  apps: [
    {
      name: 'ethercards',
      script: '/usr/lib/jvm/java-17-openjdk-amd64/bin/java',
      args: '-jar a-cards.jar',
      exp_backoff_restart_delay: 100,
    },
  ],
};
