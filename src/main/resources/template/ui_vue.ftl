// npm install koa koa-bodyparser koa-static
import core from 'cheese-core';
const Koa = require('koa');
const bodyParser = require('koa-bodyparser');
const serve = require('koa-static');
const path = require('path');
const base=core.base;
const os=core.os;
const app = new Koa();
app.use(bodyParser());
app.use(serve(path.join(os.UI_DIRECTORY.path, '')));
app.use(async (ctx, next) => {
if (ctx.method === 'POST' && ctx.path === '/click') {
console.log('Request body:', ctx.request.body);
try {
const requestBody = ctx.request.body;
if (!requestBody || typeof requestBody.count !== 'number') {
ctx.status = 400;
ctx.body = { error: 'Invalid or missing count value' };
return;
}
const newCount = (requestBody.count || 0) + 1;
ctx.body = { newCount };
ctx.status = 200;
console.log("count is", newCount)
} catch (error) {
ctx.status = 500;
ctx.body = { error: 'Internal Server Error' };
}

} else {
await next();
}
});

app.use(async (ctx) => {
if (ctx.status === 404) {
ctx.body = 'Page Not Found';
}
});
app.listen(3001, () => {
base.runWebView("http://localhost:3001")
});