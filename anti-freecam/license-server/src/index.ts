import Fastify from 'fastify';
import cors from '@fastify/cors';
import rateLimit from '@fastify/rate-limit';
import { validateRoute } from './routes/validate';
import { adminRoute } from './routes/admin';

const PORT = parseInt(process.env.PORT ?? '3000', 10);
const HOST = process.env.HOST ?? '0.0.0.0';

async function main() {
  const server = Fastify({ logger: true, trustProxy: true });

  await server.register(cors, { origin: false });
  await server.register(rateLimit, { max: 10, timeWindow: '1 minute' });

  server.get('/health', async () => ({ ok: true, ts: new Date().toISOString() }));

  await server.register(validateRoute, { prefix: '/api/v1' });
  await server.register(adminRoute,   { prefix: '/api/v1/admin' });

  try {
    await server.listen({ port: PORT, host: HOST });
    console.log(`[AntiFreeam License Server] Listening on ${HOST}:${PORT}`);
  } catch (err) {
    server.log.error(err);
    process.exit(1);
  }
}

main();
