const http = require('http');
const app = require('./app');
const port = process.env.PORT || 3000;
const { initializeSocket } = require('./socket');

const server = http.createServer(app);


// Initialize socket.io
initializeSocket(server);

server.listen(port, () => {
  console.log(`Server is running on port number ${port}`);
});