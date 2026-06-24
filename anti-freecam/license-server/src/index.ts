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

  server.get('/health', async () => ({ ok: true, ts: new Date().toISOString() }));

  // Rate-limit only the public validate endpoint. Admin routes are protected by the
  // admin secret and may be called in bursts (e.g. keygen-cli generate --count N).
  await server.register(async (publicScope) => {
    await publicScope.register(rateLimit, { max: 30, timeWindow: '1 minute' });
    await publicScope.register(validateRoute, { prefix: '/api/v1' });
  });

  await server.register(adminRoute, { prefix: '/api/v1/admin' });

  try {
    await server.listen({ port: PORT, host: HOST });
    console.log(`[AntiFreeam License Server] Listening on ${HOST}:${PORT}`);
  } catch (err) {
    server.log.error(err);
    process.exit(1);
  }
}

main();
