import { FastifyInstance } from 'fastify';
import { requireAdminSecret } from '../middleware/auth';
import { LicenseService } from '../services/LicenseService';

export async function adminRoute(fastify: FastifyInstance) {
  // All admin routes require the admin secret header
  fastify.addHook('preHandler', requireAdminSecret);

  // Generate a new license key
  fastify.post<{
    Body: { notes?: string; expiresAt?: string };
  }>('/generate', async (request, reply) => {
    const { notes, expiresAt } = request.body ?? {};
    const key = await LicenseService.generate(notes, expiresAt);
    return reply.code(201).send({ key });
  });

  // Revoke a license key
  fastify.post<{ Body: { key: string } }>('/revoke', async (request, reply) => {
    const { key } = request.body;
    await LicenseService.revoke(key);
    return reply.code(200).send({ ok: true });
  });

  // Remove server binding (allows key to move to new server)
  fastify.post<{ Body: { key: string } }>('/unbind', async (request, reply) => {
    const { key } = request.body;
    await LicenseService.unbind(key);
    return reply.code(200).send({ ok: true });
  });

  // List all licenses
  fastify.get('/licenses', async (_request, reply) => {
    const licenses = await LicenseService.listAll();
    return reply.code(200).send(licenses);
  });
}
