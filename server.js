const http = require('http');

const port = process.env.PORT || 3000;
const base = 'https://treningskamp.ofdl.no';
const fallback = `${base}/`;

const rules = [
  { match: (path) => path === '/matches' || path.startsWith('/matches/'), target: `${base}/kamper` },
  { match: (path) => path === '/clubs/matches/new', target: `${base}/ny-kamp` },
  { match: (path) => path === '/login', target: `${base}/auth/login` },
  { match: (path) => path === '/admin/matches' || path.startsWith('/admin/matches/'), target: `${base}/admin/kamper` },
];

const resolve = (path) => {
  const rule = rules.find((r) => r.match(path));
  return rule ? rule.target : fallback;
};

http
  .createServer((req, res) => {
    const path = req.url.split('?')[0];
    res.writeHead(301, { Location: resolve(path) });
    res.end();
  })
  .listen(port, () => {
    console.log(`Redirecting requests on port ${port}, fallback ${fallback}`);
  });
