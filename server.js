const http = require('http');

const port = process.env.PORT || 3000;
const target = 'https://treningskamp.ofdl.no/';

http
  .createServer((_req, res) => {
    res.writeHead(301, { Location: target });
    res.end();
  })
  .listen(port, () => {
    console.log(`Redirecting all requests to ${target} on port ${port}`);
  });
