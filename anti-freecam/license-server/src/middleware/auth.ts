import { FastifyRequest, FastifyReply } from 'fastify';

const ADMIN_SECRET = process.env.ADMIN_SECRET ?? '';

export async function requireAdminSecret(request: FastifyRequest, reply: FastifyReply) {
  const provided = request.headers['x-admin-secret'];
  if (!ADMIN_SECRET || provided !== ADMIN_SECRET) {
    reply.code(401).send({ error: 'Unauthorized' });
  }
}
